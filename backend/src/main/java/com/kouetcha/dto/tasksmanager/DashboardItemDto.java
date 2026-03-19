package com.kouetcha.dto.tasksmanager;
import com.kouetcha.model.enums.Type;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class DashboardItemDto {

    private Long id;
    private String designation;
    private String status;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateModification;
    private Type type;
    private Long projetId;
    private Long activiteId;

    public DashboardItemDto(Long id, String designation, String status,
                            LocalDateTime dateDebut, LocalDateTime dateFin, LocalDateTime dateModification,
                            Type type, Long projetId, Long activiteId) {
        this.id = id;
        this.designation = designation;
        this.status = status;
        this.dateDebut=dateDebut;
        this.dateFin = dateFin;
        this.dateModification = dateModification;
        this.type = type;
        this.projetId = projetId;
        this.activiteId = activiteId;
    }
    public DashboardItemDto(){

    }

}