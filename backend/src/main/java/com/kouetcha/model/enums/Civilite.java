package com.kouetcha.model.enums;

public enum Civilite {

    MONSIEUR("MONSIEUR"),
    MADAME("MADAME"),
    MADEMOISELLE("MADEMOISELLE"),
    DOCTEUR("DOCTEUR"),
    PROFESSEUR("PROFESSEUR");

    private final String name;

    Civilite(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}