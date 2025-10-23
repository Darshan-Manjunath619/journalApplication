package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserDetailsImp;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.utils.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("public")
@Slf4j
@Tag(name = "Public Controller" ,description = "Health , Login Controllers")
public class PublicController {

    @Autowired
    private UserEntryService userEntryService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsImp userDetailsImp;

    @Autowired
    private JwtUtil jwtUtil;

    // Health Controller
    @GetMapping("/health-checkup")
    public static String health(){
        return "ok application is running with port 8080 in local !";
    }

    @PostMapping("/signup")
    public void signUp(@RequestBody User user){
        userEntryService.saveNewUser(user);
    }

    //User Login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(),user.getPassword()));
            UserDetails userDetails = userDetailsImp.loadUserByUsername(user.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);

        } catch (AuthenticationException e) {
            log.error("Error Occured while Generating JWT Token :  " + e);
            return new ResponseEntity<>("Incorrect Password or Username " ,HttpStatus.BAD_REQUEST);
        }

    }
}
