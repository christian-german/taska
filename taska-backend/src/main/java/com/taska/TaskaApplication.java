package com.taska;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Modulithic(systemName = "Taska")
public class TaskaApplication {
    static void main(String[] args) {
        SpringApplication.run(TaskaApplication.class, args);
    }
}
