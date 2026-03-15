package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.EmailActivite;
import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
import com.kouetcha.repository.tasksmanager.EmailActiviteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailActiviteServiceImpl
        extends AbstractEmailService<EmailActivite, Activite> {

    private final EmailActiviteRepository emailRepository;
    private final ActiviteRepository activiteRepository;

    public EmailActiviteServiceImpl(EmailActiviteRepository emailRepository,
                                    ActiviteRepository activiteRepository) {
        super(emailRepository, activiteRepository);
        this.emailRepository = emailRepository;
        this.activiteRepository=activiteRepository;
    }

    @Override
    public EmailActivite addEmail(Long parentId, String emailValue) {

        Activite activite = activiteRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        EmailActivite email = emailRepository.findByEmail(emailValue).orElse(new EmailActivite());
        email.setEmail(emailValue);
        setParent(email, activite);

        return emailRepository.save(email);
    }

    @Override
    protected EmailActivite createEmailEntity() {
        return new EmailActivite();
    }

    @Override
    protected void setParent(EmailActivite email, Activite parent) {
        email.setActivite(parent);
    }

    @Override
    protected List<EmailActivite> findByParentEntity(Activite parent) {
        return emailRepository.findByActivite(parent);
    }
}
