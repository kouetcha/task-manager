package com.kouetcha.repository.tasksmanager;

import com.kouetcha.dto.tasksmanager.BaseEntityDto;
import com.kouetcha.dto.tasksmanager.DashboardItemDto;
import com.kouetcha.dto.tasksmanager.StatsProjection;
import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.Tache;
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
public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByActivite(Activite activite);
    List<Tache> findByCreateur(Utilisateur createur);

    List<Tache> findByActiviteId(Long activiteId);

    boolean existsByDesignationAndCreateurIdAndActiviteId(@NotNull String designation, @NotNull Long createurId, Long id);

    // Retourne les tâches actives
    @Query("""
        SELECT DISTINCT new com.kouetcha.dto.tasksmanager.BaseEntityDto(
            t.id,
            t.designation,
            t.description,
            t.dateDebut,
            t.dateFin,
            t.status,
            t.createur.id,
            t.createur.nom,
            t.createur.prenom,
            t.createur.email,
            t.activite.id
        )
        FROM Tache t
        LEFT JOIN t.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(t.createur.email) = LOWER(:email))
        AND e.active=true
        AND t.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    List<BaseEntityDto> findActiveByEmail(@Param("email") String email);
    @Query(value = """
        SELECT DISTINCT new com.kouetcha.dto.tasksmanager.BaseEntityDto(
            t.id,
            t.designation,
            t.description,
            t.dateDebut,
            t.dateFin,
            t.status,
            t.createur.id,
            t.createur.nom,
            t.createur.prenom,
            t.createur.email,
            t.activite.id
        )
        FROM Tache t
        LEFT JOIN t.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(t.createur.email) = LOWER(:email))
        AND e.active=true
        AND t.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """,
            countQuery = """
            SELECT COUNT(DISTINCT t)
            FROM Tache t
            LEFT JOIN t.emails e
            WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(t.createur.email) = LOWER(:email))
            AND e.active=true
            AND t.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
        """)
    Page<BaseEntityDto> findActiveByEmail(@Param("email") String email,Pageable pageable);


    // Count des tâches actives
    @Query("""
        SELECT COUNT(DISTINCT t)
        FROM Tache t
        LEFT JOIN t.emails e
        WHERE (LOWER(e.email) = LOWER(:email) OR LOWER(t.createur.email) = LOWER(:email))
        AND e.active=true
        AND t.status IN (com.kouetcha.model.enums.Status.EN_ATTENTE, com.kouetcha.model.enums.Status.EN_COURS)
    """)
    long countActiveByEmail(@Param("email") String email);

    Page<Tache> findDistinctByEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrCreateurEmail(String email, String email1, Pageable pageable);


    List<Tache> findByActiviteIdAndEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrActiviteIdAndCreateurEmail(Long activiteId, String email, Long activiteId1, String email1);


    @Query(value = """
    SELECT 
        COUNT(t.id)                                                AS total,
        SUM(CASE WHEN t.status = 'EN_COURS'   THEN 1 ELSE 0 END) AS enCours,
        SUM(CASE WHEN t.status = 'TERMINE'    THEN 1 ELSE 0 END) AS termines,
        SUM(CASE WHEN t.status = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS enAttente,
        SUM(CASE WHEN t.status = 'ANNULE'     THEN 1 ELSE 0 END) AS annules
    FROM tasksmanager_tache t
    WHERE t.createur_id = :userId
    """, nativeQuery = true)
    StatsProjection getStatsTaches(@Param("userId") Long userId);

    @Query(value = """
    SELECT 
        t.id,
        t.designation,
        t.status,
        t.date_debut        As dateDebut,
        t.date_fin          AS dateFin,
        t.date_modification AS dateModification,
        'TACHE'             AS type,
         NULL         AS projetId,
        t.activite_id       AS activiteId
    FROM tasksmanager_tache t
    WHERE t.createur_id = :userId
      AND t.date_fin < CURRENT_DATE
      AND t.status != 'TERMINE'
      AND t.status != 'ANNULE'
    ORDER BY t.date_fin ASC
    """, nativeQuery = true)
    List<Object[]> findTachesEnRetard(@Param("userId") Long userId);
}
