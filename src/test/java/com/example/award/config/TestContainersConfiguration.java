package com.example.award.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base test configuration using Testcontainers for MongoDB
 * This provides a real MongoDB instance running in Docker for integration tests.
 * Sets spring.data.mongodb.uri via system property so it applies when this config is @Import'd
 * (DynamicPropertySource is only processed on the test class, not on imported config).
 */
@TestConfiguration
public class TestContainersConfiguration {

    private static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
                .withExposedPorts(27017)
                .withReuse(true);
        mongoDBContainer.start();
        String uri = mongoDBContainer.getConnectionString();
        System.setProperty("spring.data.mongodb.uri", uri);
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        return mongoDBContainer;
    }
}
