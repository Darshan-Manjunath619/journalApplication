package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin",description = "GetAll,SignUp")
public class AdminController {
    @Autowired
    private UserEntryService userEntryService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(){
        List<User> all = userEntryService.getAll();
        return new ResponseEntity<>(all,HttpStatus.OK);
    }

    //User SignUp
    @PostMapping("/signup")
    public void signUp(@RequestBody User user){
        userEntryService.saveNewUser(user);
    }
}

