package com.kouetcha.model.base;

import com.kouetcha.model.tasksmanager.FichierCommentaire;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@MappedSuperclass
@Accessors(chain = true)
public abstract class BaseCommentaire<T> extends BaseVS implements Serializable {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    private Date date = new Date();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    public abstract T getParent();
}
