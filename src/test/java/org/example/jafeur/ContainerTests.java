package org.example.jafeur;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import services.DockerService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ContainerTests {

    @Autowired
    private DockerService dockerService;

    @BeforeAll
    static void setUp() {

    }

    @AfterAll
    static void tearDown() {

    }

    @Test
    void testStartContainer() {
        String containerName = "test-container";
        dockerService.startContainer(containerName);
        assertTrue(dockerService.getRunningContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName)));
    }

    @Test
    void testStopContainer() {
        String containerName = "test-container";
        dockerService.stopContainer(containerName);
        assertFalse(dockerService.getRunningContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName)));
    }

    @Test
    void testRestartContainer() {
        String containerName = "test-container";
        dockerService.restartContainer(containerName);
        assertTrue(dockerService.getRunningContainers().stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName)));
    }

    @Test
    void testGetRunningContainers() {
        assertNotNull(dockerService.getRunningContainers());
    }

    @Test
    void testGetAllContainers() {
        assertNotNull(dockerService.getAllContainers());
    }

    @Test
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
