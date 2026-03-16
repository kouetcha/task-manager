package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.kouetcha.dto.tasksmanager.JsonViews;
import com.kouetcha.model.base.BaseCommentaire;
import com.kouetcha.model.base.BaseVS;
import com.kouetcha.model.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tasksmanager_commentaire_fichier")
@Accessors(chain = true)
public class FichierCommentaire extends BaseVS implements Serializable {

    @Column(nullable = false)
    private String nomFichier;

    @Column(nullable = false)
    private String cheminFichier;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_commentaire_id")
    private CommentaireActivite commentaireActivite;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_commentaire_id")
    private CommentaireProjet commentaireProjet;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_commentaire_id")
    private CommentaireTache commentaireTache;

    private Date dateUpload = new Date();
    @JsonView(JsonViews.Api.class)
    @JsonIgnore
    @JsonProperty("parentId")
    public Long getParentId(){
        if(this.commentaireActivite!=null){
          return this.commentaireActivite.getId();
        }
        if(this.commentaireProjet!=null){
            return this.commentaireProjet.getId();
        }
        if(this.commentaireTache!=null){
            return this.commentaireTache.getId();
        }
        return null;
    }
    @JsonProperty("type")
    public String getType(){
        if(this.cheminFichier==null) {
            return "";
        }
        return DocumentType.getTypeByExtension(this.nomFichier).getValue();
    }
    public String getUrl() {
        return buildUrl("/download/" + this.cheminFichier);
    }

    public String   getCallbackurl() {
        return buildUrl("/onlyoffice-save/" + this.getId());
    }

    private String buildUrl(String suffix) {

        if (this.cheminFichier == null || this.cheminFichier.isBlank()) {
            return null;
        }

        try {

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null) {
                return null;
            }

            String baseUrl = recupererUrl(attrs);

            return baseUrl + suffix;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private String recupererControlleur() {

        if (this.commentaireActivite != null) {
            return "fichiers-commentaire-activite/" + this.commentaireActivite.getId();
        }

        if (this.commentaireTache != null) {
            return "fichiers-commentaire-tache/" + this.commentaireTache.getId();
        }

        if (this.commentaireProjet != null) {
            return "fichiers-commentaire-projet/" + this.commentaireProjet.getId();
        }

        return "";
    }

       private String recupererUrl(ServletRequestAttributes attrs) {

            HttpServletRequest request = attrs.getRequest();

            // Log temporaire pour voir tous les headers
            java.util.Collections.list(request.getHeaderNames())
                .forEach(name -> System.out.println("HEADER: " + name + " = " + request.getHeader(name)));

            String scheme = request.getHeader("X-Forwarded-Proto") != null
                ? request.getHeader("X-Forwarded-Proto")
                : request.getScheme();

            String host = request.getHeader("X-Forwarded-Host") != null
                ? request.getHeader("X-Forwarded-Host")
                : request.getServerName();

            return scheme + "://" + host + "/tasksmanager/" + recupererControlleur();
}
}
