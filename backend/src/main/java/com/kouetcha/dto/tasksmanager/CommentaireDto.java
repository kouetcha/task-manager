package com.kouetcha.dto.tasksmanager;

import lombok.Data;

import java.util.List;
@Data
public class CommentaireDto {
    private Long id;
    private String contenu;
    private Long auteurId;


    private Long parentId;


    // Fichiers attachés
    private List<FichierDTO> fichiers;
}
