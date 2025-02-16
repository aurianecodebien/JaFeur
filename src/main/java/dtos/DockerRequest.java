package dtos;

import jakarta.validation.constraints.NotBlank;

public class DockerRequest {

    @NotBlank(message = "Image name must not be blank")
    private String imageName;

    // Getters et Setters
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
