package com.kouetcha.dto.tasksmanager.websocket;

import com.kouetcha.model.enums.Type;
import com.kouetcha.model.enums.TypeEvent;
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
    private TypeEvent type;
    private String  message;
    private Long    entiteId;
    private Type    entiteType;
    private String  emetteur;
    private Instant timestamp;
}