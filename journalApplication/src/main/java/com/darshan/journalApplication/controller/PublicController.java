package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.auth.dto.*;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.user.dto.UserResponse;
import jakarta.validation.Valid;
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

    @Autowired
    private UserMapper userMapper;

    // Health Controller
    @GetMapping("/health-checkup")
    public static String health(){
        return "ok application is running with port 8080 in local !";
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody RegisterRequest request){
        User saved = userEntryService.saveNewUser(userMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }

    //User Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userName(),request.password()));
        UserDetails userDetails = userDetailsImp.loadUserByUsername(request.userName());
        String jwt = jwtUtil.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(jwt, "Bearer", 3600));
    }
}
