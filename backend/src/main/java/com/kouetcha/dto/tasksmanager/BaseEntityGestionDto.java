package com.kouetcha.dto.tasksmanager;

import com.kouetcha.model.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
public class BaseEntityGestionDto {
    @NotNull
    private String designation;
    private String description;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date dateDebut;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date dateFin;
    private Status status=Status.EN_ATTENTE;
    @NotNull
    private Long createurId;
    private List<String>emails;


    private Long parentId;

    private List<FichierDTO> fichiers;
}