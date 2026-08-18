package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.user.dto.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "Profile")
@SecurityRequirement(name = "bearerAuth")
public class UsersController {
    private final UserEntryService users;
    private final UserMapper mapper;

    public UsersController(UserEntryService users, UserMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Get current profile")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    public UserResponse getProfile(Authentication authentication) {
        return mapper.toResponse(users.getProfile(authentication.getName()));
    }

    @PatchMapping
    @Operation(summary = "Update current profile", description = "Updates email and/or sentiment preference.")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    public UserResponse updateProfile(Authentication authentication,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        User updated = users.updateProfile(authentication.getName(), request);
        return mapper.toResponse(updated);
    }

    @PatchMapping("/password")
    @Operation(summary = "Change password", description = "Verifies the current password, stores a new BCrypt hash, and revokes refresh tokens.")
    @ApiResponse(responseCode = "204", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Access token or current password is invalid")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        users.changePassword(authentication.getName(),
                request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
