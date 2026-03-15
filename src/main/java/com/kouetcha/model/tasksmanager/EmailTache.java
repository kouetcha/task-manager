package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kouetcha.model.base.BaseEmail;
import com.kouetcha.model.base.BaseVS;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(
        name = "tasksmanager_tache_email",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tache_id", "email"})
)
@Accessors(chain = true)
public class EmailTache extends BaseEmail<Tache> implements Serializable {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;
    @JsonIgnore
    @Override
    public Tache getParent() {
        return tache;
    }
}
