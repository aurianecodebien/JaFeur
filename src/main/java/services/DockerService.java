//package services;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.core.DefaultDockerClientConfig;
//import com.github.dockerjava.core.DockerClientImpl;
//import com.github.dockerjava.transport.httpclient5.ApacheHttpClient5Transport;
//
//import org.springframework.stereotype.Service;
//
//@Service
//public class DockerService {
//
//    private final DockerClient dockerClient;
//
//    public DockerService() {
//        // Configuration pour le client Docker
//        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withDockerHost("unix:///var/run/docker.sock") // Sur Linux/macOS
//                // .withDockerHost("tcp://localhost:2375") // Sur Windows avec Docker Desktop
//                .build();
//
//        // Création du client Docker
//        this.dockerClient = DockerClientImpl.getInstance(config,
//                new ApacheHttpClient5Transport(config));
//
//    }
//
//    public String startNginxContainer() {
//        // Pull l’image Nginx si elle n'existe pas
//        dockerClient.pullImageCmd("nginx").start();
//
//        // Créer et démarrer le conteneur
//        var container = dockerClient.createContainerCmd("nginx")
//                .withName("nginx-test")
//                .withHostConfig(new com.github.dockerjava.api.model.HostConfig())
//                .exec();
//
//        dockerClient.startContainerCmd(container.getId()).exec();
//
//        return "Nginx lancé avec ID : " + container.getId();
//    }
//}
