package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.darshan.journalApplication.shared.error.ConflictException;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import com.darshan.journalApplication.auth.RefreshTokenService;
import com.darshan.journalApplication.user.InvalidCurrentPasswordException;
import com.darshan.journalApplication.user.dto.UpdateProfileRequest;
import java.util.Locale;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UserEntryService {

    private final UserEntryRepository userEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UserEntryService(UserEntryRepository userEntryRepository,
                            PasswordEncoder passwordEncoder,
                            RefreshTokenService refreshTokenService) {
        this.userEntryRepository = userEntryRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public void saveEntry(User user){
        userEntryRepository.save(user);
    }

    @Transactional
    public User saveNewUser(User user){
        String userName = user.getUserName().trim().toLowerCase(Locale.ROOT);
        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userEntryRepository.existsByUserName(userName)) {
            throw new ConflictException("Username already exists");
        }
        if (userEntryRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(List.of("USER"));
        return userEntryRepository.save(user);
    }

    public List<User> getAll(){
        return userEntryRepository.findAll();
    }

    public Optional<User> getById(Long id){
        return userEntryRepository.findById(id);
    }

    public void deleteById(Long id){
         userEntryRepository.deleteById(id);
    }

    public User findByUserName(String userName){
        return userEntryRepository.findByUserName(userName);
    }

    @Transactional(readOnly = true)
    public User getProfile(String userName) {
        return requireUser(userName);
    }

    @Transactional
    public User updateProfile(String userName, UpdateProfileRequest request) {
        User user = requireUser(userName);
        if (request.email() != null) {
            String email = request.email().trim().toLowerCase(Locale.ROOT);
            if (!email.equals(user.getEmail()) &&
                    userEntryRepository.existsByEmailAndIdNot(email, user.getId())) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(email);
        }
        if (request.sentimentAnalysis() != null) {
            user.setSentimentAnalysis(request.sentimentAnalysis());
        }
        return userEntryRepository.save(user);
    }

    @Transactional
    public void changePassword(String userName, String currentPassword, String newPassword) {
        User user = requireUser(userName);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ConflictException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userEntryRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
    }

    private User requireUser(String userName) {
        User user = userEntryRepository.findByUserName(userName);
        if (user == null) {
            throw new ResourceNotFoundException("User profile was not found");
        }
        return user;
    }

}
