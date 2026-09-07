package com.darshan.journalservice.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentOwnerProvider implements CurrentOwnerProvider {
    @Override
    public Long requireOwnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof AccessTokenIdentity identity)) {
            throw new AuthenticationRequiredException();
        }
        return identity.userId();
    }
}
