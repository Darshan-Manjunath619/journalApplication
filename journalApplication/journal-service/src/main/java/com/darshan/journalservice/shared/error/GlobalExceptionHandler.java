package com.darshan.journalservice.shared.error;

import com.darshan.journalservice.identity.AuthenticationRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationRequiredException.class)
    ResponseEntity<ProblemDetail> unauthorized(AuthenticationRequiredException exception,
                                                HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "Authentication required",
                exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidQueryParameterException.class)
    ResponseEntity<ProblemDetail> invalidQuery(InvalidQueryParameterException exception,
                                                HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid query parameter",
                exception.getMessage(), request);
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ProblemDetail> invalidQueryFormat(Exception exception,
                                                      HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid query parameter",
                "One or more query parameters are invalid", request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException exception,
                                            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "Resource not found",
                exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> conflict(ConflictException exception,
                                           HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Resource conflict",
                exception.getMessage(), request);
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
        return response(HttpStatus.BAD_REQUEST, "Malformed request",
                "Request body is not valid JSON", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception,
                                              HttpServletRequest request) {
        log.error("Unexpected request failure", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ProblemDetail> response(HttpStatus status, String title,
                                                    String detail, HttpServletRequest request) {
        return ResponseEntity.status(status).body(problem(status, title, detail, request));
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail,
                                  HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
