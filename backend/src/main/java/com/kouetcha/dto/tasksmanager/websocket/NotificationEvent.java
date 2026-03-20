package com.kouetcha.dto.tasksmanager.websocket;

import com.kouetcha.model.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String  type;       // "TACHE_ASSIGNEE", "PROJET_MODIFIE", etc.
    private String  message;
    private Long    entiteId;
    private Type    entiteType; // "PROJET", "ACTIVITE", "TACHE"
    private String  emetteur;
    private Instant timestamp;
}