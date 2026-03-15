package com.kouetcha.dto.tasksmanager;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CommentaireUpdateDto {
    @NotNull
    private Long id;
     @NotNull
    private String contenu;

}
