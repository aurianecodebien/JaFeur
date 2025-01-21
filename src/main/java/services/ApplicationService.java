package services;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
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
        // Télécharger une image Docker
        dockerClient.pullImageCmd(imageName)
                .exec(new PullImageResultCallback())
                .awaitCompletion();

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName).exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        return "Container with ID '" + container.getId() + "' is now running!";
    }


//    public void stopAndRemoveContainer(String containerId) {
//        // Arrêter le conteneur
//        dockerClient.stopContainerCmd(containerId).exec();
//
//        // Supprimer le conteneur
//        dockerClient.removeContainerCmd(containerId).exec();
//    }
}








//    public Application updateConfigApp(int id, String configFile) {
//        Application app = applicationRepository.findById(id).orElse(null);
//        if (app != null) {
//            app.setConfigFile(configFile);
////            applicationRepository.save(app); -- il faut update dans docker
//        }
//        return app;
//    }
//
//    private void setConfigFile(String configFile) {
//        // il faut recup si c'est update ou create ou delete
//        // trouver la config et la changer/ajouter/supprimer
//        this.configFile = configFile;
//    }


//private void setConfigFile(String configFile) {
//    // Parse the JSON configFile
//    JSONObject jsonObject = new JSONObject(configFile);
//
//    // Handle 'add' operations
//    JSONArray addArray = jsonObject.getJSONArray("add");
//    for (int i = 0; i < addArray.length(); i++) {
//        JSONObject addObject = addArray.getJSONObject(i);
//        String nomVariable = addObject.getString("nomVariable");
//        // Add logic here (e.g., add to config file)
//    }
//
//    // Handle 'update' operations
//    JSONArray updateArray = jsonObject.getJSONArray("update");
//    for (int i = 0; i < updateArray.length(); i++) {
//        JSONObject updateObject = updateArray.getJSONObject(i);
//        String nomVariable = updateObject.getString("nomVariable");
//        String type = updateObject.getString("type");
//        // Update logic here (e.g., update config file)
//    }
//
//    // Handle 'delete' operations
//    JSONArray deleteArray = jsonObject.getJSONArray("delete");
//    for (int i = 0; i < deleteArray.length(); i++) {
//        JSONObject deleteObject = deleteArray.getJSONObject(i);
//        String nomVariable = deleteObject.getString("nomVariable");
//        // Delete logic here (e.g., delete from config file)
//    }
//}