package com.booknest.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BookNest Web Application — Spring MVC + Thymeleaf
 *
 * This module is the "website controller" described in the case study (section 4.9).
 * It does NOT own any database or domain data; it acts as a thin MVC aggregator that:
 *   1. Renders Thymeleaf HTML pages for customers and admins.
 *   2. Calls the back-end microservices (auth, book, cart, order, wallet,
 *      review, notification, wishlist) via RestTemplate.
 *   3. Manages the HTTP session (userId, token, role) after a successful login.
 *
 * Port: 8080 (see application.properties)
 */
@SpringBootApplication
public class BookNestWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookNestWebApplication.class, args);
    }
}
