package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.auth.*;
import com.darshan.journalApplication.auth.dto.*;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.user.dto.UserResponse;
import com.darshan.journalApplication.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {
    private final UserEntryService users;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokens;
    private final RefreshCookieService refreshCookies;
    private final TrustedOriginService trustedOrigins;

    public AuthController(UserEntryService users, UserMapper userMapper,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          RefreshTokenService refreshTokens, RefreshCookieService refreshCookies,
                          TrustedOriginService trustedOrigins) {
        this.users = users;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokens = refreshTokens;
        this.refreshCookies = refreshCookies;
        this.trustedOrigins = trustedOrigins;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a user", description = "Creates a USER account; roles cannot be supplied by the client.")
    @ApiResponse(responseCode = "201", description = "User registered")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User saved = users.saveNewUser(userMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Returns an access token and sets a rotating HttpOnly refresh-token cookie.")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = normalize(request.userName());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password()));
        User user = users.findByUserName(username);
        IssuedRefreshToken refresh = refreshTokens.issue(user);
        return withRefreshCookie(username, refresh.token());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates the HttpOnly refresh cookie and returns a new access token.")
    @ApiResponse(responseCode = "200", description = "Token rotated")
    @ApiResponse(responseCode = "401", description = "Refresh token is missing, invalid, expired, revoked, or reused")
    @ApiResponse(responseCode = "403", description = "Origin is not trusted")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request, @RequestHeader(value = "Origin", required = false) String origin) {
        trustedOrigins.verify(origin);
        String presented = refreshCookies.read(request);
        if (presented == null || presented.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        RotatedRefreshToken rotated = refreshTokens.rotate(presented);
        return withRefreshCookie(rotated.userName(), rotated.token());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the presented refresh token and clears its cookie.")
    @ApiResponse(responseCode = "204", description = "Logged out")
    @ApiResponse(responseCode = "403", description = "Origin is not trusted")
    public ResponseEntity<Void> logout(
            HttpServletRequest request, @RequestHeader(value = "Origin", required = false) String origin) {
        trustedOrigins.verify(origin);
        refreshTokens.revokePresented(refreshCookies.read(request));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.clear())
                .build();
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(String username, String refreshToken) {
        String accessToken = jwtUtil.generateToken(username);
        AuthResponse response = new AuthResponse(
                accessToken, "Bearer", jwtUtil.getAccessTokenExpiresInSeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.create(refreshToken))
                .body(response);
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
