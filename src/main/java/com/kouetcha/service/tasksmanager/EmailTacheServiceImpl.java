package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.model.tasksmanager.Tache;
import com.kouetcha.model.tasksmanager.Tache;
import com.kouetcha.repository.tasksmanager.EmailTacheRepository;
import com.kouetcha.repository.tasksmanager.TacheRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailTacheServiceImpl
        extends AbstractEmailService<EmailTache, Tache> {

    private final EmailTacheRepository emailTacheRepository;
    private final TacheRepository tacheRepository;

    public EmailTacheServiceImpl(EmailTacheRepository emailTacheRepository,
                                 TacheRepository tacheRepository) {
        super(emailTacheRepository, tacheRepository);
        this.emailTacheRepository = emailTacheRepository;
        this.tacheRepository=tacheRepository;
    }

    @Override
    public EmailTache addEmail(Long parentId, String emailValue) {

        Tache tache = tacheRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Tache introuvable"));

        EmailTache email = emailTacheRepository.findByEmailAndTacheId(emailValue,parentId).orElse(new EmailTache());
        email.setEmail(emailValue);
        setParent(email, tache);

        return emailRepository.save(email);
    }


    @Override
    protected EmailTache createEmailEntity() {
        return new EmailTache();
    }

    @Override
    protected void setParent(EmailTache email, Tache parent) {
        email.setTache(parent);
    }

    @Override
    protected List<EmailTache> findByParentEntity(Tache parent) {
        return emailTacheRepository.findByTache(parent);
    }
}
