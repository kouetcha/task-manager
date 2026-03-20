package com.kouetcha.dto.tasksmanager;

import com.kouetcha.model.enums.Type;
import com.kouetcha.model.enums.TypeEvent;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String message;
    private TypeEvent event;
    private Type type;
    private Long parentId;
    @NotNull
    private Utilisateur receveur;
    private Utilisateur emetteur;
}
