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
        name = "tasksmanager_activite_email",
        uniqueConstraints = @UniqueConstraint(columnNames = {"activite_id", "email"})
)
@Accessors(chain = true)
public class EmailActivite extends BaseEmail<Activite> implements Serializable {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;
    @JsonIgnore
    @Override
    public Activite getParent() {
        return activite;
    }
}
