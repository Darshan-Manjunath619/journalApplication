package com.darshan.journalApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class HealthCheck {
@GetMapping("/health")
    public static String health() {
        return "ok";

    }
}