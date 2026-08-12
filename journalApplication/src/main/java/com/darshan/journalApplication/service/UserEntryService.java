package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import com.darshan.journalApplication.repository.UserEntryRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.darshan.journalApplication.shared.error.ConflictException;
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

    public UserEntryService(UserEntryRepository userEntryRepository,
                            PasswordEncoder passwordEncoder) {
        this.userEntryRepository = userEntryRepository;
        this.passwordEncoder = passwordEncoder;
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

}
