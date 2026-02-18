package com.example.taskbot.Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.taskbot")
public class TaskbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskbotApplication.class, args);
	}

}
