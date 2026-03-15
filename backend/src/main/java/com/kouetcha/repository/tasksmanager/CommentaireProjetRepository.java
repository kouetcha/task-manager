package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.CommentaireProjet;
import com.kouetcha.model.tasksmanager.Projet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireProjetRepository extends JpaRepository<CommentaireProjet, Long> {
    @EntityGraph(attributePaths = {"fichiers", "auteur"})
    List<CommentaireProjet> findByProjetOrderByDateAsc(Projet projet);
}
