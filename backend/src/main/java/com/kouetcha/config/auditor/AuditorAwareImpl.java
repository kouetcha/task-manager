package com.kouetcha.config.auditor;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

//@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    /*@Override
    public Optional<String> getCurrentAuditor() {

        String username = "SYSTEM";
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        }
        return Optional.of(username);
    }*/

    @Override
    public Optional<String> getCurrentAuditor() {
        String username = UserContext.getCurrentUser();

        if (username == null) {
            if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
                UserContext.setCurrentUser(username);
            }
        }

        return Optional.ofNullable(username);
    }
}