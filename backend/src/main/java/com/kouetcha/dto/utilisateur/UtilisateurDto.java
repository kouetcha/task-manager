package com.kouetcha.dto.utilisateur;

import com.kouetcha.model.enums.RoleProjet;
import com.kouetcha.model.enums.UserCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
public class UtilisateurDto {

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

    private UserCategory category;
    private RoleProjet roleProjet=RoleProjet.NON_DEFINI;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Pattern(
            regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\-]).{8,}$",
            message = "Le mot de passe doit contenir au minimum 8 caractères, "
                    + "au moins une lettre majuscule, une lettre minuscule, "
                    + "un chiffre et un caractère spécial (#?!@$%^&*-)."
    )
    private String motdepasse;
}