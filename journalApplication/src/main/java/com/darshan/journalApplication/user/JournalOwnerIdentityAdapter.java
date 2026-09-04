package com.darshan.journalApplication.user;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.journal.port.JournalOwnerIdentityPort;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JournalOwnerIdentityAdapter implements JournalOwnerIdentityPort {
    private final UserEntryRepository users;

    public JournalOwnerIdentityAdapter(UserEntryRepository users) {
        this.users = users;
    }

    @Override
    public Long requireOwnerId(String userName) {
        User user = users.findByUserName(userName);
        if (user == null) throw new ResourceNotFoundException("User profile was not found");
        return user.getId();
    }
}
