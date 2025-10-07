package com.darshan.journalApplication.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

@Component
@SpringBootTest
public class EmailServiceTests {

    @Autowired
    private EmailService emailService;

    @Test
    void sendMailTests(){
        emailService.sendMail("darshan.manjunath17@gmail.com","Testing the mail Service From Java","Test for the java service from code");
    }
}

