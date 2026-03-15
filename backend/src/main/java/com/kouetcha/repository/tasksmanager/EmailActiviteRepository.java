package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.EmailActivite;

import com.kouetcha.model.tasksmanager.EmailProjet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailActiviteRepository extends JpaRepository<EmailActivite,Long> {
    List<EmailActivite> findByActivite(Activite parent);

    Optional<EmailActivite> findByEmail(String emailValue);
}
