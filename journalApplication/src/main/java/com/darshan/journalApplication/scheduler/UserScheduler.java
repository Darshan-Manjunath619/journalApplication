package com.darshan.journalApplication.scheduler;

import com.darshan.journalApplication.journal.domain.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.journal.persistence.JournalEntryRepository;
import com.darshan.journalApplication.service.EmailService;
import com.darshan.journalApplication.service.SentimentAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "features.sentiment-scheduler", havingValue = "true")
public class UserScheduler {

    private final SentimentAnalysis sentimentAnalysis;
    private final EmailService emailService;
    private final UserEntryRepository userEntryRepository;
    private final JournalEntryRepository journalEntryRepository;

    public UserScheduler(SentimentAnalysis sentimentAnalysis, EmailService emailService,
                         UserEntryRepository userEntryRepository,
                         JournalEntryRepository journalEntryRepository) {
        this.sentimentAnalysis = sentimentAnalysis;
        this.emailService = emailService;
        this.userEntryRepository = userEntryRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUserAndSentimentAnalysis(){
        List<User> users = userEntryRepository.findByEmailIsNotNullAndSentimentAnalysisTrue();
        for(User user : users){
            List<JournalEntry> journalEntries =
                    journalEntryRepository.findAllByOwnerIdOrderByDateDesc(user.getId());
            List<String> collect = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getContent()).collect(Collectors.toList());
            String join = String.join(" " + collect);
            String sentiment = sentimentAnalysis.getSentiment(join);
            emailService.sendMail(user.getEmail(),"Sentiment Analyziz for 7 days " ,sentiment);
        }

    }

}
