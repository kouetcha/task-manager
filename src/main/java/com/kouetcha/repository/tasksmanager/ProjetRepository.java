package com.kouetcha.repository.tasksmanager;

import com.kouetcha.dto.tasksmanager.BaseEntityDto;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.utilisateur.Utilisateur;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByCreateur(Utilisateur createur);

    boolean existsByDesignationAndCreateurId(@NotNull String designation, @NotNull Long createurId);

    List<Projet> findByCreateurId(Long userId);

    Page<Projet> findDistinctByEmailsEmailIgnoreCaseOrCreateurEmail(String email,String email1, Pageable pageable);

    @Query(value = """
                SELECT DISTINCT new com.kouetcha.dto.tasksmanager.BaseEntityDto(
                    p.id,
                    p.designation,
                    p.description,
                    p.dateDebut,
                    p.dateFin,
                    p.status,
                    p.createur.id,
                    p.createur.nom,
                    p.createur.prenom,
                    p.createur.email,
                    null
                )
                FROM Projet p
                LEFT JOIN p.emails e
                WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(p.createur.email) = LOWER(:email))
                AND e.active=true
                AND p.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
            """,
            countQuery = """
        SELECT COUNT(DISTINCT p)
        FROM Projet p
        LEFT JOIN p.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(p.createur.email) = LOWER(:email))
        AND e.active=true
        AND p.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    Page<BaseEntityDto> findActiveByEmail(@Param("email") String email, Pageable pageable);

    @Query("""
        SELECT DISTINCT new com.kouetcha.dto.tasksmanager.BaseEntityDto(
            p.id,
            p.designation,
            p.description,
            p.dateDebut,
            p.dateFin,
            p.status,
            p.createur.id,
            p.createur.nom,
            p.createur.prenom,
            p.createur.email,
            null
        )
        FROM Projet p
        LEFT JOIN p.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(p.createur.email) = LOWER(:email))
        AND e.active=true
        AND p.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    List<BaseEntityDto> findActiveByEmail(@Param("email") String email);

    @Query("""
        SELECT COUNT(DISTINCT p)
        FROM Projet p
        LEFT JOIN p.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(p.createur.email) = LOWER(:email))
        AND e.active=true
        AND p.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    long countActiveByEmail(@Param("email") String email);


    Page<Projet> findDistinctByEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrCreateurEmail(String email, String email1, Pageable pageable);
}
