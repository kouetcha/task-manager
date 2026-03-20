package com.kouetcha.service.tasksmanager.websocket;

import com.kouetcha.dto.tasksmanager.websocket.ChatMessage;
import com.kouetcha.dto.tasksmanager.websocket.NotificationEvent;
import com.kouetcha.dto.tasksmanager.websocket.PresenceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService{

    private final SimpMessagingTemplate messagingTemplate;



    // ── Notification à un utilisateur précis ─────────────────────
    @Override
    @Async
    public void sendNotification(String email, NotificationEvent event) {
        log.info("sendNotification");
        log.info(String.valueOf(event));
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                event
        );
    }

    // ── Mise à jour live d'un projet (tous les membres) ──────────
    @Override
    @Async
    public void sendProjetUpdate(Long projetId, NotificationEvent event) {
        log.info("sendNotification-I");
        log.info(String.valueOf(event));
        messagingTemplate.convertAndSend(
                "/topic/projets/" + projetId,
                event
        );
    }
    // ── Mise à jour live d'une activité (tous les membres) ──────────
    @Async
    @Override
    public void sendActiviteUpdate(Long activiteId, NotificationEvent event) {
        log.info("sendNotification-I");
        log.info(String.valueOf(event));
        messagingTemplate.convertAndSend(
                "/topic/activites/" + activiteId,
                event
        );
    }

    // ── Mise à jour live d'une tache (tous les membres) ──────────
    @Async
    @Override
    public void sendTacheUpdate(Long tacheId, NotificationEvent event) {
        log.info("sendNotification-I");
        log.info(String.valueOf(event));
        messagingTemplate.convertAndSend(
                "/topic/taches/" + tacheId,
                event
        );
    }


    // ── Message de chat privé ────────────────────────────────────
    @Override
    @Async
    public void sendChatMessage(Long receiverId, ChatMessage message) {
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                message
        );
    }

    // ── Présence broadcast ───────────────────────────────────────
    @Override
    @Async
    public void broadcastPresence(PresenceEvent event) {
        messagingTemplate.convertAndSend("/topic/presence", event);
    }
}