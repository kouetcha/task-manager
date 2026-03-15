package com.kouetcha.dto.tasksmanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireResponseDto implements Serializable {

    private Long id;
    private String contenu;
    private Date date;
    private List<FichierInfoDto> fichiers = new ArrayList<>();
    private AuteurDto auteur;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FichierInfoDto implements Serializable {
        private Long id;
        private String nomFichier;
        private String cheminFichier;
        private String type;
        private String url;
        private String callbackurl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuteurDto implements Serializable {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String profilePicture;
    }
}