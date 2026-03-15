package com.kouetcha.model.enums;

import java.time.DayOfWeek;

public enum JourSemaine {
    LUNDI("Lundi"),
    MARDI("Mardi"),
    MERCREDI("Mercredi"),
    JEUDI("Jeudi"),
    VENDREDI("Vendredi"),
    SAMEDI("Samedi"),
    DIMANCHE("Dimanche");

    private final String label;

    JourSemaine(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Retourne le DayOfWeek java.time correspondant
    public DayOfWeek toDayOfWeek() {
        return switch (this) {
            case LUNDI -> DayOfWeek.MONDAY;
            case MARDI -> DayOfWeek.TUESDAY;
            case MERCREDI -> DayOfWeek.WEDNESDAY;
            case JEUDI -> DayOfWeek.THURSDAY;
            case VENDREDI -> DayOfWeek.FRIDAY;
            case SAMEDI -> DayOfWeek.SATURDAY;
            case DIMANCHE -> DayOfWeek.SUNDAY;
        };
    }

    // Conversion inverse: DayOfWeek -> JourSemaine
    public static JourSemaine fromDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> LUNDI;
            case TUESDAY -> MARDI;
            case WEDNESDAY -> MERCREDI;
            case THURSDAY -> JEUDI;
            case FRIDAY -> VENDREDI;
            case SATURDAY -> SAMEDI;
            case SUNDAY -> DIMANCHE;
        };
    }
}