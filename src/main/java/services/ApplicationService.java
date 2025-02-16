package services;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import entities.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ApplicationRepository;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DockerClient dockerClient;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, DockerClient dockerClient) {
        this.applicationRepository = applicationRepository;
        this.dockerClient = dockerClient;
    }

    public Application getById(Integer id) {
        return applicationRepository.findById(id).orElse(null);
    }

    public List<Application> getAll() {
        return applicationRepository.findAll();
    }


    public String pullImage(String imageName) throws InterruptedException {
        dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName).exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        return "Container with ID '" + container.getId() + "' is now running!";
    }

    public void stopContainer(String containerName) {
        dockerClient.stopContainerCmd(containerName).exec();
    }

    public void removeContainer(String containerName) {
       dockerClient.removeContainerCmd(containerName).exec();
    }

    public void startContainer(String containerName) {
        dockerClient.startContainerCmd(containerName).exec();
    }
}








