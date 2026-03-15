package com.kouetcha.dto.tasksmanager;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FichierDTO {
    @NotNull
    private String nomFichier;
    @NotNull
    private MultipartFile fichier;

    public FichierDTO(MultipartFile fichier, String nomFichier){
        this.fichier = fichier;
        this.nomFichier = nomFichier;
    }
}