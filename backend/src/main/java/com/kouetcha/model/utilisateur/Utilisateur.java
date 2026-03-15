package com.kouetcha.model.utilisateur;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kouetcha.model.base.BaseVS;
import com.kouetcha.model.enums.RoleProjet;
import com.kouetcha.model.enums.UserCategory;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "tasksmanager_utilisateur")
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Utilisateur extends BaseVS implements Serializable {
    @Column(unique = true)
    @Pattern(regexp = "^[a-zA-ZàáâãäåçèéêëìíîïñòóôõöùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝ0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Veuillez fournir une adresse e-mail valide.")
    private String email;
    @Pattern(regexp = "^\\+?[0-9\\-\\s]+$", message = "Veuillez fournir un numéro de téléphone valide.")
    private String telephone;
    @JsonIgnore
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "Le mot de passe n'est pas valide. Minimum huit caractères, au moins une lettre majuscule, une lettre")
    private String motdepasse;
    private Date dernierConnexion;
    private String nom;
    private String prenom;
    @ColumnDefault("true")
    @JsonProperty("est_actif")
    private boolean estActif = true;
    private boolean admin ;
    @Enumerated(value = EnumType.STRING)
    private UserCategory category=UserCategory.NORMAL;
    private String profilePicture;
    private Date dateInscription;
    private String profilePictureLink;
    private boolean actif;
    @Enumerated(EnumType.STRING)
    private RoleProjet roleProjet=RoleProjet.NON_DEFINI;
    @JsonProperty("fullName")
    public  String getFullName(){
        return nom+" "+prenom;
    }
    @JsonProperty("profilePictureLink")
    public String getProfilePictureLink() {
        if (this.profilePicture == null || this.profilePicture.isBlank()) {
            return null;
        }

        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null)
                return null;

            HttpServletRequest request = attrs.getRequest();

            String baseUrl = request.getScheme() + "://" +
                    request.getServerName() +
                    ":" + request.getServerPort();

            return baseUrl + "/tasksmanager/utilisateur/image/" + this.profilePicture;

        } catch (Exception e) {
            return null;
        }
    }
    @JsonProperty("dateInscription")
    public Date getDateInscription() {
        return super.getDateCreation();
    }
}