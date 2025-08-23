package com.darshan.journalApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("health")

public class HealthCheck {

    // Get request to verify the application is running
    @GetMapping

    public static String health(){
        return "ok application is running with port 8080 in local !";
    }

}
