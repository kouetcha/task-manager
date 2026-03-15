package com.kouetcha.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;


import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(chain = true)
public abstract class BaseVS implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_creation", updatable = false)
    private Date dateCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_modification")
    private Date dateModification;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    @JsonIgnore
    private String createdBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    @JsonIgnore
    private String modifiedBy;

    @Column(name = "optlock", nullable = false)
    @Version
    @JsonIgnore
    private long version;

    @PrePersist
    protected void onCreate() {
       // this.createdBy= UserContext.getCurrentUser();
        if (this.dateCreation == null) {
            this.dateCreation = new Date();
        }
        this.dateModification = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = new Date();
        //this.modifiedBy=UserContext.getCurrentUser();
    }
}