package com.darshan.journalApplication.scheduler;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserImp;
import com.darshan.journalApplication.service.EmailService;
import com.darshan.journalApplication.service.SentimentAnalysis;
import com.darshan.journalApplication.service.UserDetailsImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserScheduler {

    @Autowired
    private SentimentAnalysis sentimentAnalysis;

    @Autowired
    private UserImp userImp;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUserAndSentimentAnalysis(){
        List<User> users = userImp.getAllUsersSa();
        for(User user : users){
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<String> collect = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getContent()).collect(Collectors.toList());
            String join = String.join(" " + collect);
            String sentiment = sentimentAnalysis.getSentiment(join);
            emailService.sendMail(user.getEmail(),"Sentiment Analyziz for 7 days " ,sentiment);
        }

    }

}
