package com.darshan.journalApplication.controller;


import com.darshan.journalApplication.apiresponse.WeatherResponse;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.service.WeatherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "User APIs" , description = "Greet, Update ,Delete")
public class UserController {
    private final UserEntryService userEntryService;
    private final UserEntryRepository userEntryRepository;
    private final WeatherService weatherService;

    public UserController(UserEntryService userEntryService,
                          UserEntryRepository userEntryRepository,
                          WeatherService weatherService) {
        this.userEntryService = userEntryService;
        this.userEntryRepository = userEntryRepository;
        this.weatherService = weatherService;
    }

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
        WeatherResponse bangalore = weatherService.getWeather("Bangalore");
        String greetings = " ";
        if(bangalore != null){
            greetings = "Weather Feels like " + bangalore.getCurrent().getFeelslike();
        }
        return new ResponseEntity<>("Hello " + authentication.getName().toUpperCase()+ " " + greetings , HttpStatus.OK);
    }
}
