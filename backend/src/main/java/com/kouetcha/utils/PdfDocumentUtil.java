package com.kouetcha.utils;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

import static com.kouetcha.utils.StringUtils.capitalizeWords;

public class PdfDocumentUtil {

    private static String fontPathBold;
    private static String fontPathRegular;
    private static String fontPathItalic;

    public static void setFontPathBold(String path) {
        fontPathBold = path;
    }

    public static void setFontPathRegular(String path) {
        fontPathRegular = path;
    }

    public static void setFontPathItalic(String path) {
        fontPathItalic = path;
    }



    public static Document createPdfDocument(String src, String src1, String dest,
                                             float topMargin, float rightMargin,
                                             float bottomMargin, float leftMargin,
                                             String nomDomaine, int piedMarginTop,
                                             int piedMarginLeft) throws IOException {


        Path filePathD = Paths.get(dest);
        Files.createDirectories(filePathD.getParent());

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdfDoc = new PdfDocument(writer);

        PdfDocument srcDoc;
        PdfDocument srcDoc1 = null;

        float width;
        float height;

        if (src != null) {
            PdfReader reader = new PdfReader(src);
            srcDoc = new PdfDocument(reader);
            width = srcDoc.getPage(1).getPageSize().getWidth();
            height = srcDoc.getPage(1).getPageSize().getHeight();
        } else {
            srcDoc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
            srcDoc.addNewPage(PageSize.A4);
            width = PageSize.A4.getWidth();
            height = PageSize.A4.getHeight();
        }

        if (src1 != null) {
            srcDoc1 = new PdfDocument(new PdfReader(src1));
        }

        pdfDoc.setDefaultPageSize(new PageSize(width, height));
        Document document = new Document(pdfDoc);
        document.setMargins(topMargin, rightMargin, bottomMargin, leftMargin);

        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE,
                new HeaderFooterHandler(srcDoc, srcDoc1, nomDomaine, piedMarginTop, piedMarginLeft));

        if (src == null && src1 == null) {
            pdfDoc.addNewPage();
        }

        return document;
    }
    public static PdfDocumentBundle createPdfDocumentBundle(String src, String src1, String dest,
                                                            float topMargin, float rightMargin,
                                                            float bottomMargin, float leftMargin,
                                                            String nomDomaine, int piedMarginTop,
                                                            int piedMarginLeft) {

       try {


        Path filePathD = Paths.get(dest);
        Files.createDirectories(filePathD.getParent());

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdfDoc = new PdfDocument(writer);

        PdfDocument srcDoc;
        PdfDocument srcDoc1 = null;

        float width;
        float height;

        if (src != null) {
            PdfReader reader = new PdfReader(src);
            srcDoc = new PdfDocument(reader);
            width = srcDoc.getPage(1).getPageSize().getWidth();
            height = srcDoc.getPage(1).getPageSize().getHeight();
        } else {
            srcDoc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
            srcDoc.addNewPage(PageSize.A4);
            width = PageSize.A4.getWidth();
            height = PageSize.A4.getHeight();
        }

        if (src1 != null) {
            srcDoc1 = new PdfDocument(new PdfReader(src1));
        }

        pdfDoc.setDefaultPageSize(new PageSize(width, height));
        Document document = new Document(pdfDoc);
        document.setMargins(topMargin, rightMargin, bottomMargin, leftMargin);

        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE,
                new HeaderFooterHandler(srcDoc, srcDoc1, nomDomaine, piedMarginTop, piedMarginLeft));

        if (src == null && src1 == null) {
            pdfDoc.addNewPage();
        }

        return new PdfDocumentBundle(document,srcDoc,srcDoc1);
       }
       catch (Exception e){
         throw new IllegalArgumentException("Une erreur est survenue: "+e.getMessage());
       }
    }






    public static void verifierOuverturePdfSources(String... chemins) {
        for (String chemin : chemins) {
            System.out.println("Chemin: "+chemin);
            PdfReader reader = null;
            PdfDocument pdfDoc = null;
            try {
                reader = new PdfReader(chemin);
                pdfDoc = new PdfDocument(reader);

                if (pdfDoc.getNumberOfPages() == 0) {
                    throw new IOException("Le fichier PDF est vide ou invalide : " + chemin);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Erreur lors de la lecture du fichier : " + chemin, e);
            } finally {
                try {
                    if (pdfDoc != null) {
                        pdfDoc.close();
                        System.out.println("pdfDoc.isClosed(): "+pdfDoc.isClosed());

                    }
                    if (reader != null) {
                        reader.close();
                        System.out.println("reader.isCloseStream(): "+reader.isCloseStream());
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Erreur lors de la fermeture du fichier : " + chemin, e);
                }
            }
        }
    }


    public static Cell createCellCenteredNoBorderLift(String content, int fontSize, PdfFont font,float lift) {
        Paragraph paragraph = new Paragraph(content)
                .setFont(font)
                .setFontSize(fontSize)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0) // Remove margins
                .setPadding(0) // Remove padding
                .setMultipliedLeading(1f); // Adjust the line spacing factor to reduce space between lines

        Cell cell = new Cell().add(paragraph);
        cell.setBorder(Border.NO_BORDER);
        cell.setPaddingTop(-lift); // Move the text 30px (points) up
        return cell;
    }
    public static Cell createCellCenteredNoBorder(String content, int fontSize, PdfFont font) {
        Paragraph paragraph = new Paragraph(content)
                .setFont(font)
                .setFontSize(fontSize)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0) // Remove margins
                .setPadding(0) // Remove padding
                .setMultipliedLeading(1f); // Adjust the line spacing factor to reduce space between lines
        Cell cell = new Cell().add(paragraph);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }
    public static Table createTitre(String content,float fontTaille) {
        try {


        Color borderColor = new DeviceRgb(0, 100, 0);
        SolidBorder solidBorder = new SolidBorder(borderColor, 1.5f);
        List<String> texts = Arrays.asList(content);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        List<PdfFont> fonts = Arrays.asList(boldFont);
        List<Float> fontSize = Arrays.asList(fontTaille);
        Color darkBlueColor = new DeviceRgb(0, 0, 139); // Dark blue color

        return createTableTitreWithCorner(texts, fonts, fontSize, TextAlignment.CENTER, 0.0f, 0.0f, 10f, 15f, true, 1.5f, 2.5f, darkBlueColor,solidBorder,4);
        }
        catch (Exception e){
         throw new IllegalArgumentException("Une erreur est surveneue: "+e.getMessage(),e);
        }
    }
    public static Table createTitre(String content) throws IOException {
        List<String> texts = Arrays.asList(
                content
        );
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        List<PdfFont> fonts = Arrays.asList(
                boldFont
        );
        List<Float> fontSize=Arrays.asList(23f);
        Color blueColor = new DeviceRgb(0, 0, 255);
        return createTableTitre(texts,fonts,fontSize,TextAlignment.CENTER,0.0f,0.0f,5f,10f,false,1.5f,2.5f,blueColor,null);
    }
    public static Table createTitreColor(String content, float policeSize,Color color) throws IOException {
        List<String> texts = Arrays.asList(
                content
        );
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        List<PdfFont> fonts = Arrays.asList(
                boldFont
        );
        List<Float> fontSize=Arrays.asList(policeSize);

        return createTableTitre(texts,fonts,fontSize,TextAlignment.CENTER,0.0f,0.0f,5f,10f,false,1.5f,2.5f,color,null);
    }
    public static Table createTableTitre(List<String> texts, List<PdfFont> fonts, List<Float> fontSize, TextAlignment alignment, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean isBorder, float lineSpacing,Color textColor) throws IOException {
        return createTableTitre(texts,fonts,fontSize,alignment,marginLeft,marginRight,marginTop,marginBottom,isBorder,lineSpacing,0f,textColor,null);
    }
    public static Table createTableTitre(List<String> texts, List<PdfFont> fonts, List<Float> fontSize, TextAlignment alignment, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean isBorder, float lineSpacing,float characterSpacing,Color textColor) throws IOException {
        return createTableTitre(texts,fonts,fontSize,alignment,marginLeft,marginRight,marginTop,marginBottom,isBorder,lineSpacing,characterSpacing,textColor,null);
    }
    public static Table createTableTitre(List<String> texts, List<PdfFont> fonts, List<Float> fontSize, TextAlignment alignment, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean isBorder, float lineSpacing,float characterSpacing,Color textColor,SolidBorder solidBorder) throws IOException {
        Table table = new Table(new float[]{1});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginLeft(marginLeft);
        table.setMarginRight(marginRight);
        table.setMarginBottom(marginBottom);
        table.setMarginTop(marginTop);

        // Create the paragraph
        Paragraph paragraph = new Paragraph();

        // Set line spacing (interline)
        paragraph.setMultipliedLeading(lineSpacing);
        paragraph.setCharacterSpacing(characterSpacing);

        // Add each text with the corresponding font
        for (int i = 0; i < texts.size(); i++) {
            Text text = new Text(texts.get(i));
            text.setFont(fonts.get(i));
            text.setFontSize(fontSize.get(i));
            paragraph.add(text);
        }

        // Add the paragraph to the cell
        Cell cell = new Cell().add(paragraph);
        cell.setTextAlignment(alignment);
        cell.setFontColor(textColor);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (!isBorder) {
            cell.setBorder(Border.NO_BORDER);
            table.setBorder(Border.NO_BORDER);
        }else {
            if(solidBorder!=null){
                table.setBorder(solidBorder);
            }
        }


        table.addCell(cell);

        return table;
    }
    public static Cell createDottedLineCell(float lineWidth, float height,float raduis) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setHeight(height);
        cell.setNextRenderer(new DottedLineCellRenderer(cell, lineWidth,raduis));
        return cell;
    }

    private static class DottedLineCellRenderer extends CellRenderer {
        private final float lineWidth;
        private final float raduis;

        public DottedLineCellRenderer(Cell modelElement, float lineWidth, float raduis) {
            super(modelElement);
            this.lineWidth = lineWidth;
            this.raduis=raduis;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);

            // Récupérer les coordonnées de la cellule
            Rectangle rect = getOccupiedAreaBBox();
            PdfCanvas canvas = drawContext.getCanvas();

            // Définir l'épaisseur de la ligne
            canvas.setLineWidth(lineWidth);

            float margin = 10f;
            float x1 = rect.getLeft() + margin;
            float x2 = rect.getRight() - margin;
            float y = rect.getBottom() + rect.getHeight() / 2;

            // Dessiner la ligne principale
            canvas.moveTo(x1, y).lineTo(x2, y).stroke();

            // Dessiner les boules aux extrémités
            drawCircle(canvas, x1, y, raduis); // Boule gauche
            drawCircle(canvas, x2, y, raduis); // Boule droite
        }

        private void drawCircle(PdfCanvas canvas, float centerX, float centerY, float radius) {
            canvas.circle(centerX, centerY, radius).fill();
        }
    }
    public static Cell createArrowLineCell(final float lineWidth, final float fixedHeight) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setHeight(fixedHeight);
        // Remplace le renderer par défaut par notre renderer personnalisé
        cell.setNextRenderer(new ArrowLineCellRenderer(cell, lineWidth));
        return cell;
    }

    /**
     * Renderer personnalisé pour dessiner une ligne avec des flèches dans une Cell.
     */
    private static class ArrowLineCellRenderer extends CellRenderer {
        private final float lineWidth;

        public ArrowLineCellRenderer(Cell modelElement, float lineWidth) {
            super(modelElement);
            this.lineWidth = lineWidth;
        }

        @Override
        public void draw(DrawContext drawContext) {
            // Appel du dessin de la Cell (si besoin)
            super.draw(drawContext);

            // Récupération de la zone occupée par la Cell
            Rectangle rect = getOccupiedAreaBBox();

            // Création d'un PdfCanvas pour dessiner dans la cellule
            PdfCanvas canvas = drawContext.getCanvas();

            // Définition des marges gauche/droite et calcul des coordonnées
            float margin = 10f;
            float x1 = rect.getLeft() + margin;
            float x2 = rect.getRight() - margin;
            float y = rect.getBottom() + rect.getHeight() / 2;

            // Paramétrage de l'épaisseur de la ligne
            canvas.setLineWidth(lineWidth);

            // Dessin de la ligne principale
            canvas.moveTo(x1, y);
            canvas.lineTo(x2, y);
            canvas.stroke();

            // Paramètres pour les flèches
            double angle = 0; // pour une ligne horizontale, l'angle est 0
            double arrowLength = 10 + lineWidth * 2;  // Longueur de la flèche (ajustable)
            double arrowAngle = Math.toRadians(20);     // Angle d'ouverture de 20°

            // Dessiner la flèche à l'extrémité droite
            drawArrowHead(canvas, x2, y, angle, arrowLength, arrowAngle);
            // Dessiner la flèche à l'extrémité gauche (angle inversé)
            drawArrowHead(canvas, x1, y, angle + Math.PI, arrowLength, arrowAngle);

            // Libérer le canvas
            canvas.release();
        }

        /**
         * Dessine une flèche sous forme de deux segments.
         *
         * @param canvas      le PdfCanvas sur lequel dessiner.
         * @param tipX        l'abscisse du sommet de la flèche.
         * @param tipY        l'ordonnée du sommet de la flèche.
         * @param angle       la direction de la ligne (en radians).
         * @param arrowLength la longueur de chaque segment de la flèche.
         * @param arrowAngle  l'angle d'ouverture de la flèche (en radians).
         */
        private void drawArrowHead(PdfCanvas canvas, float tipX, float tipY,
                                   double angle, double arrowLength, double arrowAngle) {
            // Calcul du premier segment
            float x1 = (float) (tipX - arrowLength * Math.cos(angle - arrowAngle));
            float y1 = (float) (tipY - arrowLength * Math.sin(angle - arrowAngle));
            // Calcul du second segment
            float x2 = (float) (tipX - arrowLength * Math.cos(angle + arrowAngle));
            float y2 = (float) (tipY - arrowLength * Math.sin(angle + arrowAngle));

            // Dessin du premier segment
            canvas.moveTo(tipX, tipY);
            canvas.lineTo(x1, y1);
            canvas.stroke();

            // Dessin du second segment
            canvas.moveTo(tipX, tipY);
            canvas.lineTo(x2, y2);
            canvas.stroke();
        }
    }
    public static ResponseEntity<Object> retournerFichier(Path filePathD) {
        try {
            if (!Files.exists(filePathD)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable ou illisible.");
            }

            Resource fileResource = new UrlResource(filePathD.toUri());
            if (!(fileResource.exists() && fileResource.isReadable())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable ou illisible.");
            }

            String filename = fileResource.getFilename();
            String extension = getFileExtension(filename).toLowerCase();

            MediaType mediaType = getMediaTypeByExtension(extension);

            // Si type inconnu dans la map, essayer probeContentType
            if (mediaType == null) {
                String probeType = Files.probeContentType(filePathD);
                if (probeType != null) {
                    try {
                        mediaType = MediaType.parseMediaType(probeType);
                    } catch (Exception e) {
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    }
                } else {
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(fileResource);

        } catch (Exception e) {
            throw new IllegalArgumentException("Une erreur est survenue : " + e.getMessage());
        }
    }



    /**
     * Retourne un MediaType Spring connu selon l'extension de fichier,
     * ou null si non trouvé (à traiter ensuite avec probeContentType)
     */
    private static MediaType getMediaTypeByExtension(String extension) {
        Map<String, MediaType> map = new HashMap<>();

        map.put("pdf", MediaType.APPLICATION_PDF);
        map.put("png", MediaType.IMAGE_PNG);
        map.put("jpg", MediaType.IMAGE_JPEG);
        map.put("jpeg", MediaType.IMAGE_JPEG);
        map.put("gif", MediaType.IMAGE_GIF);
        map.put("txt", MediaType.TEXT_PLAIN);
        map.put("html", MediaType.TEXT_HTML);
        map.put("csv", MediaType.TEXT_PLAIN);
        map.put("json", MediaType.APPLICATION_JSON);
        map.put("xml", MediaType.APPLICATION_XML);
        map.put("doc", MediaType.parseMediaType("application/msword"));
        map.put("docx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        map.put("xls", MediaType.parseMediaType("application/vnd.ms-excel"));
        map.put("xlsx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        map.put("ppt", MediaType.parseMediaType("application/vnd.ms-powerpoint"));
        map.put("pptx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        // Ajoute d'autres extensions si nécessaire

        return map.get(extension);
    }


    /**
     * Helper pour récupérer l'extension d'un fichier depuis son nom.
     */
    private static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return "";
        return filename.substring(dotIndex + 1);
    }
    public static Table createTitrePaie(String content,float fontTaille) throws IOException {
        Color borderColor = new DeviceRgb(0, 100, 0);
        SolidBorder solidBorder = new SolidBorder(borderColor, 1.5f);
        List<String> texts = Arrays.asList(content);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        List<PdfFont> fonts = Arrays.asList(boldFont);
        List<Float> fontSize = Arrays.asList(fontTaille);
        Color darkBlueColor = new DeviceRgb(0, 0, 139); // Dark blue color

        return createTableTitreWithCorner(texts, fonts, fontSize, TextAlignment.CENTER, 0.0f, 0.0f, 10f, 15f, true, 1.5f, 2.5f, darkBlueColor,solidBorder,4);

    }
    public static Table createTitrePaieNoBorder(String content,float fontTaille) throws IOException {
        Color borderColor = new DeviceRgb(0, 100, 0);
        List<String> texts = Arrays.asList(content);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        List<PdfFont> fonts = Arrays.asList(boldFont);
        List<Float> fontSize = Arrays.asList(fontTaille);
        Color darkBlueColor = new DeviceRgb(20, 20, 150); // Dark blue color

        return createTableTitreWithCorner(texts, fonts, fontSize, TextAlignment.CENTER, 0.0f, 0.0f, 0f, 15f, false, 1.5f, 2.5f, darkBlueColor,null,4);

    }
    public static Table createTableTitreWithCorner(List<String> texts, List<PdfFont> fonts, List<Float> fontSize, TextAlignment alignment, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean isBorder, float lineSpacing,float characterSpacing,Color textColor,SolidBorder solidBorder,int raduis) throws IOException {
        Table table = new Table(new float[]{1});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginLeft(marginLeft);
        table.setMarginRight(marginRight);
        table.setMarginBottom(marginBottom);
        table.setMarginTop(marginTop);

        // Create the paragraph
        Paragraph paragraph = new Paragraph();

        // Set line spacing (interline)
        paragraph.setMultipliedLeading(lineSpacing);
        paragraph.setCharacterSpacing(characterSpacing);

        // Add each text with the corresponding font
        for (int i = 0; i < texts.size(); i++) {
            Text text = new Text(texts.get(i));
            text.setFont(fonts.get(i));
            text.setFontSize(fontSize.get(i));
            paragraph.add(text);
        }

        // Add the paragraph to the cell
        Cell cell = new Cell().add(paragraph);
        cell.setTextAlignment(alignment);
        cell.setFontColor(textColor);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setBorder(Border.NO_BORDER);
        table.setBorder(Border.NO_BORDER);
        if (!isBorder) {
            cell.setBorder(Border.NO_BORDER);
            table.setBorder(Border.NO_BORDER);
        }else {


            }


        if(solidBorder!=null&&isBorder){
            //table.setBorder(solidBorder);
            cell.setNextRenderer(new RoundedBorderCellRenderer(cell,true,true,raduis));

        }
        table.addCell(cell);

        return table;
    }
    private static void adjustImageSize(Image image, float maxWidth, float maxHeight) {
        float width = image.getImageWidth();
        float height = image.getImageHeight();

        float scale = Math.min(maxWidth / width, maxHeight / height);

        if (width * scale > maxWidth) {
            scale = maxWidth / width;
        }

        if (height * scale > maxHeight) {
            scale = maxHeight / height;
        }

        image.scaleAbsolute(Math.min(width * scale, maxWidth), Math.min(height * scale, maxHeight));
        image.setHorizontalAlignment(HorizontalAlignment.CENTER);
        image.setTextAlignment(TextAlignment.CENTER);
    }
    public static Cell createCell(String content, boolean isHeader, PdfFont font, int rowSpan, int colSpan,boolean isCentered) throws IOException {
        Paragraph paragraph = new Paragraph(content);
        paragraph.setFont(font);
        Cell cell = new Cell(rowSpan, colSpan).add(paragraph);

        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(100, 100, 100));
            cell.setFontColor(new DeviceRgb(255, 255, 255));
            cell.setBold();
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {
            paragraph.setMarginLeft(15);
            cell.setTextAlignment(TextAlignment.LEFT);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        }
        if(isCentered){
            paragraph.setMarginLeft(0);
            cell.setTextAlignment(TextAlignment.CENTER);
        }

        return cell;
    }
    public static Cell createCell(String content,int size, boolean isHeader, PdfFont font, int rowSpan, int colSpan,boolean isCentered,int marginLeft) throws IOException {
        Paragraph paragraph = new Paragraph(content);
        paragraph.setFont(font);
        paragraph.setFontSize(size);
        Cell cell = new Cell(rowSpan, colSpan).add(paragraph);

        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(100, 100, 100));
            cell.setFontColor(new DeviceRgb(255, 255, 255));
            cell.setBold();
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {
            paragraph.setMarginLeft(marginLeft);
            cell.setTextAlignment(TextAlignment.LEFT);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        }
        if(isCentered){
            paragraph.setMarginLeft(0);
            cell.setTextAlignment(TextAlignment.CENTER);
        }

        return cell;
    }
    public static Cell createCell(String content,int size, boolean isHeader, PdfFont font, int rowSpan, int colSpan,boolean isCentered,int marginLeft,TextAlignment textAlignment){
        Paragraph paragraph = new Paragraph(content);
        paragraph.setFont(font);
        paragraph.setFontSize(size);
        Cell cell = new Cell(rowSpan, colSpan).add(paragraph);

        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(100, 100, 100));
            cell.setFontColor(new DeviceRgb(255, 255, 255));
            cell.setBold();
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {

            if(textAlignment.equals(TextAlignment.RIGHT)){
                paragraph.setMarginRight(marginLeft);
            }
            else {
                paragraph.setMarginLeft(marginLeft);
            }

            cell.setTextAlignment(textAlignment);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        }
        if(isCentered){
            paragraph.setMarginLeft(0);
            cell.setTextAlignment(TextAlignment.CENTER);
        }

        return cell;
    }
    public static Cell createCell(String content,int size, boolean isHeader, PdfFont font, int rowSpan, int colSpan,boolean isCentered)  {
       try {
           return  createCell(content,size,isHeader,font,rowSpan,colSpan,isCentered,15);

       }
       catch (Exception e){
         throw new IllegalArgumentException("Erreur : "+e.getMessage())  ;
       }
         }
    public static Cell createCell(String content, boolean isHeader,PdfFont font) throws IOException {
        return createCell(content,isHeader,font,false);
    }
    public static Cell createCell(String content, boolean isHeader,PdfFont font,boolean isCentered) throws IOException {
        Paragraph paragraph=new Paragraph(content);

        paragraph.setFont(font);
        Cell cell = new Cell().add(paragraph);
        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(100, 100, 100));
            cell.setFontColor(new DeviceRgb(255, 255, 255));
            cell.setBold();
            //cell.setBorder(Border.NO_BORDER);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {
            paragraph.setMarginLeft(15);
            cell.setTextAlignment(TextAlignment.LEFT);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        }
        if(isCentered){
            paragraph.setMarginLeft(0);
            cell.setTextAlignment(TextAlignment.CENTER);
        }

        return cell;
    }
    public static Cell createCellWithColor(String content, boolean isHeader, PdfFont font, boolean isCentered, DeviceRgb backgroundColor, DeviceRgb fontColor) throws IOException {
        Paragraph paragraph = new Paragraph(content);
        paragraph.setFont(font);

        Cell cell = new Cell().add(paragraph);

        if (isHeader) {
            // Si une couleur de fond est spécifiée, on l'applique
            if (backgroundColor != null) {
                cell.setBackgroundColor(backgroundColor);
            } else {
                cell.setBackgroundColor(new DeviceRgb(100, 100, 100)); // Couleur par défaut
            }

            // Si une couleur de texte est spécifiée, on l'applique
            if (fontColor != null) {
                cell.setFontColor(fontColor);
            } else {
                cell.setFontColor(new DeviceRgb(255, 255, 255)); // Couleur par défaut
            }

            cell.setBold();
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {
            paragraph.setMarginLeft(15);
            cell.setTextAlignment(TextAlignment.LEFT);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);

            // Appliquer les couleurs si spécifiées pour les cellules non en-tête
            if (backgroundColor != null) {
                cell.setBackgroundColor(backgroundColor);
            }
            if (fontColor != null) {
                cell.setFontColor(fontColor);
            }
        }

        // Centrage du texte si spécifié
        if (isCentered) {
            paragraph.setMarginLeft(0);
            cell.setTextAlignment(TextAlignment.CENTER);
        }

        return cell;
    }

    public static Cell createCell(String content, boolean isHeader, PdfFont font, int rowSpan, int colSpan) throws IOException {
        return createCell(content,isHeader,font,rowSpan,colSpan,false);
    }

    public static PdfFont helvetica() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }
    public static PdfFont italicHelvetica() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    }
    public static PdfFont boldHelvetica() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    }
    public static PdfFont timesRoman() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
    }
    public static PdfFont italicTimesRoman() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);
    }
    public static PdfFont boldtimesRoman() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);
    }
    public static PdfFont boldNormaltimesRoman() throws IOException {
        return  PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
    }
    public static PdfFont lucidaBold() throws IOException {
        PdfFont lucidaBold = PdfFontFactory.createFont(fontPathBold, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        return  lucidaBold;
    }
    public static PdfFont lucida() throws IOException {
        PdfFont lucidaRegular = PdfFontFactory.createFont(fontPathRegular, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        return  lucidaRegular;
    }
    public static PdfFont lucidaItalic() throws IOException {
        PdfFont lucidaItalic = PdfFontFactory.createFont(fontPathItalic, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        return  lucidaItalic;
    }


    public static class HeaderFooterHandler implements IEventHandler {
        private PdfDocument srcDoc;
        private PdfDocument srcDoc1;
        private int piedMarginTop;
        private int piedMarginLeft;
        private String nomDomaine;

        public HeaderFooterHandler(PdfDocument srcDoc, PdfDocument srcDoc1,String nomDomaine,int piedMarginTop, int piedMarginLeft) {
            this.srcDoc = srcDoc;
            this.srcDoc1 = srcDoc1;
            this.piedMarginTop=piedMarginTop;
            this.piedMarginLeft=piedMarginLeft;
            this.nomDomaine=nomDomaine;
        }


        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);

            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            try {
                PdfPage basePage = null;

                if (srcDoc1 != null && pageNumber > 1 && srcDoc1.getNumberOfPages() >= 1) {
                    basePage = srcDoc1.getPage(1);
                } else if (srcDoc != null && srcDoc.getNumberOfPages() >= 1) {
                    basePage = srcDoc.getPage(1);
                }

                if (basePage != null) {
                    PdfFormXObject pageCopy = basePage.copyAsFormXObject(pdfDoc);
                    pdfCanvas.addXObjectAt(pageCopy, 0, 0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            pdfCanvas.beginText();
            try {
                PdfFont font = lucidaItalic();
                pdfCanvas.setFontAndSize(font, 12);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Formatter la date et l'heure
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRENCH);

            String formattedDay = now.format(dayFormatter);
            String formattedTime = now.format(timeFormatter);

            // Texte du pied de page
            String footerText;
            if (pageNumber > 1) {
                footerText = String.format("Édité le %s à %s © %s    |    Page %d", formattedDay, formattedTime, nomDomaine, pageNumber);
            } else {
                footerText = String.format("Édité le %s à %s © %s", formattedDay, formattedTime, nomDomaine);
            }

            // Position dynamique
            Rectangle pageSize = page.getPageSize();
            float x = pageSize.getLeft() + piedMarginLeft;
            float y = pageSize.getBottom() + piedMarginTop;

            // Ajouter le texte du pied de page sur une seule ligne
            pdfCanvas.moveText(x, y).showText(footerText);

            pdfCanvas.endText();
            pdfCanvas.release();
        }


    }
    public static Table createSignatureTable(List<SignatureData> signatures,int width, int height,int marginTop) throws IOException {
        Table tableSignature = new Table(UnitValue.createPercentArray(new float[]{1, 1})); // 2 colonnes
        tableSignature.setWidth(UnitValue.createPercentValue(100));
        tableSignature.setMarginLeft(20);
        tableSignature.setMarginTop(marginTop);
        tableSignature.setBorder(Border.NO_BORDER);
        tableSignature.setKeepTogether(true);

        // Cas avec une seule signature
        if (signatures.size() == 1) {
            // Ajouter une cellule vide dans la première colonne
            tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));

            // Ajouter la signature unique dans la deuxième colonne
            SignatureData signature = signatures.get(0);

            // Titre de la signature
            Paragraph title = new Paragraph(signature.getTitle()).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
            title.setFont(PdfDocumentUtil.boldNormaltimesRoman());
            Cell titleCell = new Cell().add(title).setBorder(Border.NO_BORDER);
            tableSignature.addCell(titleCell);

            // Image de la signature (si disponible)
            if (signature.getImagePath() != null) {
                Image signatureImage = new Image(ImageDataFactory.create(signature.getImagePath()));
                adjustImageSize(signatureImage, 80, 70); // Ajuster la taille de l'image
                signatureImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                Cell signatureCell = new Cell().add(signatureImage).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER)); // Ajouter une cellule vide avant l'image
                tableSignature.addCell(signatureCell);
            } else {
                // Ajouter une cellule vide si l'image n'est pas disponible
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));
            }

            // Nom de la signature
            Paragraph name = new Paragraph(signature.getName()).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
            name.setFont(PdfDocumentUtil.boldNormaltimesRoman());
            Cell nameCell = new Cell().add(name).setBorder(Border.NO_BORDER);
            tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER)); // Ajouter une cellule vide avant le nom
            tableSignature.addCell(nameCell);

            return tableSignature;
        }

        // Cas avec plusieurs signatures
        for (int i = 0; i < signatures.size(); i += 2) {
            // Ajouter la première signature
            Paragraph title1 = new Paragraph(signatures.get(i).getTitle()).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
            Cell titleCell1 = new Cell().add(title1).setBorder(Border.NO_BORDER);
            tableSignature.addCell(titleCell1);

            if (i + 1 < signatures.size()) {
                // Ajouter la deuxième signature si elle existe
                Paragraph title2 = new Paragraph(signatures.get(i + 1).getTitle()).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
                Cell titleCell2 = new Cell().add(title2).setBorder(Border.NO_BORDER);
                tableSignature.addCell(titleCell2);
            } else {
                // Ajouter une cellule vide si pas de deuxième signature
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));
            }

            // Images des signatures
            if (signatures.get(i).getImagePath() != null) {
                Image signatureImage1 = new Image(ImageDataFactory.create(signatures.get(i).getImagePath()));
                adjustImageSize(signatureImage1, width, height); // Ajuster la taille de l'image
                signatureImage1.setHorizontalAlignment(HorizontalAlignment.CENTER);
                Cell signatureCell1 = new Cell().add(signatureImage1).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
                tableSignature.addCell(signatureCell1);
            } else {
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));
            }

            if (i + 1 < signatures.size() && signatures.get(i + 1).getImagePath() != null) {
                Image signatureImage2 = new Image(ImageDataFactory.create(signatures.get(i + 1).getImagePath()));
                adjustImageSize(signatureImage2, width, height); // Ajuster la taille de l'image
                signatureImage2.setHorizontalAlignment(HorizontalAlignment.CENTER);
                Cell signatureCell2 = new Cell().add(signatureImage2).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
                tableSignature.addCell(signatureCell2);
            } else {
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));
            }

            // Ajouter les noms des signataires
            Paragraph name1 = new Paragraph(signatures.get(i).getName()).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
            Cell nameCell1 = new Cell().add(name1).setBorder(Border.NO_BORDER);
            tableSignature.addCell(nameCell1);

            if (i + 1 < signatures.size()) {
                Paragraph name2 = new Paragraph(signatures.get(i + 1).getName()).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
                Cell nameCell2 = new Cell().add(name2).setBorder(Border.NO_BORDER);
                tableSignature.addCell(nameCell2);
            } else {
                tableSignature.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        }

        return tableSignature;

    }
    public static String generateFakeContent(int paragraphCount) {
        String baseParagraph = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
                + "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. "
                + "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
                + "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n\n";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paragraphCount; i++) {
            builder.append("§ ").append(i + 1).append(" - ").append(baseParagraph);
        }
        return builder.toString();
    }

    public static Table createSingleColumnTable(String[] contents, TextAlignment textAlignment, int marginTop, int marginLeft, boolean bold) throws IOException {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setBorder(Border.NO_BORDER);
        table.setMarginTop(marginTop);
        table.setMarginLeft(marginLeft);

        PdfFont font = PdfFontFactory.createFont(bold ? StandardFonts.TIMES_BOLD : StandardFonts.TIMES_ROMAN);

        for (String content : contents) {
            Paragraph paragraph = new Paragraph(content)
                    .setFont(font)
                    .setTextAlignment(textAlignment);

            Cell cell = new Cell()
                    .add(paragraph)
                    .setBorder(Border.NO_BORDER);

            table.addCell(cell);
        }

        return table;
    }


    public static Table createTable(String[] labels, String[] values, TextAlignment textAlignment,int margiTop,int marginLeft, boolean boldCol1) throws IOException {
        return createTable(1,1,labels,values,textAlignment,margiTop,marginLeft,boldCol1);
    }
    public static Table createTable(int n1, int n2,String[] labels, String[] values, TextAlignment textAlignment,int margiTop,int marginLeft, boolean boldCol1) throws IOException {
        // Créer un tableau à deux colonnes avec une largeur de 100 %
        Table table = new Table(UnitValue.createPercentArray(new float[]{n1, n2}));
        table.setWidth(UnitValue.createPercentValue(100)); // Largeur du tableau 100%
        table.setBorder(Border.NO_BORDER); // Pas de bordure pour le tableau
        table.setMarginTop(margiTop);
        table.setMarginLeft(marginLeft);

        // Chargement des polices
        PdfFont BOLD_FONT = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        PdfFont REGULAR_FONT = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

        // Parcourir les labels et valeurs
        for (int i = 0; i < labels.length; i++) {
            // Création et configuration du paragraphe pour le label (colonne 1)
            Paragraph labelParagraph = new Paragraph(labels[i])
                    .setFont(boldCol1?BOLD_FONT:REGULAR_FONT)
                    .setTextAlignment(textAlignment); // Ajouter des marges pour aérer

            // Création de la cellule pour le label
            Cell labelCell = new Cell().add(labelParagraph)
                    .setBorder(Border.NO_BORDER)
                    .setMarginLeft(marginLeft)
                    .setPadding(0); // Un peu d'espace à l'intérieur de la cellule

            // Création et configuration du paragraphe pour la valeur (colonne 2)
            Paragraph valueParagraph = new Paragraph(values[i])
                    .setFont(boldCol1?REGULAR_FONT:BOLD_FONT)
                    .setTextAlignment(textAlignment)
                    .setMarginLeft(10);

            // Création de la cellule pour la valeur
            Cell valueCell = new Cell().add(valueParagraph)
                    .setBorder(Border.NO_BORDER);

            // Ajouter les cellules au tableau
            table.addCell(labelCell);
            table.addCell(valueCell);
        }

        return table;
    }

    public static Image createQRCodeImage(String qrContent, float qrWidth, float qrHeight) throws IOException {
        // Générer le code QR en utilisant la bibliothèque QRGen
        ByteArrayOutputStream qrStream = QRCode.from(qrContent).to(ImageType.PNG).withSize((int) qrWidth + 10, (int) qrHeight).stream();
        byte[] qrBytes = qrStream.toByteArray();

        // Créer une image iText à partir des octets du code QR généré
        Image qrImage = new Image(ImageDataFactory.create(qrBytes));

        // Optionnel: Ajuster la taille de l'image QR dans le PDF
        qrImage.scaleToFit(qrWidth, qrHeight);

        // Retourner l'image
        return qrImage;
    }
    public static void addQRCodeToPdfFromRight(String inputPdfPath, String qrContent, float qrWidth, float qrHeight, float positionX, float positionYFromTop) throws IOException {
        // Déterminer le chemin du fichier PDF de sortie avec le code QR ajouté
        String outputPdfPath = inputPdfPath.replace(".pdf", "_QR.pdf");

        // Charger le fichier PDF existant en lecture
        PdfReader reader = new PdfReader(inputPdfPath);

        // Créer un écrivain PDF pour écrire dans le nouveau fichier PDF avec le code QR ajouté
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF à partir du lecteur et de l'écrivain
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        // Créer un document iText pour ajouter des éléments au document PDF
        Document document = new Document(pdfDoc);

        // Générer le code QR en utilisant la bibliothèque QRGen
        ByteArrayOutputStream qrStream = QRCode.from(qrContent).to(ImageType.PNG).withSize((int) qrWidth + 10, (int) qrHeight).stream();
        byte[] qrBytes = qrStream.toByteArray();

        // Créer une image du code QR à partir des octets du code QR généré
        Image qrImage = new Image(ImageDataFactory.create(qrBytes));

        // Calculer la largeur de la page et la position X
        float pageWidth = pdfDoc.getPage(1).getPageSize().getWidth();
        float adjustedPositionX = pageWidth - positionX - qrWidth; // largeur de la page moins la marge positionX

        // Calculer la position Y depuis le haut de la page
        float pageHeight = pdfDoc.getPage(1).getPageSize().getHeight();
        float adjustedPositionY = pageHeight - positionYFromTop - qrHeight; // ajuster la position en partant du haut

        // Positionner l'image du code QR selon les nouvelles coordonnées calculées
        qrImage.setFixedPosition(adjustedPositionX, adjustedPositionY);

        // Ajouter l'image du code QR au document PDF
        document.add(qrImage);

        // Fermer le document iText
        document.close();

        // Vérifier si le fichier PDF avec le code QR ajouté existe
        if (existFile(outputPdfPath)) {
            // Supprimer le fichier PDF d'entrée
            deleteFile(inputPdfPath);
            // Renommer le fichier PDF avec le code QR ajouté pour avoir le même nom que le fichier d'entrée
            renameFile(outputPdfPath, inputPdfPath);
        }
    }

    public static void addQRCodeToPdf(String inputPdfPath, String qrContent, float qrWidth, float qrHeight, float positionX, float positionYFromTop) throws IOException {
        // Déterminer le chemin du fichier PDF de sortie avec le code QR ajouté
        String outputPdfPath = inputPdfPath.replace(".pdf", "_QR.pdf");

        // Charger le fichier PDF existant en lecture
        PdfReader reader = new PdfReader(inputPdfPath);

        // Créer un écrivain PDF pour écrire dans le nouveau fichier PDF avec le code QR ajouté
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF à partir du lecteur et de l'écrivain
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        // Créer un document iText pour ajouter des éléments au document PDF
        Document document = new Document(pdfDoc);

        // Générer le code QR en utilisant la bibliothèque QRGen
        ByteArrayOutputStream qrStream = QRCode.from(qrContent).to(ImageType.PNG).withSize((int) qrWidth + 10, (int) qrHeight).stream();
        byte[] qrBytes = qrStream.toByteArray();

        // Créer une image du code QR à partir des octets du code QR généré
        Image qrImage = new Image(ImageDataFactory.create(qrBytes));

        // Calculer la position y depuis le haut de la page
        float pageHeight = pdfDoc.getPage(1).getPageSize().getHeight();
        float adjustedPositionY = pageHeight - positionYFromTop - qrHeight; // ajuster la position en partant du haut

        // Positionner l'image du code QR selon les coordonnées données
        qrImage.setFixedPosition(positionX, adjustedPositionY);

        // Ajouter l'image du code QR au document PDF
        document.add(qrImage);

        // Fermer le document iText
        document.close();

        // Vérifier si le fichier PDF avec le code QR ajouté existe
        if (existFile(outputPdfPath)) {
            // Supprimer le fichier PDF d'entrée
            deleteFile(inputPdfPath);
            // Renommer le fichier PDF avec le code QR ajouté pour avoir le même nom que le fichier d'entrée
            renameFile(outputPdfPath, inputPdfPath);
        }
    }

    public static void addImageToPdf(String inputPdfPath, String imagePath, float qrWidth, float qrHeight, float positionX, float positionYFromTop) throws IOException {
        // Déterminer le chemin du fichier PDF de sortie avec le code QR ajouté
        String outputPdfPath = inputPdfPath.replace(".pdf", "_image.pdf");

        // Charger le fichier PDF existant en lecture
        PdfReader reader = new PdfReader(inputPdfPath);

        // Créer un écrivain PDF pour écrire dans le nouveau fichier PDF avec le code QR ajouté
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF à partir du lecteur et de l'écrivain
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        // Créer un document iText pour ajouter des éléments au document PDF
        Document document = new Document(pdfDoc);

        // Créer une image à partir du fichier image spécifié
        Image image = new Image(ImageDataFactory.create(imagePath));

        // Redimensionner l'image pour correspondre aux dimensions spécifiées
        image.scaleToFit(qrWidth, qrHeight);

        // Calculer la position y depuis le haut de la page
        float pageHeight = pdfDoc.getPage(1).getPageSize().getHeight();
        float adjustedPositionY = pageHeight - positionYFromTop - image.getImageScaledHeight(); // ajuster la position en partant du haut

        // Positionner l'image selon les coordonnées spécifiées
        image.setFixedPosition(positionX, adjustedPositionY);

        // Ajouter l'image redimensionnée au document PDF
        document.add(image);

        // Fermer le document iText
        document.close();

        // Vérifier si le fichier PDF avec l'image ajoutée existe
        if (existFile(outputPdfPath)) {
            // Supprimer le fichier PDF d'entrée
            deleteFile(inputPdfPath);
            // Renommer le fichier PDF avec l'image ajoutée pour avoir le même nom que le fichier d'entrée
            renameFile(outputPdfPath, inputPdfPath);
        }
    }


    public static boolean renameFile(String oldFilePath, String newFilePath) {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (oldFile.exists()) {
            if (oldFile.renameTo(newFile)) {
                Logger.getLogger(PdfDocumentUtil.class.getName()).info("Le fichier a été renommé avec succès.");
                return true;
            } else {
                Logger.getLogger(PdfDocumentUtil.class.getName()).info("Impossible de renommer le fichier.");
                return false;
            }
        } else {
            Logger.getLogger(PdfDocumentUtil.class.getName()).info("Le fichier à renommer n'existe pas : " + oldFilePath);
            return false;
        }
    }
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        } else {
            Logger.getLogger(PdfDocumentUtil.class.getName()).info("Le fichier n'existe pas : " + filePath);
            return false;
        }
    }

    //verifier si le fichier esixte
    public static boolean existFile(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static ResponseEntity<Object> getFileResponse(Path filePathD) throws IOException {
        if (Files.exists(filePathD)) {
            Resource fileResource = new UrlResource(filePathD.toUri());
            if (fileResource.exists() || fileResource.isReadable()) {
                String contentType = "application/octet-stream";
                String headerValue = "attachment; filename=\"" + fileResource.getFilename() + "\"";
                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                        .body(fileResource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or not readable.");
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File not found or not readable. Un problème est survenu");
    }

    public static Table createImageTable(Image image,float maxWidth,float maxHeight) throws MalformedURLException {
        Table tableImage = new Table(UnitValue.createPercentArray(new float[]{1})); // 2 columns
        tableImage.setWidth(UnitValue.createPercentValue(100));
        tableImage.setMarginLeft(0);
        tableImage.setMarginTop(0);
        tableImage.setBorder(Border.NO_BORDER);
        tableImage.setKeepTogether(true);
        Cell signatureCell = new Cell().setBorder(Border.NO_BORDER);


        float width = image.getImageWidth();
        float height = image.getImageHeight();
        if (width > maxWidth || height > maxHeight) {
            float scale = Math.min(maxWidth / width, maxHeight / height);
            image.scaleAbsolute(width * scale, height * scale);
        }

        image.setHorizontalAlignment(HorizontalAlignment.CENTER); // Center the image
        signatureCell.add(image);
        tableImage.addCell(signatureCell);
        return tableImage;

    }

    public static Table createImageTable(String imagePath) throws MalformedURLException {
        Table tableImage = new Table(UnitValue.createPercentArray(new float[]{1})); // 2 columns
        tableImage.setWidth(UnitValue.createPercentValue(100));
        tableImage.setMarginLeft(0);
        tableImage.setMarginTop(0);
        tableImage.setBorder(Border.NO_BORDER);
        tableImage.setKeepTogether(true);
        Cell signatureCell = new Cell().setBorder(Border.NO_BORDER);
        Image signatureImage = new Image(ImageDataFactory.create(imagePath));

        // Restrict image dimensions to 200x200
        float maxWidth = 80;
        float maxHeight = 70;
        float width = signatureImage.getImageWidth();
        float height = signatureImage.getImageHeight();
        if (width > maxWidth || height > maxHeight) {
            float scale = Math.min(maxWidth / width, maxHeight / height);
            signatureImage.scaleAbsolute(width * scale, height * scale);
        }

        signatureImage.setHorizontalAlignment(HorizontalAlignment.CENTER); // Center the image
        signatureCell.add(signatureImage);
        tableImage.addCell(signatureCell);
        return tableImage;

    }

    public static Table createTitreWithBorder(String content, boolean isBorder, Color color, PdfFont pdfFont,int marginTop,int marginBottom, int marginLeft, float size,SolidBorder solidBorder) throws IOException {
        List<String> texts = Arrays.asList(
                content
        );

        List<PdfFont> fonts = Arrays.asList(
                pdfFont
        );
        List<Float> fontSize=Arrays.asList(size);

        return createTableTitre(texts,fonts,fontSize,TextAlignment.CENTER,marginLeft,marginLeft,marginTop,marginBottom,isBorder,1.5f,2.5f,color,solidBorder);
    }

    public static String generateHeureEdition(String nomDomaine) {
        // Obtenir la date et l'heure actuelles
        LocalDateTime now = LocalDateTime.now();

        // Formateurs pour la date et l'heure
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRENCH);

        // Formater la date et l'heure
        String formattedDay = now.format(dayFormatter);
        String formattedTime = now.format(timeFormatter);

        // Générer le texte du pied de page
        return String.format("Édité le %s à %s © %s", formattedDay, formattedTime, nomDomaine);
    }
    public static Table createSignatureTableMultiColonne(List<SignatureData> signatures, int numberOfColumns, int fontSize, float marginTop,float maxWidth,float maxHeight) throws IOException {
        // Définition des styles pour le titre et le nom
        Style titleStyle = new Style()
                .setFontSize(fontSize + 2)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        Style nameStyle = new Style()
                .setFontSize(fontSize)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER);

        // Création du tableau principal
        Table mainTable = new Table(UnitValue.createPercentArray(new float[numberOfColumns]));
        mainTable.setWidth(UnitValue.createPercentValue(100));
        mainTable.setMarginLeft(20);
        mainTable.setMarginTop(marginTop);
        mainTable.setBorder(Border.NO_BORDER);
        mainTable.setKeepTogether(false); // Autorise la répartition sur plusieurs pages

        float maxHeightEmpty = 55; // Hauteur maximale pour une cellule vide

        // Parcours de la liste des signatures
        for (int i = 0; i < signatures.size(); i++) {
            // Création d'une cellule parente pour regrouper les trois éléments (titre, signature, nom)
            Cell parentCell = new Cell().setBorder(Border.NO_BORDER);
            parentCell.setKeepTogether(true); // Garantit que les trois éléments restent sur la même page

            // 1. Ajout du titre
            parentCell.add(new Paragraph(signatures.get(i).getTitle())
                    .setFontSize(fontSize)
                    .setTextAlignment(TextAlignment.CENTER)
                    .addStyle(titleStyle));

            // 2. Ajout de la signature (image)
            if (signatures.get(i).getImagePath() != null) {
                Image signatureImage = new Image(ImageDataFactory.create(signatures.get(i).getImagePath()));
                adjustImageSize(signatureImage, maxWidth, maxHeight);
                signatureImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                signatureImage.setMarginTop(5);
                signatureImage.setMarginBottom(5);
                parentCell.add(signatureImage);
            } else {
                // Si aucune image n'est disponible, ajouter un espace vide
                Div placeholder = new Div();
                placeholder.setHeight(maxHeightEmpty);
                placeholder.add(new Paragraph(" "));
                parentCell.add(placeholder);
            }

            // 3. Ajout du nom
            parentCell.add(new Paragraph(signatures.get(i).getName())
                    .setFontSize(fontSize)
                    .setTextAlignment(TextAlignment.CENTER)
                    .addStyle(nameStyle));

            // Ajout de la cellule parente au tableau principal
            mainTable.addCell(parentCell);

            // Si on atteint le nombre de colonnes spécifié, on passe à la ligne suivante
            if ((i + 1) % numberOfColumns == 0 && (i + 1) < signatures.size()) {
                mainTable.startNewRow(); // Commencer une nouvelle ligne
            }
        }

        // Gestion des colonnes vides si le nombre de signatures n'est pas un multiple de numberOfColumns
        int remainingColumns = numberOfColumns - (signatures.size() % numberOfColumns);
        if (remainingColumns != numberOfColumns) { // Éviter d'ajouter des colonnes vides inutilement
            for (int i = 0; i < remainingColumns; i++) {
                Cell emptyCell = new Cell().setBorder(Border.NO_BORDER)
                        .add(new Paragraph(" ")); // Cellule vide
                mainTable.addCell(emptyCell);
            }
        }

        return mainTable;
    }


    public static Table createHeaderForServiceTable(String serviceDesignation,int size) throws Exception {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        PdfFont boldFont1 = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

        Table tableEntete = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        tableEntete.setMarginTop(0);
        tableEntete.setMarginBottom(20);
        tableEntete.setWidth(UnitValue.createPercentValue(100));


        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(new Date());

        tableEntete.addCell(createCellCenteredNoBorder("DIRECTION GENERALE", size, boldFont1));
        tableEntete.addCell(createCellCenteredNoBorder("", size, regularFont));
        tableEntete.addCell(createCellCenteredNoBorder("Dakar, le " + formattedDate, size+2, boldFont));
        tableEntete.addCell(createCellCenteredNoBorderLift("______________", size+5, boldFont1,15f));
        tableEntete.addCell(createCellCenteredNoBorder("", size, boldFont));
        tableEntete.addCell(createCellCenteredNoBorder("", size, regularFont));
        tableEntete.addCell(createCellCenteredNoBorder(serviceDesignation, size, boldFont1));
        tableEntete.addCell(createCellCenteredNoBorder("", size, boldFont));
        tableEntete.addCell(createCellCenteredNoBorder("", size, regularFont));

        return tableEntete;
    }
    public static Table createInfoTable(List<InfoCellDto> infos) {
        try {
            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 5}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15)
                    .setMarginTop(20);

            PdfFont fontSimple = lucidaItalic();
            PdfFont fontBold = lucidaBold();

            for (InfoCellDto info : infos) {
                Cell cellLabel = createCell(info.getLabel(), 15, false, fontBold, 1, 1, false, 5, TextAlignment.LEFT);
                Cell cellValue = createCell(info.getValue(), 14, false, fontSimple, 1, 1, false, 7, TextAlignment.LEFT);

                if (info.getTextColor() != null) {
                    cellValue.setFontColor(info.getTextColor());
                }
                if (info.getBackgroundColor() != null) {
                    cellValue.setBackgroundColor(info.getBackgroundColor());
                }

                table.addCell(cellLabel);
                table.addCell(cellValue);
            }

            return table;
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la création du tableau : " + e.getMessage(), e);
        }
    }


    public static Cell createTamponCellAvecCercles(PdfDocument pdfDoc, String texte, Color couleur) throws IOException {
        float width = 100;
        float height = 100;
        float centerX = width / 2;
        float centerY = height / 2;
        float radiusOuter = 45;
        float radiusInner = 30;

        // Crée une forme XObject
        PdfFormXObject xObject = new PdfFormXObject(new Rectangle(width, height));
        PdfCanvas canvas = new PdfCanvas(xObject, pdfDoc);

        // Dessine deux cercles
        canvas.setLineWidth(2).setStrokeColor(couleur);
        canvas.circle(centerX, centerY, radiusOuter);
        canvas.stroke();
        canvas.circle(centerX, centerY, radiusInner);
        canvas.stroke();

        // Dessiner les deux lignes horizontales encadrant le texte
        float lineOffset = 8;  // Décalage des lignes du texte, réduit pour mieux centrer
        float lineLength = radiusInner-1 ; // Réduit la longueur des lignes pour ne pas dépasser du cercle intérieur
        canvas.moveTo(centerX - lineLength, centerY - lineOffset)  // Ligne du dessus
                .lineTo(centerX + lineLength, centerY - lineOffset)
                .stroke();

        canvas.moveTo(centerX - lineLength, centerY + lineOffset)  // Ligne du dessous
                .lineTo(centerX + lineLength, centerY + lineOffset)
                .stroke();

        // Dessine le texte au centre avec le bon constructeur
        PdfFont font = lucida();
        Rectangle rect = new Rectangle(0, 0, width, height);
        Canvas drawCanvas = new Canvas(canvas, rect);
        drawCanvas.showTextAligned(
                new Paragraph(texte.toUpperCase())
                        .setFont(font)
                        .setFontSize(14)
                        .setFontColor(couleur),
                centerX, centerY,
                TextAlignment.CENTER, VerticalAlignment.MIDDLE
        );

        // Crée l'image à insérer dans la cellule
        Image image = new Image(xObject).setHorizontalAlignment(HorizontalAlignment.CENTER).setTextAlignment(TextAlignment.CENTER);
        return new Cell()
                .add(image)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    public static boolean multipartFileIsPdf(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        return contentType != null && contentType.equalsIgnoreCase("application/pdf")
                && filename != null && filename.toLowerCase().endsWith(".pdf");
    }


}