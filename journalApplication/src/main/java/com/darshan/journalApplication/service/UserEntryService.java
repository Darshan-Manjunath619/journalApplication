package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import com.darshan.journalApplication.repository.UserEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class UserEntryService {

    @Autowired
    private UserEntryRepository userEntryRepository;

    private  static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveEntry(User user){
        userEntryRepository.save(user);
    }

    public void saveNewUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Arrays.asList("USER"));
        userEntryRepository.save(user);
    }

    public List<User> getAll(){
        return userEntryRepository.findAll();
    }

    public Optional<User> getById(ObjectId id){
        return userEntryRepository.findById(id);
    }

    public void deleteById(ObjectId id){
         userEntryRepository.deleteById(id);
    }

    public User findByUserName(String userName){
        return userEntryRepository.findByUserName(userName);
    }

}
