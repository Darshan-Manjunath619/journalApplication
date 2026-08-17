package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.user.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
public class UsersController {
    private final UserEntryService users;
    private final UserMapper mapper;

    public UsersController(UserEntryService users, UserMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    @GetMapping
    public UserResponse getProfile(Authentication authentication) {
        return mapper.toResponse(users.getProfile(authentication.getName()));
    }

    @PatchMapping
    public UserResponse updateProfile(Authentication authentication,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        User updated = users.updateProfile(authentication.getName(), request);
        return mapper.toResponse(updated);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        users.changePassword(authentication.getName(),
                request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
