package com.darshan.journalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JournalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JournalServiceApplication.class, args);
    }
}
