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
@Table(name = "tasksmanager_activite_commentaire")
@Accessors(chain = true)
public class CommentaireActivite extends BaseCommentaire<Activite> implements Serializable {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;
    @OneToMany(mappedBy = "commentaireActivite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichierCommentaire> fichiers = new ArrayList<>();
    @JsonIgnore
    @Override
    public Activite getParent() {
        return activite;
    }
}
