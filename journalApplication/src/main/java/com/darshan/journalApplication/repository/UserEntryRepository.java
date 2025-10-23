package com.darshan.journalApplication.repository;

import com.darshan.journalApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntryRepository extends JpaRepository<User, Long> {

    User findByUserName(String userName);

    void deleteByUserName(String userName);
}
