package com.kouetcha.dto.tasksmanager;

import com.kouetcha.model.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BaseEntityDto {

    private Long id;
    private String designation;
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private Status status;
    private Long createurId;
    private String createurNom;
    private String createurPrenom;
    private String createurEmail;
    private Long parentId; // projetId pour Activite, null pour Projet


    public BaseEntityDto(Long id, String designation, String description,
                         Date dateDebut, Date dateFin, Status status,
                         Long createurId, String createurNom,String createurPrenom,String createurEmail,Long parentId) {
        this.id = id;
        this.designation = designation;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.status = status;
        this.createurId = createurId;
        this.createurNom=createurNom;
        this.createurPrenom=createurPrenom;
        this.createurEmail=createurEmail;
        this.parentId = parentId;
    }
}