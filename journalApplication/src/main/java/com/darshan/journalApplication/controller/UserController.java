package com.darshan.journalApplication.controller;


import com.darshan.journalApplication.apiresponse.weatherResponse;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserEntryService userEntryService;

    @Autowired
    private UserEntryRepository userEntryRepository;

    @Autowired
    private WeatherService weatherService;

   @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String name = authentication.getName();
       User userInDb = userEntryService.findByUserName(name);
       if(userInDb != null){
           userInDb.setPassword(user.getPassword());
       }
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
   }

    @DeleteMapping
    public ResponseEntity<?> deleteByUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userEntryRepository.deleteByUserName(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<?> greetings(){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        weatherResponse bangalore = weatherService.getWeather("Bangalore");
        String greetings = "";
        if(bangalore != null){
            greetings = "Weather Feels like " + bangalore.getCurrent().getFeelslike();
        }
        return new ResponseEntity<>("Hello" + authentication.getName() + greetings , HttpStatus.OK);
    }
}
