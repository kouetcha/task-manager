package com.kouetcha.config.utils;

public class MontantEnLettres {
    private static final String[] units = { "", "", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix",
            "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf" };

    private static final String[] tens = { "", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante-dix",
            "quatre-vingt", "quatre-vingt-dix" };

    public static String convertirMontantEnLettres(double montant) {
        if (montant == 0) {
            return "zéro";
        }
        return convertirPartieEntiere((long) montant) + " francs CFA et " + convertirPartieDecimale(montant);
    }

    private static String convertirPartieEntiere(long nombre) {
        if (nombre < 0) {
            return "moins " + convertirPartieEntiere(-nombre);
        }

        if (nombre < 20) {
            return units[(int) nombre];
        }

        if (nombre < 100) {
            return tens[(int) nombre / 10] + ((nombre % 10 != 0) ? " " : "") + units[(int) nombre % 10];
        }

        if (nombre < 1000) {
            return units[(int) nombre / 100] + " cent" + ((nombre % 100 != 0) ? " " : "") + convertirPartieEntiere(nombre % 100);
        }

        if (nombre < 1000000) {
            return convertirPartieEntiere(nombre / 1000) + " mille" + ((nombre % 1000 != 0) ? " " : "") + convertirPartieEntiere(nombre % 1000);
        }

        if (nombre < 1000000000) {
            return convertirPartieEntiere(nombre / 1000000) + " million" + ((nombre % 1000000 != 0) ? " " : "") + convertirPartieEntiere(nombre % 1000000);
        }

        return convertirPartieEntiere(nombre / 1000000000) + " milliard" + ((nombre % 1000000000 != 0) ? " " : "") + convertirPartieEntiere(nombre % 1000000000);
    }

    private static String convertirPartieDecimale(double nombre) {
        long partieDecimale = Math.round((nombre - Math.floor(nombre)) * 100);
        if (partieDecimale == 0) {
            return "zéro centimes";
        }
        if (partieDecimale == 1) {
            return "un centime";
        }
        return convertirPartieEntiere(partieDecimale) + " centimes";
    }
}