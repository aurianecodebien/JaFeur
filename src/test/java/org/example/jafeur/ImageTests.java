package org.example.jafeur;

import com.github.dockerjava.api.model.Image;
import model.ContainerRunParam;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import services.DockerService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImageTests {

    @Autowired
    private DockerService dockerService;

    @Test
    @Order(1)
    void testPullImage() throws InterruptedException {
        String imageName = "hello-world:latest";
        String result = dockerService.pullImage(imageName);
        assertTrue(result.contains("is now running"));
        dockerService.removeContainer(result.split("'")[1]);
    }

    @Test
    @Order(2)
    void testGetAllImages() {
        List<Image> images = dockerService.getAllImages();
        assertNotNull(images);
        assertFalse(images.isEmpty());
        assertTrue(images.stream().anyMatch(image -> Arrays.asList(image.getRepoTags()).contains("hello-world:latest")));
    }

    @Test
    @Order(3)
    void testRemoveImage() {
        String imageId = "hello-world:latest";
        dockerService.removeImage(imageId);
        assertFalse(dockerService.getAllImages().stream()
                .anyMatch(image -> Arrays.asList(image.getRepoTags()).contains((imageId))));
    }

    @Test
    @Order(4)
    void testBuildDockerfile() {
        Path dockerfilePath = Paths.get("src/test/resources/Dockerfile");
        String tag = "test-image:latest";
        String imageId = dockerService.buildDockerfile(tag, dockerfilePath);
        assertNotNull(imageId);
        assertTrue(dockerService.getAllImages().stream().anyMatch(image -> image.getRepoTags()[0].equals(tag)));
    }

    @Test
    @Order(5)
    void testStartImage() {
        ContainerRunParam params = new ContainerRunParam("test-start-container", "8080:80", Map.of("ENV_VAR", "value"), "test-image", "/data", "echo Hello");
        String result = dockerService.startImage(params);
        assertTrue(result.contains("test-start-container started"));

        // remove container and image
        dockerService.removeContainer("test-start-container");
        dockerService.removeImage("test-image");
    }

}
