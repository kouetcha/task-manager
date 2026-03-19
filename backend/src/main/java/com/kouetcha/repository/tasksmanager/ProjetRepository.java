package com.kouetcha.repository.tasksmanager;

import com.kouetcha.dto.tasksmanager.BaseEntityDto;
import com.kouetcha.dto.tasksmanager.DashboardItemDto;
import com.kouetcha.dto.tasksmanager.StatsProjection;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.utilisateur.Utilisateur;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query(value = """
    SELECT 
        COUNT(p.id)                                                AS total,
        SUM(CASE WHEN p.status = 'EN_COURS'   THEN 1 ELSE 0 END) AS enCours,
        SUM(CASE WHEN p.status = 'TERMINE'    THEN 1 ELSE 0 END) AS termines,
        SUM(CASE WHEN p.status = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS enAttente,
        SUM(CASE WHEN p.status = 'ANNULE'     THEN 1 ELSE 0 END) AS annules
    FROM tasksmanager_projet p
    WHERE p.createur_id = :userId
    """, nativeQuery = true)
    StatsProjection getStatsProjets(@Param("userId") Long userId);

    @Query(value = """
    SELECT 
        p.id,
        p.designation,
        p.status,
        p.date_fin          AS dateFin,
        p.date_modification AS dateModification,
        'PROJET'            AS type,
        p.id                AS projetId,
        NULL                AS activiteId
    FROM tasksmanager_projet p
    WHERE p.createur_id = :userId
      AND p.date_fin < CURRENT_DATE
      AND p.status != 'TERMINE'
      AND p.status != 'ANNULE'
    ORDER BY p.date_fin ASC
    """, nativeQuery = true)
    List<Object[]> findProjetsEnRetard(@Param("userId") Long userId);

    @Query(value = """
    SELECT
        id, designation, status,
        date_debut        AS dateDebut,
        date_fin          AS dateFin,
        date_modification AS dateModification,
        type,
        projetId,
        activiteId
    FROM (
        SELECT
            p.id,
            p.designation,
            p.status,
            p.date_debut,
            p.date_fin,
            p.date_modification,
            'PROJET' AS type,
            p.id     AS projetId,
            NULL     AS activiteId
        FROM tasksmanager_projet p
        WHERE p.createur_id = :userId
        UNION
        SELECT
            p.id,
            p.designation,
            p.status,
            p.date_debut,
            p.date_fin,
            p.date_modification,
            'PROJET' AS type,
            p.id     AS projetId,
            NULL     AS activiteId
        FROM tasksmanager_projet p
        INNER JOIN tasksmanager_projet_email pe
               ON pe.projet_id = p.id
              AND pe.email      = :userEmail
              AND pe.active     = true

        UNION

        SELECT
            a.id,
            a.designation,
            a.status,
            a.date_debut,
            a.date_fin,
            a.date_modification,
            'ACTIVITE'  AS type,
            a.projet_id AS projetId,
            a.id        AS activiteId
        FROM tasksmanager_activite a
        WHERE a.createur_id = :userId
        UNION
        SELECT
            a.id,
            a.designation,
            a.status,
            a.date_debut,
            a.date_fin,
            a.date_modification,
            'ACTIVITE'  AS type,
            a.projet_id AS projetId,
            a.id        AS activiteId
        FROM tasksmanager_activite a
        INNER JOIN tasksmanager_activite_email ae
               ON ae.activite_id = a.id
              AND ae.email        = :userEmail
              AND ae.active       = true

        UNION

        SELECT
            t.id,
            t.designation,
            t.status,
            t.date_debut,
            t.date_fin,
            t.date_modification,
            'TACHE'       AS type,
            NULL          AS projetId,
            t.activite_id AS activiteId
        FROM tasksmanager_tache t
        WHERE t.createur_id = :userId
        UNION
        SELECT
            t.id,
            t.designation,
            t.status,
            t.date_debut,
            t.date_fin,
            t.date_modification,
            'TACHE'       AS type,
            NULL          AS projetId,
            t.activite_id AS activiteId
        FROM tasksmanager_tache t
        INNER JOIN tasksmanager_tache_email te
               ON te.tache_id = t.id
              AND te.email     = :userEmail
              AND te.active    = true

    ) AS dashboard
    ORDER BY date_modification DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopDashboard(
            @Param("userId")    Long   userId,
            @Param("userEmail") String userEmail,
            @Param("limit")     int    limit
    );

    @Query(value = """
    SELECT
        id, designation, status,
        date_debut  AS dateDebut,
        date_fin    AS dateFin,
        type,
        projetId,
        activiteId
    FROM (
        SELECT
            p.id,
            p.designation,
            p.status,
            p.date_debut,
            p.date_fin,
            'PROJET' AS type,
            p.id     AS projetId,
            NULL     AS activiteId
        FROM tasksmanager_projet p
        WHERE p.createur_id = :userId
          AND p.date_fin   >= :start
          AND p.date_debut <= :end
        UNION
        SELECT
            p.id,
            p.designation,
            p.status,
            p.date_debut,
            p.date_fin,
            'PROJET' AS type,
            p.id     AS projetId,
            NULL     AS activiteId
        FROM tasksmanager_projet p
        INNER JOIN tasksmanager_projet_email pe
               ON pe.projet_id = p.id
              AND pe.email      = :userEmail
              AND pe.active     = true
        WHERE p.date_fin   >= :start
          AND p.date_debut <= :end

        UNION

        SELECT
            a.id,
            a.designation,
            a.status,
            a.date_debut,
            a.date_fin,
            'ACTIVITE'  AS type,
            a.projet_id AS projetId,
            a.id        AS activiteId
        FROM tasksmanager_activite a
        WHERE a.createur_id = :userId
          AND a.date_fin   >= :start
          AND a.date_debut <= :end
        UNION
        SELECT
            a.id,
            a.designation,
            a.status,
            a.date_debut,
            a.date_fin,
            'ACTIVITE'  AS type,
            a.projet_id AS projetId,
            a.id        AS activiteId
        FROM tasksmanager_activite a
        INNER JOIN tasksmanager_activite_email ae
               ON ae.activite_id = a.id
              AND ae.email        = :userEmail
              AND ae.active       = true
        WHERE a.date_fin   >= :start
          AND a.date_debut <= :end

        UNION

        SELECT
            t.id,
            t.designation,
            t.status,
            t.date_debut,
            t.date_fin,
            'TACHE'       AS type,
            NULL          AS projetId,
            t.activite_id AS activiteId
        FROM tasksmanager_tache t
        WHERE t.createur_id = :userId
          AND t.date_fin   >= :start
          AND t.date_debut <= :end
        UNION
        SELECT
            t.id,
            t.designation,
            t.status,
            t.date_debut,
            t.date_fin,
            'TACHE'       AS type,
            NULL          AS projetId,
            t.activite_id AS activiteId
        FROM tasksmanager_tache t
        INNER JOIN tasksmanager_tache_email te
               ON te.tache_id = t.id
              AND te.email     = :userEmail
              AND te.active    = true
        WHERE t.date_fin   >= :start
          AND t.date_debut <= :end

    ) AS calendar
    ORDER BY date_debut ASC
    """, nativeQuery = true)
    List<Object[]> findCalendarEvents(
            @Param("userId")    Long      userId,
            @Param("userEmail") String    userEmail,
            @Param("start")     LocalDate start,
            @Param("end")       LocalDate end
    );
}
