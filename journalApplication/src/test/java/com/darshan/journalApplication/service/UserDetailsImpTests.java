package com.darshan.journalApplication.service;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import static org.mockito.Mockito.when;

public class UserDetailsImpTests {

    @InjectMocks
    private UserDetailsImp userDetailsImp;


    @Mock
    private UserEntryRepository userEntryRepository;

    @BeforeEach
    public  void setUp(){
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void loadUserByUsernameTests(){
        when(userEntryRepository.findByUserName(ArgumentMatchers.anyString())).thenReturn(User.builder().userName("ram").password("dhvfdhfgved").role(new ArrayList<>()).build());
        UserDetails user = userDetailsImp.loadUserByUsername("ram");
        Assertions.assertNotNull(user);
    }
}
