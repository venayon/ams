package com.example.award;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main Spring Boot Application for Award Management System
 * 
 * This application demonstrates:
 * - Service Layer pattern with AwardService
 * - Proxy Pattern with AwardRepoProxy ("Traffic cop")
 * - Facade Pattern with AwardFacade ("Decision maker")
 * - Feature Toggle pattern for switching between OldFlow and NewFlow
 * - MongoDB repositories for data persistence
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.award.repository")
public class AwardManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AwardManagementApplication.class, args);
    }
}
