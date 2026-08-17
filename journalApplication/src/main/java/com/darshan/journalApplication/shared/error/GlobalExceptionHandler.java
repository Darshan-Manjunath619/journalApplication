package com.darshan.journalApplication.shared.error;

import com.darshan.journalApplication.auth.InvalidRefreshTokenException;
import com.darshan.journalApplication.auth.RefreshTokenReuseException;
import com.darshan.journalApplication.auth.UntrustedOriginException;
import com.darshan.journalApplication.user.InvalidCurrentPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import java.net.URI;
import java.util.*;
import com.darshan.journalApplication.shared.web.CorrelationIdFilter;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException exception,
                                            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem(HttpStatus.NOT_FOUND, "Resource not found",
                        exception.getMessage(), request));
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> conflict(ConflictException exception,
                                           HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "Resource conflict",
                        exception.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception,
                                              HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "Request contains invalid fields", request);
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> malformed(HttpMessageNotReadableException exception,
                                             HttpServletRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST,
                "Malformed request", "Request body is not valid JSON", request));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ProblemDetail> unauthorized(BadCredentialsException exception,
                                                HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem(
                HttpStatus.UNAUTHORIZED, "Authentication failed",
                "Invalid username or password", request));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<ProblemDetail> invalidRefreshToken(
            InvalidRefreshTokenException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem(
                HttpStatus.UNAUTHORIZED, "Invalid session",
                "The refresh token is missing, invalid, or expired", request));
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    ResponseEntity<ProblemDetail> refreshTokenReuse(
            RefreshTokenReuseException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem(
                HttpStatus.UNAUTHORIZED, "Session invalidated",
                "Refresh token reuse was detected; sign in again", request));
    }

    @ExceptionHandler(UntrustedOriginException.class)
    ResponseEntity<ProblemDetail> untrustedOrigin(
            UntrustedOriginException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem(
                HttpStatus.FORBIDDEN, "Untrusted origin",
                "This browser origin is not allowed", request));
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    ResponseEntity<ProblemDetail> invalidCurrentPassword(
            InvalidCurrentPasswordException exception, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(problem(
                HttpStatus.BAD_REQUEST, "Password change rejected",
                exception.getMessage(), request));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception,
                                              HttpServletRequest request) {
        log.error("Unexpected request failure", exception);
        return ResponseEntity.internalServerError().body(problem(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred", request));
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail,
                                  HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        Object correlationId = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }
        return problem;
    }
}
