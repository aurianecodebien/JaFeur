package org.example.jafeur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.example.jafeur", "controllers", "services", "config"})
@EntityScan(basePackages = {"model"})
public class JaFeurApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaFeurApplication.class, args);
	}

}
