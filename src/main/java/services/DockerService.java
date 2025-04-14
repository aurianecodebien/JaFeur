package services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import model.ContainerRunParam;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    @Autowired
    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void updateApp(String name, String dockerfilePath) {
        try {
            String newImageId = buildDockerfile(name, Path.of(dockerfilePath));

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

    // Applique une nouvelle configuration d'environnement (redéploiement)
    public ResponseEntity<String> configApp(String id, Map<String, String> conf) {

        var inspect = dockerClient.inspectContainerCmd(id).exec();
        ContainerRunParam params = new ContainerRunParam(
                inspect.getName(),
                inspect.getNetworkSettings().getPorts().toString(),
                conf,
                inspect.getImageId(),
                null,
                null
        );

        if ("running".equals(dockerClient.inspectContainerCmd(id).exec().getState().getStatus())) {
            dockerClient.stopContainerCmd(id).exec();
        }

        dockerClient.removeContainerCmd(id).exec();
        try {
            startImage(params);
            return ResponseEntity.ok(id);
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

    public String buildDockerfile(String tag, Path path) {
        return dockerClient.buildImageCmd()
                .withDockerfile(new File(path.toString()))
                .withPull(true)
                .withTags(new HashSet<>(List.of(tag)))
                .exec(new BuildImageResultCallback())
                .awaitImageId();
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
