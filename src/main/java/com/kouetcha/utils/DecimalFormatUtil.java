package com.kouetcha.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


public class DecimalFormatUtil {
    private DecimalFormatUtil(){

    }


    public static String decimalFormat(double montant) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(' ');  // Utilisation de l'espace comme séparateur de milliers pour la France
        symbols.setDecimalSeparator(',');   // Utilisation de la virgule comme séparateur décimal pour la France

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##", symbols);

        return decimalFormat.format(montant);
    }
    public static String decimalFormatEntier(double montant) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(' '); // Espace comme séparateur de milliers

        DecimalFormat decimalFormat = new DecimalFormat("#,##0", symbols); // Pas de décimales

        return decimalFormat.format(montant);
    }



}