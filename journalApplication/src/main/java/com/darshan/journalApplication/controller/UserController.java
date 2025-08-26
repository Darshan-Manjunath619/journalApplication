package com.darshan.journalApplication.controller;


import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserEntryService userEntryService;

   @GetMapping
    public List<User> getAllUser(){
       return userEntryService.getAll();
    }

    @PostMapping
   public void createNewUser(@RequestBody User user){
       userEntryService.saveEntry(user);
   }

   @PutMapping("/{userName}")
    public ResponseEntity<?> updateUser(@RequestBody User user , @PathVariable String userName){
       User userInDb = userEntryService.findByUserName(user.getUserName());
       if(userInDb != null){
           userInDb.setPassword(user.getPassword());
       }
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
   }
}
