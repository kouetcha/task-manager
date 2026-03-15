package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireActiviteRepository extends JpaRepository<CommentaireActivite, Long> {
    List<CommentaireActivite> findByActiviteOrderByDateAsc(Activite activite);
}
