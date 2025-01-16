package listener;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.EventsCmd;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.stereotype.Service;

@Service
public class DockerEventListener {

    private final DockerClient dockerClient;

    public DockerEventListener() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    public void listenToEvents() {
        EventsCmd eventsCmd = dockerClient.eventsCmd();
        eventsCmd.exec(new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Event event) {
                if ("die".equals(event.getStatus())) {
                    String containerId = event.getId();
                    System.out.println("Container stopped, restarting: " + containerId);
                    dockerClient.restartContainerCmd(containerId).exec();
                }
            }
        });
    }
}
