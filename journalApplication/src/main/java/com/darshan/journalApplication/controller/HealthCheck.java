package com.darshan.journalApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("health")

public class HealthCheck {
    @GetMapping

    public static String health(){
        return "ok application is running !";
    }

}
