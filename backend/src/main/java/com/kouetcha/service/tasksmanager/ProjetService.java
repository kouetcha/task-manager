package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.enums.Status;
import com.kouetcha.model.tasksmanager.Projet;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjetService {
    Projet create(@Valid BaseEntityGestionDto dto);

    Projet update(Long id, @Valid BaseEntityGestionDto dto);

    Projet updateDesignation(Long id, @Valid TexteDto dto);

    Projet updateDescription(Long id, @Valid TexteDto dto);

    Projet updateDateDebut(Long id, @Valid DateDto dto);

    Projet updateDateFin(Long id, @Valid DateDto dto);

    void delete(Long id);

    void deleteByCreateur(Long userId);

    Projet changeStatus(Long id, Status status);

    void addFichier(Long projetId, FichierDTO fichierDTO);

    void deleteFichier(Long projetId, Long fichierId);

    Page<Projet> findByEmail(String email, Pageable pageable);

    Projet findById(Long id);

    List<Projet> findByCreateurId(Long userId);

    List<BaseEntityDto>findDtoActiveByEmail(String email);

    Page<BaseEntityDto>findDtoActiveByEmail(String email, Pageable pageable);

    long countDtoActiveByEmail(String email);

    List<Projet> findAll();

    boolean existsById(Long id);

    long count();
}
