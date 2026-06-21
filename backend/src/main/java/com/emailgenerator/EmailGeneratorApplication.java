package com.emailgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
@SpringBootApplication
@ConfigurationPropertiesScan
public class EmailGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailGeneratorApplication.class, args);
    }
}
