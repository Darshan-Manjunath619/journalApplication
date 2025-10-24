package com.darshan.journalApplication.repos;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserImpTests {

    @MockBean
    private UserEntryRepository userEntryRepository;


    @Test
    void findUserBySa() {
        when(userEntryRepository.findByEmailIsNotNullAndSentimentAnalysisTrue()).thenReturn(List.of(new User()));
        assertFalse(userEntryRepository.findByEmailIsNotNullAndSentimentAnalysisTrue().isEmpty());
    }
}
