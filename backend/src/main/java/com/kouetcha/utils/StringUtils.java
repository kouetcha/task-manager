package com.kouetcha.utils;


import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    public static String normaliserTexte(String texte) {
        if (texte == null) return "";
        String texteSansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return texteSansAccents.toLowerCase().trim();
    }
    public static String safe(String input) {
        return input == null ? "" : input.replace("\"", "\\\"");
    }

    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // Remplace les tirets et underscores par un espace temporairement pour traiter chaque segment
        String[] words = str.replace("-", " - ").replace("_", " _ ").replace("("," ( ").split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                if (word.equals("-") || word.equals("_")|| word.equals("(")) {
                    capitalized.append(word);  // Maintenir le tiret ou l'underscore
                } else {
                    capitalized.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase());
                }
                capitalized.append(" ");
            }
        }

        // Trim the trailing space
        return capitalized.toString().trim().replace(" - ", "-").replace(" _ ", "_").replace(" ( "," (");
    }
    public static String capitalizeWordsSimple(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // Remplace les tirets et underscores par un espace temporairement pour traiter chaque segment
        String[] words = str.replace("-", " - ").replace("_", " _ ").split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                if (word.equals("-") || word.equals("_")|| word.equals("(")) {
                    capitalized.append(word);  // Maintenir le tiret ou l'underscore
                } else {
                    capitalized.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase());
                }
                capitalized.append(" ");
            }
        }

        // Trim the trailing space
        return capitalized.toString().trim().replace(" - ", "-").replace(" _ ", "_");
    }


    public static String toUpperCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }
    public static String toLowerCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
    }
    public static String formatLongWithLeadingZeros(Long number, int n) {
        if (number == null) {
            return null;
        }
        // %0nd signifie que l'on ajoute des zéros à gauche jusqu'à ce que la chaîne ait une longueur de n
        return String.format("%0" + n + "d", number);
    }
    public static String obtenirPreposition(String mot) {
        // Vérifier si le mot commence par une voyelle ou un h muet
        if (mot != null && (mot.matches("^[aeiouhAEIOUH].*"))) {
            return " d'"; // Utiliser "d'" si le mot commence par une voyelle ou un h muet
        } else {
            return " de "; // Utiliser "de" sinon
        }
    }
    public static String sanitizeFilename(String originalFilename) {
        // Normaliser pour éliminer les caractères spéciaux (accents, etc.)
        String normalized = Normalizer.normalize(originalFilename, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String sanitizedFilename = pattern.matcher(normalized).replaceAll("");

        // Remplacer les caractères non alphabétiques et non numériques par des underscores
        sanitizedFilename = sanitizedFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        // Limiter la longueur à 255 caractères (par exemple, pour les systèmes de fichiers qui ont une limite)
        if (sanitizedFilename.length() > 255) {
            sanitizedFilename = sanitizedFilename.substring(0, 255);
        }

        // Vous pouvez aussi ajouter un préfixe ou suffixe unique pour éviter les conflits de noms
        return sanitizedFilename;
    }




}