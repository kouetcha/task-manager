package com.kouetcha.dto.tasksmanager.websocket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ChatMessage {
    private Long    senderId;
    private String  senderName;
    private Long    receiverId;   // null si message de groupe
    private Long    projetId;     // null si message privé
    private String  content;
    private Instant timestamp;
}
