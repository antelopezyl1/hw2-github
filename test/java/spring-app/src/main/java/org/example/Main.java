package org.example;

import org.example.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * main class of Spring Boot app.
 * start Spring Boot config and internal web server.
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class Main {

    public static void main(String[] args) {
        // start Spring Boot app
        SpringApplication.run(Main.class, args);
    }

}