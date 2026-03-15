package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.CommentaireTache;
import com.kouetcha.model.tasksmanager.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireTacheRepository extends JpaRepository<CommentaireTache, Long> {
    List<CommentaireTache> findByTacheOrderByDateAsc(Tache tache);
}
