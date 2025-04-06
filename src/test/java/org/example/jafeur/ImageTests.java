package org.example.jafeur;

import com.github.dockerjava.api.model.Image;
import model.ContainerRunParam;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import services.DockerService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageTests {

    @Autowired
    private DockerService dockerService;

    @Test
    void testPullImage() throws InterruptedException {
        String imageName = "hello-world:latest";
        String result = dockerService.pullImage(imageName);
        assertTrue(result.contains("is now running"));
        // remove container and image
        dockerService.stopContainer(result.split("'")[1]);
        dockerService.removeContainer(result);
        dockerService.removeImage("hello-world:latest");
    }

    @Test
    void testGetAllImages() {
        List<Image> images = dockerService.getAllImages();
        // Assert that the list is not null and not empty
        assertNotNull(images);
        assertFalse(images.isEmpty());
        // Assert that the list contains the hello-world image
        assertTrue(images.stream().anyMatch(image -> image.getRepoTags()[0].equals("hello-world:latest")));
    }

    @Test
    void testRemoveImage() {
        String imageId = "hello-world:latest";
        dockerService.removeImage(imageId);
        assertFalse(dockerService.getAllImages().stream()
                .anyMatch(image -> image.getRepoTags()[0].equals(imageId)));
    }

    @Test
    void testBuildDockerfile() {
        Path dockerfilePath = Paths.get("src/test/resources/Dockerfile");
        String tag = "test-image:latest";
        String imageId = dockerService.buildDockerfile(tag, dockerfilePath);
        assertNotNull(imageId);
        assertTrue(dockerService.getAllImages().stream().anyMatch(image -> image.getRepoTags()[0].equals(tag)));
    }

    @Test
    void testStartImage() {
        ContainerRunParam params = new ContainerRunParam("test-start-container", "8080:80", Map.of("ENV_VAR", "value"), "test-image", "/data", "echo Hello");
        String result = dockerService.startImage(params);
        assertTrue(result.contains("is now running"));

        // remove container and image
        dockerService.stopContainer("test-start-container");
        dockerService.removeContainer("test-start-container");
        dockerService.removeImage("test-image");
    }

}
