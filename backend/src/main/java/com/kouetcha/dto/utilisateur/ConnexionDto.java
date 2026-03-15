package com.kouetcha.dto.utilisateur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConnexionDto {
    @NotBlank(message = "L'adresse e-mail est obligatoire.")
    @Email(message = "Veuillez fournir une adresse e-mail valide.")
    private String email;
    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Pattern(
            regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\-]).{8,}$",
            message = "Le mot de passe doit contenir au minimum 8 caractères, "
                    + "au moins une lettre majuscule, une lettre minuscule, "
                    + "un chiffre et un caractère spécial (#?!@$%^&*-).")

    private String motdepasse;
}