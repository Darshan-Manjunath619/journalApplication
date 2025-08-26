package com.darshan.journalApplication.repository;

import com.darshan.journalApplication.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserEntryRepository extends MongoRepository<User, ObjectId> {
    User findByUserName(String userName);



}
