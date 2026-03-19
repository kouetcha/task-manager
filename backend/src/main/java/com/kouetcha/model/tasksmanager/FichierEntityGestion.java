package com.kouetcha.model.tasksmanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "tasksmanager_entity_gestion_fichier")
@Accessors(chain = true)
public class FichierEntityGestion extends BaseVS implements Serializable {

    @Column(nullable = false)
    private String nomFichier;

    @Column(nullable = false)
    private String cheminFichier;

    private Date dateUpload = new Date();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id")
    private Activite activite;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id")
    private Tache tache;

    public String getType(){
        if(this.cheminFichier==null) {
            return "";
        }
        return DocumentType.getTypeByExtension(this.cheminFichier).getValue();
    }

    public String getUrl() {
        return buildUrl("/download/" + this.cheminFichier);
    }

    public String getCallbackurl() {
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

        if (this.activite != null) {
            return "fichiers-activite/" + this.activite.getId();
        }

        if (this.tache != null) {
            return "fichiers-tache/" + this.tache.getId();
        }

        if (this.projet != null) {
            return "fichiers-projet/" + this.projet.getId();
        }

        return "";
    }

    private String recupererUrl(ServletRequestAttributes attrs) {

        HttpServletRequest request = attrs.getRequest();

        String scheme = request.getHeader("X-Forwarded-Proto") != null
                ? request.getHeader("X-Forwarded-Proto")
                : request.getScheme();

        String xForwardedHost = request.getHeader("X-Forwarded-Host");
        String host;
        String portStr = "";

        if (xForwardedHost != null && !xForwardedHost.isEmpty()) {
            // ✅ $http_host peut contenir host:port ex: 192.168.190.159:4400
            if (xForwardedHost.contains(":")) {
                String[] parts = xForwardedHost.split(":", 2);
                host = parts[0];
                portStr = ":" + parts[1];
            } else {
                host = xForwardedHost;
                String forwardedPort = request.getHeader("X-Forwarded-Port");
                if (forwardedPort != null && !forwardedPort.isEmpty()) {
                    int port = Integer.parseInt(forwardedPort);
                    if (!isDefaultPort(scheme, port)) {
                        portStr = ":" + port;
                    }
                }
            }
        } else {
            host = request.getServerName();
            int serverPort = request.getServerPort();
            if (!isDefaultPort(scheme, serverPort)) {
                portStr = ":" + serverPort;
            }
        }

        // ✅ Remplacer par URL interne Docker si OnlyOffice local
        String url = scheme + "://" + host + portStr + "/tasksmanager/" + recupererControlleur();


        return url;
    }

private boolean isDefaultPort(String scheme, int port) {
    return ("http".equals(scheme) && port == 80)
        || ("https".equals(scheme) && port == 443);
}
}
