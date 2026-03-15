package com.kouetcha.config.auditor;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(String username) {
        currentUser.set(username);
    }

    public static String getCurrentUser() {
        return currentUser.get();
    }

    public static void clearCurrentUser() {
        currentUser.remove();
    }
}