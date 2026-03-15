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
        name = "tasksmanager_projet_email",
        uniqueConstraints = @UniqueConstraint(columnNames = {"projet_id", "email"})
)
@Accessors(chain = true)
public class EmailProjet extends BaseEmail<Projet> implements Serializable {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @JsonIgnore
    @Override
    public Projet getParent() {
        return projet;
    }
    public Long getEntiteId(){
        if(this.projet==null) return null;
        return this.projet.getId();
    }
}