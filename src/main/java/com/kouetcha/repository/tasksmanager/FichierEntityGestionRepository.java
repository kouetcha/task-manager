package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.tasksmanager.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FichierEntityGestionRepository extends JpaRepository<FichierEntityGestion,Long> {
    List<FichierEntityGestion> findByTache(Tache parent);

    List<FichierEntityGestion> findByActivite(Activite parent);

    List<FichierEntityGestion> findByProjet(Projet parent);



    List<FichierEntityGestion> findByProjetId(Long parentId);
}
