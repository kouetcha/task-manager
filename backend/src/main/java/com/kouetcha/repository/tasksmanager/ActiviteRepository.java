package com.kouetcha.repository.tasksmanager;

import com.kouetcha.dto.tasksmanager.BaseEntityDto;
import com.kouetcha.dto.tasksmanager.DashboardItemDto;
import com.kouetcha.dto.tasksmanager.StatsProjection;
import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    List<Activite> findByProjet(Projet projet);
    List<Activite> findByCreateur(Utilisateur createur);

    boolean existsByDesignationAndCreateurIdAndProjetId(@NotNull String designation, @NotNull Long createurId, Long id);

    List<Activite> findByProjetId(Long projetId);


    Page<Activite> findByEmailsEmailIgnoreCase(String email, Pageable pageable);

    Page<Activite> findDistinctByEmailsEmailIgnoreCaseOrCreateurEmail(String email,String email1, Pageable pageable);
    @Query(value = """
            SELECT DISTINCT new com.kouetcha.dto.tasksmanager.BaseEntityDto(
                a.id,
                a.designation,
                a.description,
                a.dateDebut,
                a.dateFin,
                a.status,
                a.createur.id,
                a.createur.nom,
                a.createur.prenom,
                a.createur.email,
                a.projet.id
            )
            FROM Activite a
            LEFT JOIN a.emails e
            WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(a.createur.email) = LOWER(:email))
            AND e.active=true
            AND a.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
        """,
            countQuery = """
            SELECT COUNT(DISTINCT a)
            FROM Activite a
            LEFT JOIN a.emails e
            WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(a.createur.email) = LOWER(:email))
            AND e.active=true
            AND a.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
        """)
    Page<BaseEntityDto> findActiveByEmail(@Param("email") String email, Pageable pageable);

    @Query("""
        SELECT DISTINCT new com.kouetcha.dto.tasksmanager.BaseEntityDto(
            a.id,
            a.designation,
            a.description,
            a.dateDebut,
            a.dateFin,
            a.status,
            a.createur.id,
            a.createur.nom,
            a.createur.prenom,
            a.createur.email,
            a.projet.id
        )
        FROM Activite a
        LEFT JOIN a.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(a.createur.email) = LOWER(:email))
        AND e.active=true
        AND a.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    List<BaseEntityDto> findActiveByEmail(@Param("email") String email);

    @Query("""
        SELECT COUNT(DISTINCT a)
        FROM Activite a
        LEFT JOIN a.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(a.createur.email) = LOWER(:email))
        AND e.active=true
        AND a.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    long countActiveByEmail(@Param("email") String email);


    Page<Activite> findDistinctByEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrCreateurEmail(String email, String email1, Pageable pageable);


    List<Activite> findByProjetIdAndEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrProjetIdAndCreateurEmail(Long projetId, String email, Long projetId1, String email1);


    // ActiviteRepository
    @Query(value = """
    SELECT 
        COUNT(a.id)                                                AS total,
        SUM(CASE WHEN a.status = 'EN_COURS'   THEN 1 ELSE 0 END) AS enCours,
        SUM(CASE WHEN a.status = 'TERMINE'    THEN 1 ELSE 0 END) AS termines,
        SUM(CASE WHEN a.status = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS enAttente,
        SUM(CASE WHEN a.status = 'ANNULE'     THEN 1 ELSE 0 END) AS annules
    FROM tasksmanager_activite a
    WHERE a.createur_id = :userId
    """, nativeQuery = true)
    StatsProjection getStatsActivites(@Param("userId") Long userId);

    @Query(value = """
    SELECT 
        a.id,
        a.designation,
        a.status,
        a.date_fin          AS dateFin,
        a.date_modification AS dateModification,
        'ACTIVITE'          AS type,
        a.projet_id         AS projetId,
        a.id                AS activiteId
    FROM tasksmanager_activite a
    WHERE a.createur_id = :userId
      AND a.date_fin < CURRENT_DATE
      AND a.status != 'TERMINE'
      AND a.status != 'ANNULE'
    ORDER BY a.date_fin ASC
    """, nativeQuery = true)
    List<Object[]> findActivitesEnRetard(@Param("userId") Long userId);
}
