package entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "application")
public class Application {

    public Application() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty
    private Integer id;

    @Column(name = "name")
    @JsonProperty
    private String name;

    @Column(name = "status")
    @JsonProperty
    private int status;

    @Column(name = "url")
    @JsonProperty
    private String url;

}
