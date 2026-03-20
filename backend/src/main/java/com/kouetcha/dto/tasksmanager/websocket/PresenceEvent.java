package com.kouetcha.dto.tasksmanager.websocket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class PresenceEvent {
    private Long    userId;
    private String  fullName;
    private boolean online;
    private Instant timestamp;
}
