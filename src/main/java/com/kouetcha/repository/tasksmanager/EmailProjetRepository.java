package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.model.tasksmanager.Projet;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface EmailProjetRepository extends JpaRepository<EmailProjet,Long> {
    List<EmailProjet> findByProjet(Projet parent);



    Optional<EmailProjet> findByEmailAndProjetId(String emailValue, Long id);
}
