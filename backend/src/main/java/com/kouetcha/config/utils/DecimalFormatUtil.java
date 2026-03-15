package com.kouetcha.config.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalFormatUtil {

    public DecimalFormatUtil(String s, DecimalFormatSymbols symbols) {
    }

    public static String decimalFormat(double montant) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        DecimalFormat decimalFormat = new DecimalFormat("###,###", symbols);
        String montantFormat = decimalFormat.format(montant);
        return montantFormat;
    }
}