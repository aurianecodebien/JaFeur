package org.example.jafeur;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import services.DockerService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigTests {

    @Autowired
    private DockerService dockerService;

    private final DockerClient dockerClient;

    @Autowired
    public ConfigTests(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @BeforeAll
    static void setUpBeforeAll() {
        System.out.println("Starting ConfigTests...");
    }

    @AfterAll
    static void tearDownAfterAll() {
        System.out.println("Finished ConfigTests.");
    }

    @Test
    @Order(1)
    void testConfigApp_AddUpdateDeleteEnvVars() throws InterruptedException {
        String containerName = "config-test-container";

        // 🔁 Supprimer s'il existe déjà
        dockerService.getAllContainers().stream()
                .filter(container -> Arrays.asList(container.getNames()).contains("/" + containerName))
                .findFirst()
                .ifPresent(existing -> {
                    try {
                        dockerClient.removeContainerCmd(existing.getId()).withForce(true).exec();
                        System.out.println("Conteneur existant supprimé");
                    } catch (Exception e) {
                        fail("Could not remove existing container: " + e.getMessage());
                    }
                });

        // 🚀 1. Créer le conteneur initial
        CreateContainerResponse created = dockerClient.createContainerCmd("alpine")
                .withName(containerName)
                .withEnv("INITIAL_VAR=initial")
                .withCmd("sh", "-c", "env && sleep 9999")
                .exec();
        dockerClient.startContainerCmd(created.getId()).exec();
        System.out.println("Conteneur initial lancé");

        Thread.sleep(500);

        // ➕ 2. Ajout des variables
        dockerService.configApp(containerName, Map.of(
                "add", Map.of("VAR1", "value1", "VAR2", "toremove")
        ));

        Thread.sleep(1000);

        InspectContainerResponse afterAdd = dockerClient.inspectContainerCmd(containerName).exec();
        String[] rawEnvsAfterAdd = afterAdd.getConfig().getEnv();
        assertNotNull(rawEnvsAfterAdd, "Env list is null after add");

        List<String> envsAfterAdd = Arrays.asList(rawEnvsAfterAdd);
        System.out.println("Env après ajout : " + envsAfterAdd);

        assertTrue(envsAfterAdd.contains("VAR1=value1"), "VAR1=value1 n'est pas présent");
        assertTrue(envsAfterAdd.contains("VAR2=toremove"), "VAR2=toremove n'est pas présent");

        // 🔁 3. Update + Delete
        dockerService.configApp(containerName, Map.of(
                "update", Map.of("VAR1", "updated-value"),
                "delete", Map.of("VAR2", "")
        ));

        Thread.sleep(1000);

        InspectContainerResponse afterUpdate = dockerClient.inspectContainerCmd(containerName).exec();
        String[] rawEnvsAfterUpdate = afterUpdate.getConfig().getEnv();
        assertNotNull(rawEnvsAfterUpdate, "Env list is null after update");

        List<String> envsAfterUpdate = Arrays.asList(rawEnvsAfterUpdate);
        System.out.println("Env après update : " + envsAfterUpdate);

        assertTrue(envsAfterUpdate.contains("VAR1=updated-value"), "VAR1 n'est pas mis à jour");
        assertFalse(envsAfterUpdate.stream().anyMatch(e -> e.startsWith("VAR2=")), "VAR2 aurait dû être supprimé");
    }

}