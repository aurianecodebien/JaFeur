//package controllers;
//
//import dtos.DockerRequest;
//import entities.Application;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import services.ApplicationService;
//import services.DockerService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("App")
//public class ApplicationController {
//
//    private final DockerService dockerService;
//
//
//    @Autowired
//    public ApplicationController(DockerService dockerService) {
//        this.dockerService = dockerService;
//    }
//
//    // **Retrieve application information**
////    @GetMapping("/List")
////    @Operation(summary = "List all applications", description = "Returns a list of all applications with their details.")
////    @Tag(name = "Retrieve Information")
////    public ResponseEntity<List<Application>> listAppInfo() {
////        List<Application> apps = applicationService.getAll();
////        return ResponseEntity.ok(apps);
////    }
////
////    @GetMapping("/{id}")
////    @Operation(summary = "Get application details", description = "Returns details of a specific application by its ID.")
////    @Tag(name = "Retrieve Information")
////    public ResponseEntity<Application> findAppInfo(@PathVariable("id") int id) {
////        Application app = applicationService.getById(id);
////        return ResponseEntity.ok(app);
////    }
//
//    // **Change application status (start, stop, restart)**
//    @PutMapping("/Start/{id}")
//    @Operation(summary = "Start a specific application", description = "Starts a specific application by its ID.")
//    @Tag(name = "Change Status")
//    public String startApp(@PathVariable("id") int id) {
//        return "ok";
//    }
//
//    @PutMapping("/Stop/{id}")
//    @Operation(summary = "Stop an application", description = "Stops a specific application by its ID.")
//    @Tag(name = "Change Status")
//    public String stopApp(@PathVariable("id") int id) {
//        return "ok";
//    }
//
//    @PutMapping("/Restart/{id}")
//    @Operation(summary = "Restart an application", description = "Restarts a specific application by its ID.")
//    @Tag(name = "Change Status")
//    public String restartApp(@PathVariable("id") int id) {
//        return "ok";
//    }
//
//    // **Configure application settings**
//    @PostMapping("/Config/{id}")
//    @Operation(summary = "Configure an application", description = "Configures a specific application with the provided parameters.")
//    @Tag(name = "Configuration")
//    public String configApp(@PathVariable("id") int id, @RequestBody String config) {
//        return "ok";
//    }
//
//    // **Check crash status**
//    @PutMapping("IsCrash/{id}")
//    @Operation(summary = "Check application crash status", description = "Checks whether a specific application has crashed.")
//    @Tag(name = "Crash Status and Errors")
//    public String isCrash(@PathVariable("id") int id) {
//        return "ok";
//    }
//
//    @PutMapping("List/IsCrash")
//    @Operation(summary = "List crashed applications", description = "Returns a list of applications that have crashed.")
//    @Tag(name = "Crash Status and Errors")
//    public String isCrashList() {
//        return "ok";
//    }
//
//
//    @GetMapping("/start-nginx")
//    public String startNginx() {
//        return dockerService.startNginxContainer();
//    }
//}
