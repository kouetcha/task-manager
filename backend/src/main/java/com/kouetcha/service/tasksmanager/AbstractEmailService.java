package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.base.BaseEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Transactional

public abstract class AbstractEmailService<E extends BaseEmail<T>, T>
        implements EmailService<E, T> {

    protected final JpaRepository<E, Long> emailRepository;
    protected final JpaRepository<T, Long> parentRepository;

    protected AbstractEmailService(JpaRepository<E, Long> emailRepository,
                                   JpaRepository<T, Long> parentRepository) {
        this.emailRepository = emailRepository;
        this.parentRepository = parentRepository;
    }

    protected abstract E createEmailEntity();



    protected abstract void setParent(E email, T parent);

    protected abstract List<E> findByParentEntity(T parent);

    @Override
    public E addEmail(Long parentId, String emailValue) {

        T parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        E email = createEmailEntity();
        email.setEmail(emailValue);
        setParent(email, parent);

        return emailRepository.save(email);
    }

    @Override
    public void removeEmail(Long emailId) {
        emailRepository.deleteById(emailId);
    }

    @Override
    public E activateEmail(Long emailId) {
        E email = emailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        email.setActive(true);
        return emailRepository.save(email);
    }
    @Override
    public E updateEmail(Long emailId, String emailValue) {
        E email = emailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        email.setEmail(emailValue);
        return emailRepository.save(email);
    }

    @Override
    public E deactivateEmail(Long emailId) {
        E email = emailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        email.setActive(false);
        return emailRepository.save(email);
    }

    @Override
    public List<E> findByParent(Long parentId) {
        T parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        return findByParentEntity(parent);
    }
}
