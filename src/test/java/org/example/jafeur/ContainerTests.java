package org.example.jafeur;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import services.DockerService;

import java.awt.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContainerTests {

    @Autowired
    private DockerService dockerService;

    private final DockerClient dockerClient;

    @Autowired
    public ContainerTests(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @BeforeAll
    static void setUp() {

    }

    @AfterAll
    static void tearDown() {

    }

    @Test
    @Order(1)
    void testStartContainer() throws InterruptedException {
        Path dockerfilePath = Paths.get("src/test/resources/Dockerfile");
        String tag = "test-image:latest";
        String imageId = dockerService.buildDockerfile(tag, dockerfilePath);

        CreateContainerResponse createdContainer = dockerClient.createContainerCmd(imageId)
                .withName("test-container")
                .exec();

        dockerClient.startContainerCmd(createdContainer.getId()).exec();
        assertTrue(dockerService.getRunningContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + "test-container")));
    }

    @Test
    @Order(2)
    void testRestartContainer() {
        String containerName = "test-container";
        dockerService.restartContainer(containerName);
        assertTrue(dockerService.getRunningContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName)));
    }

    @Test
    @Order(3)
    void testGetRunningContainers() {
        assertNotNull(dockerService.getRunningContainers());
    }

    @Test
    @Order(4)
    void testGetAllContainers() {
        assertNotNull(dockerService.getAllContainers());
    }

    @Test
    @Order(5)
    void testStopContainer() {
        String containerName = "test-container";
        dockerService.stopContainer(containerName);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "update", "--restart=no", containerName);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to update the restart policy");
        }
        assertFalse(dockerService.getRunningContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName)));
        assertTrue(dockerService.getAllContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName) && container.getState().equals("exited")));
    }

    @Test
    @Order(6)
    void testRemoveContainer() {
        String containerName = "test-container";
        dockerService.removeContainer(containerName);
        assertFalse(dockerService.getAllContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName)));
    }

    @Test
    void testListCrashedContainers() {
        // Test the listCrashedContainers method
    }

    @Test
    void testIsContainerCrashed() {
        // Test the isContainerCrashed method
    }

    @Test
    void testConfigApp() {
        // Test the configApp method
    }

}
