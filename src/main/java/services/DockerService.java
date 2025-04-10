package services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.*;
import model.ContainerRunParam;
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

    public ResponseEntity<String> configApp(String id, Map<String, String> conf) {

        ContainerRunParam params = new ContainerRunParam(
                dockerClient.inspectContainerCmd(id).exec().getName(),
                dockerClient.inspectContainerCmd(id).exec().getNetworkSettings().getPorts().toString(),
                conf,
                dockerClient.inspectContainerCmd(id).exec().getImageId(),
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

    public String startImage(ContainerRunParam params) {
        CreateContainerCmd containerBuilder = dockerClient.createContainerCmd(params.getImage());

        // Définir le nom du conteneur
        if (params.getName() != null) {
            containerBuilder.withName(params.getName());
        }

        // Générer un port basé sur l'ID (évite conflits)
//        int defaultPort = 80; // Port interne de l'app
//        int hostPort = generatePortFromName(params.getName()); // Port unique basé sur le nom
//
//        // Exposer le port
//        ExposedPort exposedPort = ExposedPort.tcp(defaultPort);
//        HostConfig hostConfig = new HostConfig()
//                .withPortBindings(new PortBinding(Ports.Binding.bindPort(hostPort), exposedPort));
//
//// Appliquer d'abord l'exposition des ports, puis le HostConfig
//        containerBuilder
//                .withExposedPorts(exposedPort)
//                .withHostConfig(hostConfig); // Appliquer la config après

        containerBuilder
                .withExposedPorts(ExposedPort.tcp(80));
        //        .withHostConfig(new HostConfig().withNetworkMode("web")); // <-- le réseau Docker partagé avec Traefik  // enlevé car bugs pour les tests



        // Ajouter les variables d'environnement
        if (params.getEnv() != null) {
            List<String> env = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.getEnv().entrySet()) {
                env.add(entry.getKey() + "=" + entry.getValue());
            }
            containerBuilder.withEnv(env);
        }

        // Ajouter le volume si défini
        if (params.getVolume() != null) {
            containerBuilder.withVolumes(new Volume(params.getVolume()));
        }

        // Ajouter une commande spécifique si définie
        if (params.getCommand() != null) {
            containerBuilder.withCmd(params.getCommand());
        }

        // Ajouter les labels pour Traefik/Nginx
        containerBuilder.withLabels(Map.of(
                "traefik.enable", "true",
                "traefik.http.routers." + params.getName() + ".rule", "Host(`jafeur-" + params.getName() + ".localhost`)",
                "traefik.http.routers." + params.getName() + ".entrypoints", "web",
                "traefik.http.services." + params.getName() + ".loadbalancer.server.port", "80"
        ));


        // Exécuter la création du conteneur
        CreateContainerResponse container = containerBuilder.exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        return "Container " + params.getName() + " started! Accessible at http://jafeur-" + params.getName() + ".localhost";
    }

    private int generatePortFromName(String name) {
        int hash = name.hashCode();
        int basePort = 10000; // Évite conflits avec ports système
        return basePort + (Math.abs(hash) % 50000); // Ports entre 10000 et 60000
    }

    public void removeImage(String imageId) {
        dockerClient.removeImageCmd(imageId).exec();
    }

}
