package com.kouetcha.config.utils;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.kouetcha.config.Valeur;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MissionUtil {

    public static Table titreDePage(float pageWidth, String reference, String titre) {
        Table header = new Table(new float[]{pageWidth / 2, pageWidth / 2});
        header.setWidth(pageWidth).setMarginBottom(20f);

        header.addCell(new Cell()
                .add(new Paragraph().add(new Text(titre.concat("_N° ".concat(reference).concat(Valeur.SRH)))
                        .setCharacterSpacing(0.6f)
                        .setBold().setFontSize(12))
                ).setHorizontalAlignment(HorizontalAlignment.LEFT).setBorder(Border.NO_BORDER));

        header.addCell(new Cell()
                .add(new Paragraph().add(new Text("Cotonou, le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .setFontSize(12))
                ).setHorizontalAlignment(HorizontalAlignment.RIGHT).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));

        return header;
    }

    //formattage date
    public static String formateDate(LocalDate localDate) {
        /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");

        String formatDate = localDate.format(formatter);*/

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

        String formatDate = localDate.format(formatter);

        // Convertir la première lettre du jour en majuscule
        formatDate = formatDate.substring(0, 1).toUpperCase() + formatDate.substring(1);

        // Convertir la première lettre du mois en majuscule
        int indexFirstSpace = formatDate.indexOf(" ");
        int indexSecondSpace = formatDate.indexOf(" ", indexFirstSpace + 1);
        String month = formatDate.substring(indexFirstSpace + 1, indexSecondSpace);
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        formatDate = formatDate.substring(0, indexFirstSpace + 1) + month + formatDate.substring(indexSecondSpace);

        return formatDate;
    }
}