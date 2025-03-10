package controllers;

import model.Application;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("App")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // **Retrieve application information**
    @GetMapping("/List")
    @Operation(summary = "List all applications", description = "Returns a list of all applications with their details.")
    @Tag(name = "Retrieve Information")
    public ResponseEntity<List<Application>> listAppInfo() {
        List<Application> apps = applicationService.getAll();
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details", description = "Returns details of a specific application by its ID.")
    @Tag(name = "Retrieve Information")
    public ResponseEntity<Application> findAppInfo(@PathVariable("id") int id) {
        Application app = applicationService.getById(id);
        return ResponseEntity.ok(app);
    }

}
