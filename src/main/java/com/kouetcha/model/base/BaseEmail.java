package com.kouetcha.model.base;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;


@Getter
@Setter
@MappedSuperclass
@Accessors(chain = true)
public abstract class BaseEmail<T> extends BaseVS implements Serializable {

    @Column(nullable = false)
    @Pattern(
            regexp = "^[a-zA-ZàáâãäåçèéêëìíîïñòóôõöùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝ0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Veuillez fournir une adresse e-mail valide. "
    )
    private String email;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private boolean active = true;
    @JsonIgnore
    public abstract T getParent();
}