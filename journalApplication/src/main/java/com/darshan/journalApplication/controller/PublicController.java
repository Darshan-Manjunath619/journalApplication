package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("public")

public class PublicController {

    @Autowired
    private UserEntryService userEntryService;

    // Get request to verify the application is running
    @GetMapping
    public static String health(){
        return "ok application is running with port 8080 in local !";
    }

    @PostMapping
    public void createNewUser(@RequestBody User user){
        userEntryService.saveNewUser(user);
    }
}
