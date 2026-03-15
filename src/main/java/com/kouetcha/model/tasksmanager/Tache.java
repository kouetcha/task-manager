package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kouetcha.model.base.BaseEntityGestion;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "tasksmanager_tache",uniqueConstraints = @UniqueConstraint(columnNames = {"createur_id","designation","activite_id"}))
@Accessors(chain = true)
public class Tache extends BaseEntityGestion implements Serializable {

    @ManyToOne
    @JsonIgnoreProperties(value = {"emails","fichiers","createur"})
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailTache> emails = new ArrayList<>();
    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichierEntityGestion> fichiers = new ArrayList<>();
}
