package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.user.dto.UserResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserEntryService userEntryService;
    private final UserMapper userMapper;

    public AdminController(UserEntryService userEntryService, UserMapper userMapper) {
        this.userEntryService = userEntryService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userEntryService.getAll().stream().map(userMapper::toResponse).toList();
    }
}

