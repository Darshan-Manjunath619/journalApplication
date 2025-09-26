package com.darshan.journalApplication.service;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserEntryServiceTests {

    @Autowired
    private UserEntryService userEntryService;

    @Autowired
    private UserEntryRepository userEntryRepository;

    @ParameterizedTest
    @CsvSource({
            "ram",
            "ab"
    })
    public void findByUserName(String name){
        assertNotNull(userEntryRepository.findByUserName(name));
    }

    @ParameterizedTest
    @CsvSource({
            "1,1,2",
            "3,3,6",
            "5,6,11"
    })
    public void test(int a , int b , int expected){
        assertEquals(expected , a + b);
    }

}
