package services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import model.ContainerRunParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.*;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    @Autowired
    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void updateApp(String name, MultipartFile file) {
        try {
            String newImageId = buildDockerfile(name, file);

            int nextVersion = getNextVersion(name);

            String newContainerName = name + "-v" + nextVersion;
            CreateContainerResponse newContainer = dockerClient.createContainerCmd(newImageId)
                    .withName(newContainerName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withRestartPolicy(RestartPolicy.alwaysRestart())
                            .withNetworkMode("traefik-network"))
                    .withLabels(Map.of(
                            "traefik.enable", "true",
                            "traefik.http.routers." + name + ".rule", "Host(`jafeur-" + name + ".localhost`)",
                            "traefik.http.routers." + name + ".entrypoints", "web",
                            "traefik.http.services." + name + ".loadbalancer.server.port", "80"
                    ))
                    .exec();

            dockerClient.startContainerCmd(newContainer.getId()).exec();

            waitForContainerHealth(newContainerName);

            Thread.sleep(500); // Adjust the delay as needed

            dockerClient.stopContainerCmd(name).exec();
            dockerClient.removeContainerCmd(name).exec();

            dockerClient.renameContainerCmd(newContainer.getId()).withName(name).exec();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update application: " + e.getMessage(), e);
        }
    }

    private int getNextVersion(String baseName) {
        List<Container> allContainers = dockerClient.listContainersCmd().withShowAll(true).exec();
        int maxVersion = 0;

        for (Container container : allContainers) {
            String containerName = container.getNames()[0].replaceFirst("/", "");
            if (containerName.startsWith(baseName + "-v")) {
                try {
                    int version = Integer.parseInt(containerName.substring((baseName + "-v").length()));
                    maxVersion = Math.max(maxVersion, version);
                } catch (NumberFormatException ignored) {
                    // Ignore containers with invalid version numbers
                }
            }
        }

        return maxVersion + 1;
    }

    private void waitForContainerHealth(String containerName) throws InterruptedException {
        while (true) {
            try {
                InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
                InspectContainerResponse.ContainerState state = containerInfo.getState();

                if (state.getHealth() == null) {
                    System.out.println("No health check configured for container " + containerName + ". Assuming it is running.");
                    break;
                }

                String healthStatus = state.getHealth().getStatus();

                if ("healthy".equals(healthStatus)) {
                    break;
                } else if ("unhealthy".equals(healthStatus)) {
                    throw new RuntimeException("Container " + containerName + " is unhealthy!");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to inspect container " + containerName + ": " + e.getMessage(), e);
            }

            Thread.sleep(1000);
        }
    }

    /// Container management

    public void stopContainer(String containerName) {
        dockerClient.stopContainerCmd(containerName).exec();
    }

    public void removeContainer(String containerName) {
        dockerClient.removeContainerCmd(containerName).exec();
    }

    public void startContainer(String containerName) {
        dockerClient.startContainerCmd(containerName).exec();
    }

    public List<Container> getRunningContainers() {
        return dockerClient.listContainersCmd().exec();
    }

    public List<Container> getAllContainers() {
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }

    public void restartContainer(String containerName) { dockerClient.restartContainerCmd(containerName).exec();}

    public boolean isContainerCrashed(String containerName) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName) && "exited".equals(container.getState()));
    }

    public List<String> listCrashedContainers() {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> "Exited".equals(container.getState()))
                .map(container -> container.getNames()[0])
                .toList();
    }

    public ResponseEntity<String> configApp(String name, Map<String, Object> conf) {
        try {
            Map<String, String> envMap = new HashMap<>();

            InspectContainerResponse container = dockerClient.inspectContainerCmd(name).exec();

            String[] oldEnvList = container.getConfig().getEnv();
            if (oldEnvList != null) {
                for (String env : oldEnvList) {
                    String[] parts = env.split("=", 2);
                    if (parts.length == 2) {
                        envMap.put(parts[0], parts[1]);
                    }
                }
            }

            Map<String, String> toAdd = (Map<String, String>) conf.getOrDefault("add", Map.of());
            Map<String, String> toUpdate = (Map<String, String>) conf.getOrDefault("update", Map.of());
            Map<String, String> toDelete = (Map<String, String>) conf.getOrDefault("delete", Map.of());

            toAdd.forEach(envMap::put);
            toUpdate.forEach(envMap::put);
            toDelete.keySet().forEach(envMap::remove);

            ContainerRunParam params = new ContainerRunParam(
                    container.getName().replace("/", ""), // le nom avec '/' à retirer
                    container.getNetworkSettings().getPorts().toString(),
                    envMap,
                    container.getImageId(),
                    null,
                    null
            );

            if ("running".equals(container.getState().getStatus())) {
                dockerClient.stopContainerCmd(name).exec();
            }
            dockerClient.removeContainerCmd(name).exec();

            System.out.println("Variables finales envoyées à Docker : " + params.getEnv());

            startImage(params);
            return ResponseEntity.ok(name);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /// Image management

    public String pullImage(String imageName) throws InterruptedException {
        dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName).exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        return "Container with ID '" + container.getId() + "' is now running!";
    }

    public List<Image> getAllImages() {
        return dockerClient.listImagesCmd().exec();
    }


    public String buildDockerfile(String tag, MultipartFile dockerfile) {
        try {
            // Create a temporary directory for the Docker build context
            Path tempDir = java.nio.file.Files.createTempDirectory("docker-build-context");

            // Save the uploaded Dockerfile to the temporary directory
            File dockerfilePath = tempDir.resolve("Dockerfile").toFile();
            dockerfile.transferTo(dockerfilePath);

            // Get the directory where the Dockerfile resides
            File dockerfileFolder = dockerfilePath.getParentFile();

            // Copy all files and subdirectories from the Dockerfile's folder to the temporary directory
            File[] files = dockerfileFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    Path targetPath = tempDir.resolve(file.getName());
                    if (file.isDirectory()) {
                        java.nio.file.Files.walk(file.toPath())
                                .forEach(source -> {
                                    try {
                                        Path destination = tempDir.resolve(file.toPath().relativize(source).toString());
                                        java.nio.file.Files.copy(source, destination);
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failed to copy file: " + source, e);
                                    }
                                });
                    } else {
                        java.nio.file.Files.copy(file.toPath(), targetPath);
                    }
                }
            }

            // Build the Docker image using the temporary directory as the build context
            String imageId = dockerClient.buildImageCmd(tempDir.toFile())
                    .withDockerfile(dockerfilePath)
                    .withPull(true)
                    .withTags(new HashSet<>(List.of(tag)))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();

            // Clean up the temporary directory
            java.nio.file.Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            return imageId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Dockerfile: " + e.getMessage(), e);
        }
    }

    // Lance un conteneur avec les paramètres nécessaires (env, volume, traefik)
    public String startImage(ContainerRunParam params) {
        CreateContainerCmd containerBuilder = dockerClient.createContainerCmd(params.getImage());

        if (params.getName() != null) {
            containerBuilder.withName(params.getName());
        }

        containerBuilder
                .withExposedPorts(ExposedPort.tcp(80));

        if (params.getEnv() != null) {
            List<String> env = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.getEnv().entrySet()) {
                env.add(entry.getKey() + "=" + entry.getValue());
            }
            containerBuilder.withEnv(env);
        }

        if (params.getVolume() != null) {
            containerBuilder.withVolumes(new Volume(params.getVolume()));
        }

        if (params.getCommand() != null) {
            containerBuilder.withCmd(params.getCommand());
        }

        // Configuration des labels pour exposer l'app via Traefik
        containerBuilder.withHostConfig(new HostConfig().withNetworkMode("traefik-network"))
                .withLabels(Map.of(
                "traefik.enable", "true",
                "traefik.http.routers." + params.getName() + ".rule", "Host(`jafeur-" + params.getName() + ".localhost`)",
                "traefik.http.routers." + params.getName() + ".entrypoints", "web",
                "traefik.http.services." + params.getName() + ".loadbalancer.server.port", "80"
        ));


        CreateContainerResponse container = containerBuilder.exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        return "Container " + params.getName() + " started! Accessible at http://jafeur-" + params.getName() + ".localhost";
    }

    public void removeImage(String imageId) {
        dockerClient.removeImageCmd(imageId).exec();
    }

}
