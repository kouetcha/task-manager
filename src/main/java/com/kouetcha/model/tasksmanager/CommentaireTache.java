package com.kouetcha.model.tasksmanager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kouetcha.model.base.BaseCommentaire;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasksmanager_tache_commentaire")
@Accessors(chain = true)
public class CommentaireTache extends BaseCommentaire<Tache> implements Serializable {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;
    @OneToMany(mappedBy = "commentaireTache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichierCommentaire> fichiers = new ArrayList<>();
    @JsonIgnore
    @Override
    public Tache getParent() {
        return tache;
    }
}