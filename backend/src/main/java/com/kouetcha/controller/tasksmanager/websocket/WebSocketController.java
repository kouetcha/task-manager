package com.kouetcha.controller.tasksmanager.websocket;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.tasksmanager.websocket.ChatMessage;
import com.kouetcha.dto.tasksmanager.websocket.PresenceEvent;
import com.kouetcha.security.service.UserDetailsImpl;
import com.kouetcha.service.tasksmanager.websocket.WebSocketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "${client.url}")
@Tag(name = "WS")
@Slf4j
public class WebSocketController {

    private final WebSocketService wsService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message,
                            Principal principal) {
        message.setSenderName(principal.getName());
        message.setTimestamp(Instant.now());
        wsService.sendChatMessage(message.getReceiverId(), message);
    }

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        String email = extractEmail(sha);
        if (email == null) return;

        wsService.broadcastPresence(PresenceEvent.builder()
                .fullName(email)
                .online(true)
                .timestamp(Instant.now())
                .build());

        log.info("✅ Connexion WS : {}", email);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        String email = extractEmail(sha);
        if (email == null) return;

        wsService.broadcastPresence(PresenceEvent.builder()
                .fullName(email)
                .online(false)
                .timestamp(Instant.now())
                .build());

        log.info("🔴 Déconnexion WS : {}", email);
    }


    private String extractEmail(StompHeaderAccessor sha) {
        Principal principal = sha.getUser();
        if (principal == null) return null;

        if (principal instanceof Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof String email)          return email;
            if (p instanceof UserDetails ud)        return ud.getUsername();
            if (p instanceof UserDetailsImpl ud)    return ud.getUsername();
        }


        return principal.getName();
    }
}