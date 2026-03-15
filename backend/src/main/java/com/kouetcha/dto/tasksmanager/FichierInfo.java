package com.kouetcha.dto.tasksmanager;

import com.kouetcha.model.tasksmanager.FichierCommentaire;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FichierInfo {

    private Long id;
    private String nomFichier;
    private String url;
    private String cheminFichier;
    private Date dateUpload;
    private String type;

    public FichierInfo(FichierCommentaire fichierCommentaire){
        this.id=fichierCommentaire.getId();
        this.cheminFichier=fichierCommentaire.getCheminFichier();
        this.url=fichierCommentaire.getUrl();
        this.type=fichierCommentaire.getType();
        this.dateUpload=fichierCommentaire.getDateUpload();
        this.nomFichier=fichierCommentaire.getNomFichier();
    }
}
