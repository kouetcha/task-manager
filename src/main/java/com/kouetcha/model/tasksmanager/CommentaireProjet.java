package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kouetcha.dto.tasksmanager.FichierInfo;
import com.kouetcha.model.base.BaseCommentaire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasksmanager_projet_commentaire")
@Accessors(chain = true)
public class CommentaireProjet extends BaseCommentaire<Projet> implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projet_id", nullable = false)
    @JsonIgnore
    private Projet projet;

    @OneToMany(mappedBy = "commentaireProjet", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)

    private List<FichierCommentaire> fichiers = new ArrayList<>();

    @Override
    @JsonIgnore
    public Projet getParent() {
        return projet;
    }

   /* @JsonProperty("fichiers")
    public List<FichierInfo> getFichiersInfo() {
        return fichiers.stream()
                .map(FichierInfo::new)
                .toList();
    }*/
}