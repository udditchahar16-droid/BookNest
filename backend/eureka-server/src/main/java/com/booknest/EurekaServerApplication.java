package com.booknest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * BookNest Eureka Discovery Server
 *
 * Runs on port 8761. All microservices register here so that booknest-web
 * (and any future services) can discover them by name instead of hard-coding
 * URLs. The dashboard is available at http://localhost:8761 in a browser.
 *
 * Note: This server is OPTIONAL — BookNest-RabbitMQ works fine without it
 * because booknest-web uses fixed base-URLs in application.properties.
 * Add Eureka client dependency + eureka.client.service-url.defaultZone to
 * each service's application.properties when you want full service discovery.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
