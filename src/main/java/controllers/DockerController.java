package controllers;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import model.ContainerRunParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import services.DockerService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
public class DockerController {

    private final DockerService dockerService;

    @Autowired
    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @PutMapping("/Start/{name}")
    @Operation(summary = "Start a specific application", description = "Starts a specific application by its name.")
    @Tag(name = "Change Status")
    public void startApp(@PathVariable("name") String name) {
        dockerService.startContainer(name);
    }

    @PutMapping("/Stop/{name}")
    @Operation(summary = "Stop an application", description = "Stops a specific application by its name.")
    @Tag(name = "Change Status")
    public void stopApp(@PathVariable("name") String name) {
        dockerService.stopContainer(name);
    }

    @PutMapping("/Remove/{name}")
    @Operation(summary = "Remove an application", description = "Remove a specific application by its name.")
    @Tag(name = "Change Status")
    public void removeApp(@PathVariable("name") String name) {
        dockerService.removeContainer(name);
    }

    @PutMapping("/Restart/{name}")
    @Operation(summary = "Restart an application", description = "Restarts a specific application by its name.")
    @Tag(name = "Change Status")
    public void restartApp(@PathVariable("name") String name) {
        dockerService.restartContainer(name);
    }

    @PostMapping("/Config/byName/{name}")
    @Operation(
            summary = "Configure an application",
            description = "Allows adding, updating, or deleting environment variables of a container. The JSON body must contain `add`, `update`, and/or `delete` keys."
    )
    @Tag(name = "Application")
    public ResponseEntity<String> configAppByName(@PathVariable("name") String name, @RequestBody Map<String, Object> config) {
        return dockerService.configApp(name, config);
    }

    @PutMapping("IsCrash/{name}")
    @Operation(summary = "Check application crash status", description = "Checks whether a specific application has crashed.")
    @Tag(name = "Crash Status and Errors")
    public boolean isCrash(@PathVariable("name") String name) {
        return dockerService.isContainerCrashed(name);
    }

    @PutMapping("List/IsCrash")
    @Operation(summary = "List crashed applications", description = "Returns a list of applications that have crashed.")
    @Tag(name = "Crash Status and Errors")
    public List<String> isCrashList() {
        return dockerService.listCrashedContainers();
    }

    @PostMapping("/image/run/{applicationName}")
    @Operation(summary = "Pull docker image", description = "Pull and deploy a Docker Image. You must specify the version.")
    @Tag(name = "Image")
    public ResponseEntity<String> runDockerImage(@PathVariable("applicationName") String applicationName) {

        try {
            String result = dockerService.pullImage(applicationName);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while running Docker image: " + e.getMessage());
        }
    }

    @PostMapping(path = "/image/buildDockerfile/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Build Docker image from Dockerfile", description = "Build a Docker Image from an uploaded Dockerfile.")
    @Tag(name = "Image")
    public ResponseEntity<String> buildDockerfile(@RequestParam String tag, @RequestParam("file") MultipartFile file) {
        try {
            String id = dockerService.buildDockerfile(tag, file);
            return ResponseEntity.ok(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while building Dockerfile: " + e.getMessage());
        }
    }

    @PostMapping("/image/start/")
    @Operation(summary = "Start Docker image", description = "Start a Docker Image.")
    @Tag(name = "Image")
    public ResponseEntity<String> startImage(@RequestBody ContainerRunParam params) {
        try {
            String result = dockerService.startImage(params);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while starting Docker image: " + e.getMessage());
        }
    }

    @GetMapping("/containers/{showAll}")
    @Operation(summary = "Get all containers", description = "Get a list of all Docker containers.")
    @Tag(name = "Retrieve Information")
    public ResponseEntity<List<Container>> getContainers(@PathVariable Boolean showAll) {
        try {
            // Renvoie soit tous les conteneurs, soit uniquement ceux en cours d'exécution
            List<Container> containers;
            if (showAll) {
                containers = dockerService.getAllContainers();
                return ResponseEntity.ok(containers);
            } else {
                containers = dockerService.getRunningContainers();
            }
            return ResponseEntity.ok(containers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/images")
    @Operation(summary = "Get all images", description = "Get a list of all Docker images.")
    @Tag(name = "Retrieve Information")
    public ResponseEntity<List<Image>> getAllImages() {
        try {
            List<Image> images = dockerService.getAllImages();
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/image")
    @Operation(summary = "Remove image", description = "Remove image by its id.")
    @Tag(name = "Image")
    public ResponseEntity<String> removeImage(@RequestParam String imageId) {
        try {
            dockerService.removeImage(imageId);
            return ResponseEntity.ok("Image removed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update")
    @Operation(summary = "Update application", description = "Update a specific application by its name.")
    @Tag(name = "Update")
    public ResponseEntity<String> updateApp(@RequestParam String name, @RequestParam MultipartFile file) {
        try {
            dockerService.updateApp(name, file);
            return ResponseEntity.ok("Application updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
