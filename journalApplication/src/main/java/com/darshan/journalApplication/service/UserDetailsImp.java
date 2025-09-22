package com.darshan.journalApplication.service;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsImp implements UserDetailsService {

    @Autowired
    private UserEntryRepository userEntryRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userEntryRepository.findByUserName(username);
        if(user != null) {
            return org.springframework.security.core.userdetails.User.builder().
                    username(user.getUserName()).
                    password(user.getPassword()).
                    roles(user.getRole().toArray(new String[0])).
                    build();
        }
        throw new UsernameNotFoundException("User Not found with username " + username);
    }
}
