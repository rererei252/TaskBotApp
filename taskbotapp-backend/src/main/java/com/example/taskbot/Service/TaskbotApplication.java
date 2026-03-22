package com.example.taskbot.Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.taskbot")
@AutoConfigurationPackage(basePackages = "com.example.taskbot")
@EnableJpaRepositories(basePackages = "com.example.taskbot")
public class TaskbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskbotApplication.class, args);
    }
}
