package com.kouetcha.model.enums;

public enum RoleProjet {

    CHEF_PROJET("Chef de projet"),
    MEMBRE_EQUIPE("Membre d'équipe"),
    COLLABORATEUR("Collaborateur externe"),
    OBSERVATEUR("Observateur"),
    NON_DEFINI("Non défini");

    private final String label;

    RoleProjet(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}