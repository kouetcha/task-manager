package com.kouetcha.utils;

import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ExcelCellStyleUtil {

    private Workbook workbook;

    public CellStyle borderedCellStyle;
    public CellStyle lockedCellStyle;
    public CellStyle titleStyle;
    public CellStyle headerStyle;
    public CellStyle matiereStyle;
    public CellStyle matiereSimpleStyle;
    public CellStyle centeredStyle;
    public CellStyle centeredGreenStyle;
    public CellStyle centeredYellowStyle;
    public CellStyle centeredVioletStyle;
    public CellStyle centeredRedStyle;
    public CellStyle leftStyle;
    public CellStyle leftStyleRedoublant;
    public CellStyle centeredStyleRedoublant;
    public CellStyle thickBorderStyle;
    public CellStyle thickBorderStyle1;
    public CellStyle centeredBordStyle;
    public CellStyle centeredBoldStyle;
    public CellStyle centeredBoldRedStyle;
    public CellStyle semiCenteredBoldStyle;
    public CellStyle centeredBordRedStyle;
    public CellStyle unlockedCellStyle;

    // Colored styles
    public CellStyle lockedCellStyleGreen;
    public CellStyle lockedCellStyleYellow;
    public CellStyle lockedCellStyleViolet;
    public CellStyle titleStyleGreen;
    public CellStyle titleStyleYellow;
    public CellStyle titleStyleViolet;
    public CellStyle headerStyleGreen;
    public CellStyle headerStyleYellow;
    public CellStyle headerStyleViolet;
    public CellStyle matiereStyleGreen;
    public CellStyle matiereStyleYellow;
    public CellStyle matiereStyleViolet;
    public CellStyle matiereSimpleStyleGreen;
    public CellStyle matiereSimpleStyleYellow;
    public CellStyle matiereSimpleStyleViolet;
    public CellStyle leftStyleGreen;
    public CellStyle leftStyleYellow;
    public CellStyle leftStyleViolet;
    public CellStyle thickBorderStyleGreen;
    public CellStyle thickBorderStyleYellow;
    public CellStyle thickBorderStyleViolet;
    public CellStyle thickBorderStyle1Green;
    public CellStyle thickBorderStyle1Yellow;
    public CellStyle thickBorderStyle1Violet;
    public CellStyle centeredBordStyleGreen;
    public CellStyle centeredBordStyleYellow;
    public CellStyle centeredBordStyleViolet;
    public CellStyle centeredBoldStyleGreen;
    public CellStyle centeredBoldStyleYellow;
    public CellStyle centeredBoldStyleViolet;
    public CellStyle semiCenteredBoldStyleGreen;
    public CellStyle semiCenteredBoldStyleYellow;
    public CellStyle semiCenteredBoldStyleViolet;
    public CellStyle centeredBordRedStyleGreen;
    public CellStyle centeredBordRedStyleYellow;
    public CellStyle centeredBordRedStyleViolet;
    public CellStyle centeredRedStyleGreen;
    public CellStyle centeredRedStyleYellow;
    public CellStyle centeredRedStyleViolet;
    public CellStyle unlockedCellStyleGreen;
    public CellStyle unlockedCellStyleYellow;
    public CellStyle unlockedCellStyleViolet;

    public ExcelCellStyleUtil(Workbook workbook) {
        this.workbook = workbook;
        createStyles();
        createColoredStyles();
    }
    public CellStyle creerStyle(boolean isBorder, boolean isBold, HorizontalAlignment horizontalAlignment,
                                VerticalAlignment verticalAlignment, short color, short policeSize) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints(policeSize);
        font.setBold(isBold);
        font.setFontName("Times New Roman");

        CellStyle newStyle = workbook.createCellStyle();

        // Appliquer la couleur de fond
        newStyle.setFillForegroundColor(color);
        newStyle.setFillPattern((FillPatternType.DIAMONDS));
        newStyle.setWrapText(true);
        newStyle.setVerticalAlignment(verticalAlignment);
        newStyle.setAlignment(horizontalAlignment);

        newStyle.setFont(font);

        if (isBorder) {
            newStyle.setBorderBottom(BorderStyle.THIN);
            newStyle.setBorderRight(BorderStyle.THIN);
            newStyle.setBorderLeft(BorderStyle.THIN);
            newStyle.setBorderTop(BorderStyle.THIN);
        }

        // Ajuster l'indentation en fonction de l'alignement horizontal
        if (!horizontalAlignment.equals(HorizontalAlignment.CENTER)) {
            // Si l'alignement n'est pas centré, appliquer une indentation
            newStyle.setIndention((short) 1);
        }

        return newStyle;
    }


    private void createStyles() {
        // Bordered Cell Style
        borderedCellStyle = workbook.createCellStyle();
        borderedCellStyle.setBorderBottom(BorderStyle.THIN);
        borderedCellStyle.setBorderTop(BorderStyle.THIN);
        borderedCellStyle.setBorderLeft(BorderStyle.THIN);
        borderedCellStyle.setBorderRight(BorderStyle.MEDIUM);

        // Locked Cell Style
        lockedCellStyle = workbook.createCellStyle();
        lockedCellStyle.cloneStyleFrom(borderedCellStyle);
        lockedCellStyle.setAlignment(HorizontalAlignment.CENTER);
        lockedCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        lockedCellStyle.setLocked(true);

        // Title Style
        titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Header Style
        headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.cloneStyleFrom(borderedCellStyle);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setLocked(true);

        // Matiere Style
        matiereStyle = workbook.createCellStyle();
        Font matiereFont = workbook.createFont();
        matiereFont.setBold(true);
        matiereStyle.cloneStyleFrom(borderedCellStyle);
        matiereStyle.setFont(matiereFont);
        matiereStyle.setAlignment(HorizontalAlignment.CENTER);
        matiereStyle.setVerticalAlignment(VerticalAlignment.DISTRIBUTED);
        matiereStyle.setLocked(true);

        // Matiere Simple Style
        matiereSimpleStyle = workbook.createCellStyle();
        Font matiereSimpleFont = workbook.createFont();
        matiereSimpleFont.setBold(false);
        matiereSimpleStyle.cloneStyleFrom(borderedCellStyle);
        matiereSimpleStyle.setFont(matiereSimpleFont);
        matiereSimpleStyle.setAlignment(HorizontalAlignment.CENTER);
        matiereSimpleStyle.setVerticalAlignment(VerticalAlignment.DISTRIBUTED);
        matiereSimpleStyle.setLocked(true);

        // Centered Style
        Font centeredFont = workbook.createFont();
        centeredFont.setBold(false);
        centeredStyle = workbook.createCellStyle();
        centeredStyle.cloneStyleFrom(borderedCellStyle);
        centeredStyle.setFont(centeredFont);
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredStyle.setLocked(true);

        //Centered green light
        Font centeredGreenFont = workbook.createFont();
        centeredGreenFont.setBold(false);
        centeredGreenStyle = workbook.createCellStyle();
        centeredGreenStyle.cloneStyleFrom(borderedCellStyle);
        centeredGreenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        centeredGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        centeredGreenStyle.setFont(centeredGreenFont);
        centeredGreenStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredGreenStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredGreenStyle.setLocked(true);

        //Centered Yellow light
        Font centeredYellowFont = workbook.createFont();
        centeredYellowFont.setBold(false);
        centeredYellowStyle = workbook.createCellStyle();
        centeredYellowStyle.cloneStyleFrom(borderedCellStyle);
        centeredYellowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        centeredYellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        centeredYellowStyle.setFont(centeredYellowFont);
        centeredYellowStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredYellowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredYellowStyle.setLocked(true);

        //Centered violet clair
        Font centeredVioletFont = workbook.createFont();
        centeredVioletFont.setBold(false);
        centeredVioletStyle = workbook.createCellStyle();
        centeredVioletStyle.cloneStyleFrom(borderedCellStyle);
        centeredVioletStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        centeredVioletStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        centeredVioletStyle.setFont(centeredVioletFont);
        centeredVioletStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredVioletStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredVioletStyle.setLocked(true);

        // Centered Red Style
        centeredRedStyle = workbook.createCellStyle();
        Font centeredRedFont = workbook.createFont();
        centeredRedFont.setColor(IndexedColors.RED.getIndex());
        centeredRedStyle.cloneStyleFrom(borderedCellStyle);
        centeredRedStyle.setFont(centeredRedFont);
        centeredRedStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredRedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredRedStyle.setLocked(true);

        // Left Style
        Font leftFont = workbook.createFont();
        leftFont.setBold(false);
        leftStyle = workbook.createCellStyle();
        leftStyle.cloneStyleFrom(borderedCellStyle);
        leftStyle.setFont(leftFont);
        leftStyle.setAlignment(HorizontalAlignment.LEFT);
        leftStyle.setLocked(true);
        // Création du style
        leftStyleRedoublant = workbook.createCellStyle();
        leftStyleRedoublant.cloneStyleFrom(borderedCellStyle);
        leftStyleRedoublant.setVerticalAlignment(VerticalAlignment.CENTER);

      // Création d'une nouvelle police (rouge)
        Font blackRedFont = workbook.createFont();
        blackRedFont.setFontName(leftFont.getFontName()); // Garde la même police
        blackRedFont.setFontHeightInPoints(leftFont.getFontHeightInPoints()); // Même taille
        blackRedFont.setBold(true); // En gras
        blackRedFont.setColor(IndexedColors.BLACK.index); // Rouge
        leftStyleRedoublant.setFont(blackRedFont);
        //leftStyleRedoublant.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        //leftStyleRedoublant.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        leftStyleRedoublant.setAlignment(HorizontalAlignment.LEFT);
        leftStyleRedoublant.setVerticalAlignment(VerticalAlignment.DISTRIBUTED);
        leftStyleRedoublant.setLocked(true);

        centeredStyleRedoublant=workbook.createCellStyle();
        centeredStyleRedoublant.cloneStyleFrom(leftStyleRedoublant);
        centeredStyleRedoublant.setAlignment(HorizontalAlignment.CENTER);
        centeredStyleRedoublant.setVerticalAlignment(VerticalAlignment.DISTRIBUTED);


        //thick style
        thickBorderStyle = workbook.createCellStyle();
        thickBorderStyle.cloneStyleFrom(centeredStyle);
        thickBorderStyle.setBorderRight(BorderStyle.THICK);
        thickBorderStyle.setLocked(true);

        //thick simple style
        thickBorderStyle1 = workbook.createCellStyle();
        thickBorderStyle1.setBorderRight(BorderStyle.THICK);
        thickBorderStyle1.setLocked(true);

        // Centered Border Style
        Font centeredBordFont = workbook.createFont();
        centeredBordFont.setBold(true);
        centeredBordStyle = workbook.createCellStyle();
        centeredBordStyle.cloneStyleFrom(borderedCellStyle);
        centeredBordStyle.setFont(centeredBordFont);
        centeredBordStyle.setBorderRight(BorderStyle.THICK);
        centeredBordStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredBordStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredBordStyle.setLocked(true);

        //centered bold style
        Font centeredBoldFont = workbook.createFont();
        centeredBoldFont.setBold(true);
        centeredBoldStyle = workbook.createCellStyle();
        centeredBoldStyle.cloneStyleFrom(borderedCellStyle);
        centeredBoldStyle.setFont(centeredBoldFont);
        centeredBoldStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredBoldStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredBoldStyle.setIndention((short) 6);
        centeredBoldStyle.setLocked(true);
        //centered bold red style
        Font centeredBoldRedFont = workbook.createFont();
        centeredBoldRedFont.setBold(true);
        centeredBoldRedStyle = workbook.createCellStyle();
        centeredBoldRedFont.setColor(IndexedColors.RED.getIndex());
        centeredBoldRedStyle.cloneStyleFrom(borderedCellStyle);
        centeredBoldRedStyle.setFont(centeredBoldRedFont);
        centeredBoldRedStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredBoldRedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredBoldRedStyle.setIndention((short) 6);
        centeredBoldRedStyle.setLocked(true);

        //Semi centered
        Font semiCenteredBoldFont = workbook.createFont();
        semiCenteredBoldFont.setBold(true);
        semiCenteredBoldFont.setColor(IndexedColors.WHITE.getIndex());
        semiCenteredBoldStyle = workbook.createCellStyle();
        semiCenteredBoldStyle.cloneStyleFrom(borderedCellStyle);
        semiCenteredBoldFont.setFontHeightInPoints((short) 12);
        semiCenteredBoldStyle.setFont(semiCenteredBoldFont);
        semiCenteredBoldStyle.setAlignment(HorizontalAlignment.CENTER);
        semiCenteredBoldStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        semiCenteredBoldStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        semiCenteredBoldStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        semiCenteredBoldStyle.setIndention((short) 3);
        semiCenteredBoldStyle.setLocked(true);

        //Center border red
        centeredBordRedStyle = workbook.createCellStyle();
        centeredBordRedStyle.cloneStyleFrom(centeredBoldRedStyle);
        centeredBordRedStyle.setBorderRight(BorderStyle.THICK);

        unlockedCellStyle = workbook.createCellStyle();
        unlockedCellStyle.cloneStyleFrom(borderedCellStyle);
        unlockedCellStyle.setAlignment(HorizontalAlignment.CENTER);
        unlockedCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        unlockedCellStyle.setLocked(false);
    }

    // Method to create colored styles based on an existing style
    private CellStyle createColoredStyle(CellStyle baseStyle, short color) {
        CellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(baseStyle);
        newStyle.setFillForegroundColor(color);
      /*  newStyle.setBorderBottom(BorderStyle.THIN);
        newStyle.setBorderRight(BorderStyle.THIN);
        newStyle.setBorderLeft(BorderStyle.THIN);
        newStyle.setBorderTop(BorderStyle.THIN);*/
        newStyle.setFillPattern(FillPatternType.DIAMONDS);
        return newStyle;
    }
    private CellStyle createColoredStyle(CellStyle baseStyle, short color, int fontSize) {
        CellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(baseStyle);
        newStyle.setFillForegroundColor(color);
        newStyle.setFillPattern(FillPatternType.DIAMONDS);
       newStyle.setBorderBottom(BorderStyle.THIN);
        newStyle.setBorderRight(BorderStyle.THIN);
        newStyle.setBorderLeft(BorderStyle.THIN);
        newStyle.setBorderTop(BorderStyle.THIN);
        // Création d'une nouvelle police avec la taille spécifiée
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) fontSize);

        // Application de la police au style
        newStyle.setFont(font);
        newStyle.setWrapText(true);

        return newStyle;
    }
    public CellStyle obtenirStyleGreen(int font){
        return createColoredStyle(titleStyle,IndexedColors.LIGHT_GREEN.getIndex(),font);
    }
    public CellStyle obtenirStyleYellow(int font){
        return createColoredStyle(titleStyle, IndexedColors.LIGHT_YELLOW.getIndex(),font);
    }
    public CellStyle obtenirStyleViolet(int font){
        return createColoredStyle(titleStyle,IndexedColors.LAVENDER.getIndex(),font);
    }


    // Method to create variations of existing styles in green, yellow, and violet
    private void createColoredStyles() {
        // Create green, yellow, and violet variations for each style
        centeredGreenStyle = createColoredStyle(centeredStyle, IndexedColors.LIGHT_GREEN.getIndex());
        centeredYellowStyle = createColoredStyle(centeredStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        centeredVioletStyle = createColoredStyle(centeredStyle, IndexedColors.LAVENDER.getIndex());

        lockedCellStyleGreen = createColoredStyle(lockedCellStyle, IndexedColors.LIGHT_GREEN.getIndex());
        lockedCellStyleYellow = createColoredStyle(lockedCellStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        lockedCellStyleViolet = createColoredStyle(lockedCellStyle, IndexedColors.LAVENDER.getIndex());

        titleStyleGreen = createColoredStyle(titleStyle, IndexedColors.LIGHT_GREEN.getIndex());
        titleStyleYellow = createColoredStyle(titleStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        titleStyleViolet = createColoredStyle(titleStyle, IndexedColors.LAVENDER.getIndex());

        headerStyleGreen = createColoredStyle(headerStyle, IndexedColors.LIGHT_GREEN.getIndex());
        headerStyleYellow = createColoredStyle(headerStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        headerStyleViolet = createColoredStyle(headerStyle, IndexedColors.LAVENDER.getIndex());

        matiereStyleGreen = createColoredStyle(matiereStyle, IndexedColors.LIGHT_GREEN.getIndex());
        matiereStyleYellow = createColoredStyle(matiereStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        matiereStyleViolet = createColoredStyle(matiereStyle, IndexedColors.LAVENDER.getIndex());

        matiereSimpleStyleGreen = createColoredStyle(matiereSimpleStyle, IndexedColors.LIGHT_GREEN.getIndex());
        matiereSimpleStyleYellow = createColoredStyle(matiereSimpleStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        matiereSimpleStyleViolet = createColoredStyle(matiereSimpleStyle, IndexedColors.LAVENDER.getIndex());

        leftStyleGreen = createColoredStyle(leftStyle, IndexedColors.LIGHT_GREEN.getIndex());
        leftStyleYellow = createColoredStyle(leftStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        leftStyleViolet = createColoredStyle(leftStyle, IndexedColors.LAVENDER.getIndex());

        thickBorderStyleGreen = createColoredStyle(thickBorderStyle, IndexedColors.LIGHT_GREEN.getIndex());
        thickBorderStyleYellow = createColoredStyle(thickBorderStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        thickBorderStyleViolet = createColoredStyle(thickBorderStyle, IndexedColors.LAVENDER.getIndex());

        thickBorderStyle1Green = createColoredStyle(thickBorderStyle1, IndexedColors.LIGHT_GREEN.getIndex());
        thickBorderStyle1Yellow = createColoredStyle(thickBorderStyle1, IndexedColors.LIGHT_YELLOW.getIndex());
        thickBorderStyle1Violet = createColoredStyle(thickBorderStyle1, IndexedColors.LAVENDER.getIndex());

        centeredBordStyleGreen = createColoredStyle(centeredBordStyle, IndexedColors.LIGHT_GREEN.getIndex());
        centeredBordStyleYellow = createColoredStyle(centeredBordStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        centeredBordStyleViolet = createColoredStyle(centeredBordStyle, IndexedColors.LAVENDER.getIndex());

        centeredBoldStyleGreen = createColoredStyle(centeredBoldStyle, IndexedColors.LIGHT_GREEN.getIndex());
        centeredBoldStyleYellow = createColoredStyle(centeredBoldStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        centeredBoldStyleViolet = createColoredStyle(centeredBoldStyle, IndexedColors.LAVENDER.getIndex());

        semiCenteredBoldStyleGreen = createColoredStyle(semiCenteredBoldStyle, IndexedColors.LIGHT_GREEN.getIndex());
        semiCenteredBoldStyleYellow = createColoredStyle(semiCenteredBoldStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        semiCenteredBoldStyleViolet = createColoredStyle(semiCenteredBoldStyle, IndexedColors.LAVENDER.getIndex());

        centeredBordRedStyleGreen = createColoredStyle(centeredBordRedStyle, IndexedColors.LIGHT_GREEN.getIndex());
        centeredBordRedStyleYellow = createColoredStyle(centeredBordRedStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        centeredBordRedStyleViolet = createColoredStyle(centeredBordRedStyle, IndexedColors.LAVENDER.getIndex());

        centeredRedStyleGreen = createColoredStyle(centeredRedStyle, IndexedColors.LIGHT_GREEN.getIndex());
        centeredRedStyleYellow = createColoredStyle(centeredRedStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        centeredRedStyleViolet = createColoredStyle(centeredRedStyle, IndexedColors.LAVENDER.getIndex());

        unlockedCellStyleGreen=createColoredStyle(unlockedCellStyle, IndexedColors.LIGHT_GREEN.getIndex());
        unlockedCellStyleYellow=createColoredStyle(unlockedCellStyle, IndexedColors.LIGHT_YELLOW.getIndex());
        unlockedCellStyleViolet=createColoredStyle(unlockedCellStyle, IndexedColors.LAVENDER.getIndex());
    }
    public static Map<String, List<Integer>> findColumnsByValuesMap(Sheet sheet, int rowIndex, List<String> searchStrings) {
        Map<String, List<Integer>> columnsMap = new HashMap<>();

        // Initialize the map with empty lists
        for (String searchString : searchStrings) {
            columnsMap.put(searchString, new ArrayList<>());
        }

        Row row = sheet.getRow(rowIndex);

        if (row != null) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    if (columnsMap.containsKey(cellValue)) {
                        columnsMap.get(cellValue).add(cell.getColumnIndex());
                    }
                }
            }
        }

        return columnsMap;
    }
    public static List<Integer> findColumnsByValues(Sheet sheet, int rowIndex, List<String> searchStrings) {
        List<Integer> columns = new ArrayList<>();
        Row row = sheet.getRow(rowIndex);

        if (row != null) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && searchStrings.contains(cell.getStringCellValue())) {
                    Logger.getLogger(ExcelCellStyleUtil.class.getName()).info("Valeur="+cell.getStringCellValue());
                    columns.add(cell.getColumnIndex());
                }
            }
        }

        return columns;
    }
}