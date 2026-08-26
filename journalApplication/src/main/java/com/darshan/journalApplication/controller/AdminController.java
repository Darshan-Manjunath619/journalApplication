package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final UserEntryService userEntryService;
    private final UserMapper userMapper;

    public AdminController(UserEntryService userEntryService, UserMapper userMapper) {
        this.userEntryService = userEntryService;
        this.userMapper = userMapper;
    }

    @GetMapping
    @Operation(summary = "List users", description = "Returns safe user DTOs; requires the ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Users returned")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "ADMIN role required")
    public List<UserResponse> getAllUsers() {
        return userEntryService.getAll().stream().map(userMapper::toResponse).toList();
    }
}

