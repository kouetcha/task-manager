package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.websocket.NotificationEvent;
import com.kouetcha.model.enums.Type;
import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.tasksmanager.Tache;
import com.kouetcha.repository.tasksmanager.EmailProjetRepository;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import com.kouetcha.service.tasksmanager.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class EmailProjetServiceImpl
        extends AbstractEmailService<EmailProjet, Projet> {

    private final EmailProjetRepository emailProjetRepository;
    private final ProjetRepository projetRepository;
    private final WebSocketService webSocketService;

    public EmailProjetServiceImpl(EmailProjetRepository emailProjetRepository,
                                  ProjetRepository projetRepository, WebSocketService webSocketService) {
        super(emailProjetRepository, projetRepository);
        this.emailProjetRepository = emailProjetRepository;
        this.projetRepository = projetRepository;
        this.webSocketService = webSocketService;
    }

    @Override
    protected EmailProjet createEmailEntity() {
        return new EmailProjet();
    }

    @Override
    public EmailProjet addEmail(Long parentId, String emailValue) {

        Projet projet = projetRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        EmailProjet email = emailProjetRepository.findByEmailAndProjetId(emailValue,projet.getId()).orElse(new EmailProjet());
        email.setEmail(emailValue);
        setParent(email, projet);

        return emailRepository.save(email);
    }



    @Override
    protected void setParent(EmailProjet email, Projet parent) {
        email.setProjet(parent);
    }

    @Override
    protected List<EmailProjet> findByParentEntity(Projet parent) {
        return emailProjetRepository.findByProjet(parent);
    }
}