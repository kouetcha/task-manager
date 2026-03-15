package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Tache;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TacheService {
    Tache create(BaseEntityGestionDto dto, Long activiteId);

    Tache update(Long id, BaseEntityGestionDto dto);

    Tache updateDesignation(Long id, @Valid TexteDto dto);

    Tache updateDescription(Long id, @Valid TexteDto dto);

    Tache updateDateDebut(Long id, @Valid DateDto dto);

    Tache updateDateFin(Long id, @Valid DateDto dto);

    void delete(Long id);

    void addFichier(Long tacheId, FichierDTO fichierDTO);

    void deleteFichier(Long tacheId, Long fichierId);

    Page<Tache> findByEmail(String email, Pageable pageable);

    List<BaseEntityDto>findDtoActiveByEmail(String email);

    Page<BaseEntityDto>findDtoActiveByEmail(String email, Pageable pageable);

    long countDtoActiveByEmail(String email);

    Tache findById(Long id);

    List<Tache> findByActiviteId(Long activiteId);

    List<Tache> findByActiviteIdAndEmail(Long activiteId, String email);
}
