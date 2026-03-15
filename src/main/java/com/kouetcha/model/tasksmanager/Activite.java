package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kouetcha.model.base.BaseEntityGestion;
import com.kouetcha.model.base.BaseVS;
import com.kouetcha.model.enums.Status;
import com.kouetcha.model.utilisateur.Utilisateur;
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
@Table(name = "tasksmanager_activite",uniqueConstraints = @UniqueConstraint(columnNames = {"createur_id","designation","projet_id"}))
@Accessors(chain = true)
public class Activite extends BaseEntityGestion implements Serializable {

    @ManyToOne
    @JsonIgnoreProperties(value = {"emails","fichiers","createur"})
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailActivite> emails = new ArrayList<>();
    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichierEntityGestion> fichiers = new ArrayList<>();
}
