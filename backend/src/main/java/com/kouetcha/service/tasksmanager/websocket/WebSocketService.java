package com.kouetcha.service.tasksmanager.websocket;

import com.kouetcha.dto.tasksmanager.websocket.ChatMessage;
import com.kouetcha.dto.tasksmanager.websocket.NotificationEvent;
import com.kouetcha.dto.tasksmanager.websocket.PresenceEvent;
import org.springframework.scheduling.annotation.Async;

public interface WebSocketService {
    // ── Notification à un utilisateur précis ─────────────────────
    void sendNotification(String email, NotificationEvent event);

    // ── Mise à jour live d'un projet (tous les membres) ──────────
    void sendProjetUpdate(Long projetId, NotificationEvent event);

    // ── Mise à jour live d'une activité (tous les membres) ──────────
    @Async
    void sendActiviteUpdate(Long activiteId, NotificationEvent event);

    // ── Mise à jour live d'une tache (tous les membres) ──────────
    @Async
    void sendTacheUpdate(Long tacheId, NotificationEvent event);

    // ── Message de chat privé ────────────────────────────────────
    void sendChatMessage(Long receiverId, ChatMessage message);

    // ── Présence broadcast ───────────────────────────────────────
    void broadcastPresence(PresenceEvent event);
}
