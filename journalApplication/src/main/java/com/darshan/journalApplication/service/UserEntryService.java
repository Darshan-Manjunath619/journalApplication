package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import com.darshan.journalApplication.repository.UserEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Slf4j
public class UserEntryService {

    @Autowired
    private UserEntryRepository userEntryRepository;

    private  static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveEntry(User user){
        userEntryRepository.save(user);
    }

    public void saveNewUser(User user){
        try
        {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Arrays.asList("USER" , "ADMIN"));
        userEntryRepository.save(user);
        }
        catch (Exception e) {
              log.info("Error while using info");
              log.error("Error occured for {}: "  ,user.getUserName() ,e);
              log.warn("Error while using warn");
        }

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
