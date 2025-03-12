package services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.*;
import model.ContainerRunParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    @Autowired
    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /// Container management ///

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

    public List<Image> getAllImages() {
        return dockerClient.listImagesCmd().exec();
    }

    public void restartContainer(String containerName) { dockerClient.restartContainerCmd(containerName).exec();}

    public void configureContainer(String containerName, String config) {
        // Implementation to configure a container, possibly updating environment variables or settings
        // This is a placeholder as Docker API does not directly support reconfiguring a running container
    }

    public boolean isContainerCrashed(String containerName) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .anyMatch(container -> container.getNames()[0].equals(containerName) && "Exited".equals(container.getState()));
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

    /// Image management ///

    public String pullImage(String imageName) throws InterruptedException {
        dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName).exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        return "Container with ID '" + container.getId() + "' is now running!";
    }

    public String buildDockerfile(String tag, Path path) {
        return dockerClient.buildImageCmd()
                .withDockerfile(new File(path.toString()))
                .withPull(true)
                .withTags(new HashSet<>(List.of(tag)))
                .exec(new BuildImageResultCallback())
                .awaitImageId();
    }

    public String startImage (ContainerRunParam params) {
        CreateContainerCmd containerBuilder = dockerClient.createContainerCmd(params.getImage());
        if (params.getName() != null) {
            containerBuilder.withName(params.getName());
        }
        if (params.getPorts() != null) {
            List<ExposedPort> ports = new ArrayList<>();
            for (String port : params.getPorts().split(":")) {
                ExposedPort exposedPort = ExposedPort.tcp(Integer.parseInt(port));
                ports.add(exposedPort);
            }
            containerBuilder.withExposedPorts(ports);
        }
        if (params.getEnv() != null) {
            containerBuilder.withEnv(params.getEnv());
        }
        if (params.getVolume() != null) {
            containerBuilder.withVolumes(new Volume(params.getVolume()));
        }
        if (params.getCommand() != null) {
            containerBuilder.withCmd(params.getCommand());
        }
        CreateContainerResponse container = containerBuilder.exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        return "Container with ID '" + container.getId() + "' is now running!";
    }

    public void removeImage(String imageId) {
        dockerClient.removeImageCmd(imageId).exec();
    }

    public void listenAppCrash() {
        //dockerClient.eventsCmd().exec(new ApplicationCrashListener());
    }
}
