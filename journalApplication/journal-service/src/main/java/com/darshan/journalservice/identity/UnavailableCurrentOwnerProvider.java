package com.darshan.journalservice.identity;

import org.springframework.stereotype.Component;

@Component
public class UnavailableCurrentOwnerProvider implements CurrentOwnerProvider {
    @Override
    public Long requireOwnerId() {
        throw new AuthenticationRequiredException();
    }
}
