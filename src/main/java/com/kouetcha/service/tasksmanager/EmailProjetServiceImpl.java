package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.repository.tasksmanager.EmailProjetRepository;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailProjetServiceImpl
        extends AbstractEmailService<EmailProjet, Projet> {

    private final EmailProjetRepository emailProjetRepository;
    private final ProjetRepository projetRepository;

    public EmailProjetServiceImpl(EmailProjetRepository emailProjetRepository,
                                  ProjetRepository projetRepository) {
        super(emailProjetRepository, projetRepository);
        this.emailProjetRepository = emailProjetRepository;
        this.projetRepository = projetRepository;
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