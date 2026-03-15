package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.model.tasksmanager.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailTacheRepository extends JpaRepository<EmailTache,Long> {
    List<EmailTache> findByTache(Tache parent);

    Optional<EmailTache> findByEmail(String emailValue);

    Optional<EmailTache> findByEmailAndTacheId(String emailValue, Long parentId);
}
