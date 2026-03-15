package com.kouetcha.model.base;

import com.kouetcha.model.enums.Status;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@Accessors(chain = true)
public abstract class BaseEntityGestion extends BaseVS implements Serializable {

    private String designation;

    @Column(columnDefinition = "Text")
    private String description;

    private Date dateDebut;
    private Date dateFin;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "createur_id", nullable = false)
    private Utilisateur createur;
}
