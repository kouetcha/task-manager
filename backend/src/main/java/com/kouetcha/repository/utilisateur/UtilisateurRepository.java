package com.kouetcha.repository.utilisateur;

import com.kouetcha.model.utilisateur.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {
    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findByEmailIn(List<String> emails);
}
