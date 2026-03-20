package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kouetcha.model.base.BaseVS;
import com.kouetcha.model.enums.Type;
import com.kouetcha.model.enums.TypeEvent;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tasksmanager_notification")
@Accessors(chain = true)
public class Notification extends BaseVS {
    @Column(columnDefinition = "Text",nullable = false)
    private String message;
    private TypeEvent event;
    private Type type;
    private Long id;
    private Long parentId;
    private boolean seen;
    private Date date=new Date();
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "recepteur_id", nullable = false)
    private Utilisateur recepteur;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "emetteur_id")
    private Utilisateur emetteur;
}
