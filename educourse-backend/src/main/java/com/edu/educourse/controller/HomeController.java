package com.edu.educourse.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================
 *  HomeController — Basic Health Check Endpoint
 * ============================================================
 *
 *  A simple controller to confirm the application is running.
 *
 *  Endpoint: GET /
 *  Access:   PUBLIC — no JWT token required.
 *            Configured in SecurityConfig:
 *              .requestMatchers("/").permitAll()
 *
 *  Usage:
 *    GET http://localhost:8080/
 *    Response: "Application is Running"
 * ============================================================
 */
@RestController
public class HomeController {

    /**
     * Returns a simple status message.
     *
     * GET /
     *
     * Access: PUBLIC — no JWT token required.
     * Anyone can call this to verify the server is up and running.
     *
     * @return a plain string indicating the app is running
     */
    @GetMapping("/")
    public String home() {
        return "Application is Running";
    }
}
