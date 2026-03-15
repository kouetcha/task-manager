package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Activite;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ActiviteService {
    Activite create(BaseEntityGestionDto dto, Long projetId);

    Activite update(Long id, BaseEntityGestionDto dto);

    Activite updateDesignation(Long id, @Valid TexteDto dto);

    Activite updateDescription(Long id, @Valid TexteDto dto);

    Activite updateDateDebut(Long id, @Valid DateDto dto);

    Activite updateDateFin(Long id, @Valid DateDto dto);

    void delete(Long id);

    void addFichier(Long activiteId, FichierDTO fichierDTO);

    void deleteFichier(Long activiteId, Long fichierId);

    Activite findById(Long id);

    List<Activite> findByProjetId(Long projetId);


    List<Activite> findByProjetIdAndEmail(Long projetId, String email);

    Page<Activite> findByEmail(String email, Pageable pageable);

    List<BaseEntityDto>findDtoActiveByEmail(String email);

    Page<BaseEntityDto>findDtoActiveByEmail(String email, Pageable pageable);

    long countDtoActiveByEmail(String email);
}
