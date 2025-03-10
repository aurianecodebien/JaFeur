package model;

import lombok.Data;

import java.util.List;

@Data
public class ContainerRunParam {

    private String name;
    private String ports;
    private List<String> env;
    private String image;
    private String volume;
    private String command;

}
