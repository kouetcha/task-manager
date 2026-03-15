package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.base.BaseCommentaire;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.model.tasksmanager.CommentaireProjet;
import com.kouetcha.model.tasksmanager.CommentaireTache;
import com.kouetcha.model.tasksmanager.FichierCommentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichierCommentaireRepository extends JpaRepository<FichierCommentaire, Long> {

    List<FichierCommentaire> findByCommentaireActivite(CommentaireActivite parent);

    List<FichierCommentaire> findByCommentaireActiviteId(Long parentId);

    List<FichierCommentaire> findByCommentaireProjetId(Long parentId);

    List<FichierCommentaire> findByCommentaireProjet(CommentaireProjet parent);

    List<FichierCommentaire> findByCommentaireTache(CommentaireTache parent);

    List<FichierCommentaire> findByCommentaireTacheId(Long parentId);
}
