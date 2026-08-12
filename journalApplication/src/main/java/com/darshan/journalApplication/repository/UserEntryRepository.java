package com.darshan.journalApplication.repository;

import com.darshan.journalApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEntryRepository extends JpaRepository<User, Long> {

    User findByUserName(String userName);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    void deleteByUserName(String userName);

    List<User> findByEmailIsNotNullAndSentimentAnalysisTrue();

}
