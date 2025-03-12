package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ContainerRunParam {

    private String name;
    private String ports;
    private Map<String, String> env;
    private String image;
    private String volume;
    private String command;

}
