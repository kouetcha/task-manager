package com.kouetcha.config.auditor;

import com.kouetcha.model.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<Utilisateur> utilisateurConnecte = new ThreadLocal<Utilisateur>();

    public static void setCurrentUser(String username) {
        currentUser.set(username);
    }

    public static String getCurrentUser() {
        return currentUser.get();
    }
    public static Utilisateur getUtilisaeurConnecte(){
        return  utilisateurConnecte.get();
    }
    public static void setUtilisaeurConnecte(Utilisateur utilisateur){
       utilisateurConnecte.set(utilisateur);
    }

    public static void clearCurrentUser() {
        currentUser.remove();
    }
}