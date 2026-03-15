package com.kouetcha.model.tasksmanager;

import com.kouetcha.model.base.BaseEntityGestion;

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
@Table(name = "tasksmanager_projet",uniqueConstraints = @UniqueConstraint(columnNames = {"createur_id","designation"}))
@Accessors(chain = true)
public class Projet extends BaseEntityGestion implements Serializable {

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailProjet> emails = new ArrayList<>();
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichierEntityGestion> fichiers = new ArrayList<>();
}
