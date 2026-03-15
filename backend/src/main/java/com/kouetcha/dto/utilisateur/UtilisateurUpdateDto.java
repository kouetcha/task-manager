package com.kouetcha.dto.utilisateur;

import com.kouetcha.model.enums.RoleProjet;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class UtilisateurUpdateDto {

    @NotBlank(message = "L'adresse e-mail est obligatoire.")
    @Email(message = "Veuillez fournir une adresse e-mail valide.")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    @Pattern(
            regexp = "^\\+?[0-9\\-\\s]+$",
            message = "Veuillez fournir un numéro de téléphone valide."
    )
    private String telephone;

    @NotBlank(message = "Le nom est obligatoire.")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire.")
    private String prenom;
    private RoleProjet roleProjet=RoleProjet.NON_DEFINI;


}