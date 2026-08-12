package com.darshan.journalApplication.user;

import com.darshan.journalApplication.auth.dto.RegisterRequest;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.user.dto.UserResponse;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class UserMapper {
    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setUserName(request.userName());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setSentimentAnalysis(request.sentimentAnalysis());
        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUserName(), user.getEmail(),
                user.isSentimentAnalysis(), new ArrayList<>(user.getRole()));
    }
}
