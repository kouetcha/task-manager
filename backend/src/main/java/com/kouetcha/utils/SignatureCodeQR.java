package com.kouetcha.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.colorspace.PdfColorSpace;
import com.itextpdf.kernel.pdf.colorspace.PdfDeviceCs;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.kouetcha.exception.RequestException;
import com.spire.doc.ToPdfParameterList;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.annotations.PdfAnnotation;
import com.spire.pdf.annotations.PdfWatermarkAnnotation;
import com.spire.pdf.exporting.PdfImageInfo;
import com.spire.presentation.Presentation;
import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;
import lombok.extern.slf4j.Slf4j;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.springframework.scheduling.annotation.Async;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class SignatureCodeQR {

    private static Tesseract getTesseractInstance() {
        Tesseract tess = new Tesseract();
        tess.setDatapath("media/tesseract-main_kouetcha/tesseract-main/tesseract-main/tessdata");
        tess.setLanguage("fra");
        return tess;
    }
    public static void convertFile(String LIBREOFFICE_PATH, String ext, File tempFile, File pdfFile) throws IOException, InterruptedException {
        String osName = System.getProperty("os.name").toLowerCase();

        // Vérifier si LibreOffice est bien installé
        File libreOfficeFile = new File(LIBREOFFICE_PATH);
        if (!libreOfficeFile.exists()) {
            throw new RuntimeException("❌ LibreOffice introuvable à l'emplacement : " + LIBREOFFICE_PATH);
        }

        switch (ext) {
            case "doc":
            case "docx":
            case "ppt":
            case "pptx":
            case "txt":
            case "xls":
            case "xlsx":
                executeLibreOfficeCommand(LIBREOFFICE_PATH, tempFile, pdfFile);
                break;
            default:
                throw new IllegalArgumentException("❌ Type de fichier non supporté : " + ext);
        }
    }
    private static void convertExcelToPdf(File excelFile, File pdfFile) {
        try {
            log.info("Début de la conversion Excel vers PDF");

            // Crée un objet Workbook et charge le fichier Excel
            Workbook workbook = new Workbook();
            workbook.loadFromFile(excelFile.getAbsolutePath());

            // Configuration de la conversion pour ajuster à la taille de la page
            workbook.getConverterSetting().setSheetFitToPage(true);

            // Sauvegarde du fichier PDF
            workbook.saveToFile(pdfFile.getAbsolutePath(), FileFormat.PDF);

            // Vérification que le fichier PDF a bien été créé
            if (!pdfFile.exists() || pdfFile.length() == 0) {
                throw new RequestException("La conversion n'a pas généré de fichier PDF valide");
            }

            log.info("Conversion Excel vers PDF terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la conversion Excel vers PDF: {}", e.getMessage());
            throw new RequestException("Erreur lors de la conversion Excel vers PDF: " + e.getMessage());
        }
    }

    private static void executeLibreOfficeCommand(String libreOfficePath, File inputFile, File outputFile) throws IOException, InterruptedException {
        // Créer la commande sans utiliser cmd.exe
        List<String> command = new ArrayList<>();
        command.add(libreOfficePath);
        command.add("--headless");
        command.add("--convert-to");
        command.add("pdf");
        command.add("--outdir");
        command.add(outputFile.getParent());
        command.add(inputFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        log.info("🚀 Exécution de la commande : " + String.join(" ", command));

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info("LibreOffice Output: " + line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("❌ LibreOffice a échoué avec le code : " + exitCode);
        }

        String inputFilePath = inputFile.getPath();
        String pdfFilePath = FilenameUtils.removeExtension(inputFilePath) + ".pdf";
        log.info("PDF PATH== " + pdfFilePath);
        log.info("outputFile.getPath()== " + outputFile.getPath());

        // Renommer le fichier PDF généré
        SignatureCodeQR.renameFile(pdfFilePath, outputFile.getPath());

        // Vérifier si le fichier a bien été généré
        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new RuntimeException("❌ Conversion échouée, fichier PDF vide : " + outputFile.getAbsolutePath());
        }
    }



    //Set default locale

    private static final double HISTOGRAM_TOLERANCE = 0.0;
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        } else {
            System.out.println("Le fichier n'existe pas : " + filePath);
            return false;
        }
    }

    //verifier si le fichier esixte
    public static boolean existFile(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
    public static boolean replaceFile(String oldFilePath, String newFilePath) {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (!oldFile.exists()) {
            System.out.println("Le fichier à renommer n'existe pas : " + oldFilePath);
            return false;
        }

        // Supprimer le fichier existant avec le même nom que le nouveau fichier
        if (newFile.exists()) {
            if (!newFile.delete()) {
                System.out.println("Impossible de supprimer le fichier existant : " + newFilePath);
                return false;
            }
        }

        // Renommer le fichier
        if (oldFile.renameTo(newFile)) {
            System.out.println("Le fichier a été renommé avec succès.");
            return true;
        } else {
            System.out.println("Impossible de renommer le fichier.");
            return false;
        }
    }

    //renommer le fichier
    public static boolean renameFile(String oldFilePath, String newFilePath) {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (oldFile.exists()) {
            // Si le fichier de destination existe, le supprimer
            if (newFile.exists() && !newFile.delete()) {
                System.out.println("Impossible de supprimer le fichier existant : " + newFilePath);
                return false;
            }

            // Renommer l'ancien fichier en nouveau fichier
            if (oldFile.renameTo(newFile)) {
                System.out.println("Le fichier a été renommé avec succès.");
                return true;
            } else {
                System.out.println("Impossible de renommer le fichier.");
                return false;
            }
        } else {
            System.out.println("Le fichier à renommer n'existe pas : " + oldFilePath);
            return false;
        }
    }


    //convertir l'image
    public static PDImageXObject convertImageToPDImageXObject(String imagePath) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
        return LosslessFactory.createFromImage(new PDDocument(), bufferedImage);
    }


    public static boolean iscadreExist(String filePath, String providedImagePath) {
        Locale newLocale = Locale.ROOT;
        Locale.setDefault(newLocale);
        String filePathBlanc = filePath.replace(".pdf", "_Blanc.pdf");
        String filePathSI = filePath.replace(".pdf", "_Signature.pdf");
        // String providedImagePath = "media/signature/cadre4.png";

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            int pageNum = 0; // Numéro de la page où se trouve l'image (indexée à partir de zéro)

            for (PDPage page : document.getPages()) {
                pageNum++;

                PdfDocument spireDoc = new PdfDocument();
                spireDoc.loadFromFile(filePath);
                PdfPageBase spirePage = spireDoc.getPages().get(pageNum - 1);
                PdfImageInfo[] imageInfo = spirePage.getImagesInfo();

                PDResources resources = page.getResources();

                for (COSName resourceName : resources.getXObjectNames()) {
                    if (resources.isImageXObject(resourceName)) {
                        PDImageXObject imageXObject = (PDImageXObject) resources.getXObject(resourceName);

                        COSBase cosMatrix = imageXObject.getCOSObject().getDictionaryObject(COSName.MATRIX);
                        Matrix imageMatrix = null;
                        if (cosMatrix instanceof COSArray) {
                            imageMatrix = new Matrix((COSArray) cosMatrix);
                        }

                        float x = 0;
                        float y = 0;

                        PDImageXObject pdImageXObject = convertImageToPDImageXObject(providedImagePath);
                        ImageInfo info = findMatchingImageInfo1(pdImageXObject, imageInfo);

                        if (info == null) {
                            return false;
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public static int nombrecadreExistant(String filePath, String providedImagePath) {
        Locale newLocale = Locale.ROOT;
        Locale.setDefault(newLocale);
        int nombre = 0;

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            int pageNum = 0; // Numéro de la page où se trouve l'image (indexée à partir de zéro)

            for (PDPage page : document.getPages()) {
                pageNum++;

                PdfDocument spireDoc = new PdfDocument();
                spireDoc.loadFromFile(filePath);
                PdfPageBase spirePage = spireDoc.getPages().get(pageNum - 1);
                PdfImageInfo[] imageInfo = spirePage.getImagesInfo();

                PDResources resources = page.getResources();

                for (COSName resourceName : resources.getXObjectNames()) {
                    if (resources.isImageXObject(resourceName)) {
                        PDImageXObject imageXObject = (PDImageXObject) resources.getXObject(resourceName);

                        COSBase cosMatrix = imageXObject.getCOSObject().getDictionaryObject(COSName.MATRIX);
                        Matrix imageMatrix = null;
                        if (cosMatrix instanceof COSArray) {
                            imageMatrix = new Matrix((COSArray) cosMatrix);
                        }

                        float x = 0;
                        float y = 0;

                        PDImageXObject pdImageXObject = convertImageToPDImageXObject(providedImagePath);
                        ImageInfo info = findMatchingImageInfo1(pdImageXObject, imageInfo);

                        if (info != null) {
                            nombre += 1;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nombre;
    }

    public static List<String> splitPdfIntoMultiplePdfs(String filePath) throws IOException {
        List<String> splitPdfPaths = new ArrayList<>();

        // Charger le document PDF
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            int pageCount = document.getNumberOfPages();

            if (pageCount == 1) {
                // Si le PDF contient une seule page, copier le fichier sans suffixe "_part_1.pdf"
                String singlePagePdfPath = filePath.replace(".pdf", "_part_1.pdf");
                document.save(singlePagePdfPath);
                splitPdfPaths.add(singlePagePdfPath);
            } else {
                // Sinon, diviser le PDF en parties
                Splitter splitter = new Splitter();
                List<PDDocument> splitDocuments = splitter.split(document);

                Iterator<PDDocument> iterator = splitDocuments.iterator();
                int i = 1;
                while (iterator.hasNext()) {
                    PDDocument splitDocument = iterator.next();

                    // Enregistrer le document séparé dans un fichier
                    String fileName = new File(filePath).getName();
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String splitPdfPath = filePath.replace(fileName, baseName + "_part_" + i + ".pdf");

                    splitDocument.save(splitPdfPath);
                    splitPdfPaths.add(splitPdfPath);

                    // Fermer le document séparé
                    splitDocument.close();
                    i++;
                }
            }
        }

        return splitPdfPaths;
    }


    public static List<Coordinates> getCoordinatesNew(String filePath, String providedImagePath) {
        Locale.setDefault(Locale.ROOT);
        List<Coordinates> coordinatesList = new ArrayList<>();
        Set<String> seenCoordinates = new HashSet<>();
        Map<Integer, PdfImageInfo[]> imageCache = new HashMap<>();

        // Créer un fichier temporaire
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp_pdf_", ".pdf");
            Files.copy(new File(filePath).toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Supprimer les filigranes
            removeWatermarks(tempFile.getAbsolutePath(), tempFile.getAbsolutePath());

            try (PDDocument document = PDDocument.load(tempFile)) {
                PdfDocument spireDoc = new PdfDocument();
                spireDoc.loadFromFile(tempFile.getAbsolutePath());

                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                List<Callable<Void>> tasks = new ArrayList<>();

                for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
                    final int pageIndex = pageNum;
                    tasks.add(() -> {
                        PDPage page = document.getPage(pageIndex);
                        PdfPageBase spirePage = spireDoc.getPages().get(pageIndex);
                        PdfImageInfo[] imageInfo;

                        // Caching image info for pages
                        if (!imageCache.containsKey(pageIndex)) {
                            imageInfo = spirePage.getImagesInfo();  // Cache image info per page
                            imageCache.put(pageIndex, imageInfo);
                        } else {
                            imageInfo = imageCache.get(pageIndex);
                        }

                        PDImageXObject pdImageXObject = convertImageToPDImageXObject(providedImagePath);
                        List<ImageInfo> matchingImageInfos = findMatchingImageInfo(pdImageXObject, imageInfo);

                        if (matchingImageInfos.isEmpty()) {
                            matchingImageInfos = findMatchingImageInfoXls(pdImageXObject, imageInfo);
                        }

                        matchingImageInfos.stream()
                                .filter(info -> !seenCoordinates.contains(pageIndex + "-" + info.x + "-" + info.y))
                                .forEach(info -> {
                                    coordinatesList.add(new Coordinates(pageIndex + 1, info.x, info.y,
                                            (float) (3 + (3 / 4.0) * info.width),
                                            (float) (3 + (3 / 4.0) * info.height) - 1));

                                    seenCoordinates.add(pageIndex + "-" + info.x + "-" + info.y);
                                });

                        return null;
                    });
                }

                executor.invokeAll(tasks);
                executor.shutdown();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Supprimer le fichier temporaire
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        return coordinatesList;
    }




    public static List<Coordinates> getCoordinates(String filePath, PDImageXObject pdImageXObject) {

        try {
            BufferedImage referenceImage = pdImageXObject.getImage();
            log.info("referenceImage : "+referenceImage.getHeight());
        }
        catch (Exception e){
          e.printStackTrace();
        }

        Locale.setDefault(Locale.ROOT);
        List<Coordinates> coordinatesList = new ArrayList<>();
        Set<String> seenCoordinates = new HashSet<>();
        Map<Integer, PdfImageInfo[]> imageCache = new HashMap<>();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PdfDocument spireDoc = new PdfDocument();
            spireDoc.loadFromFile(filePath);

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Callable<Void>> tasks = new ArrayList<>();
            log.info("document.getNumberOfPages() "+document.getNumberOfPages());
            for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
                final int pageIndex = pageNum;
                tasks.add(() -> {
                    PdfPageBase spirePage = spireDoc.getPages().get(pageIndex);
                    PdfImageInfo[] imageInfo;

                    // Caching image info for pages
                    if (!imageCache.containsKey(pageIndex)) {
                        imageInfo = Arrays.stream(spirePage.getImagesInfo())
                                .filter(pdfImageInfo -> pdfImageInfo.getImage().getHeight() < 500) // Filtrer les images dont la hauteur est inférieure à 500
                                .toArray(PdfImageInfo[]::new);
                        imageCache.put(pageIndex, imageInfo);
                    } else {
                        imageInfo = imageCache.get(pageIndex);
                    }
                    BufferedImage referenceImage = pdImageXObject.getImage();
                    log.info("referenceImage : "+referenceImage.getHeight());
                    List<ImageInfo> matchingImageInfos = findMatchingImageInfoByOCR(referenceImage, imageInfo);
                   // List<ImageInfo> matchingImageInfos = findMatchingImageInfo(pdImageXObject, imageInfo);

                    if (matchingImageInfos.isEmpty()) {
                        matchingImageInfos  = findMatchingImageInfo(pdImageXObject, imageInfo);
                    }
                    if (matchingImageInfos.isEmpty()) {
                        matchingImageInfos = findMatchingImageInfoXls(pdImageXObject, imageInfo);
                    }

                    matchingImageInfos.stream()
                            .filter(info -> !seenCoordinates.contains(pageIndex + "-" + info.x + "-" + info.y))
                            .forEach(info -> {
                                coordinatesList.add(new Coordinates(pageIndex + 1, info.x, info.y,
                                        (float) (3 + (3 / 4.0) * info.width),
                                        (float) (3 + (3 / 4.0) * info.height) - 1));

                                seenCoordinates.add(pageIndex + "-" + info.x + "-" + info.y);
                            });

                    return null;
                });
            }

            executor.invokeAll(tasks);
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return coordinatesList;
    }




    //ajouter une signature au fichier

    public static String addSignatureAllPage(String filePath, String providedImagePath, byte[] imagePath) throws IOException {
        Locale newLocale = Locale.ROOT;
        Locale.setDefault(newLocale);
        String filePathBlanc = filePath.replace(".pdf", "_Blanc.pdf");
        String filePathSI = filePath.replace(".pdf", "_Signature.pdf");
        List<Coordinates> coordinatesList = getCoordinatesNew(filePath, providedImagePath);

        for (Coordinates coordinates : coordinatesList) {
            addBlankImageToPage(filePath, filePathBlanc, coordinates.getPageNum(), coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight() );
            addImageByteToPdf(filePathBlanc, filePathSI, imagePath, coordinates.getPageNum(), coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight() - 1);
            if (existFile(filePathBlanc) && existFile(filePathSI)) {
                deleteFile(filePathBlanc);
                deleteFile(filePath);
                renameFile(filePathSI, filePath);
            }
            System.out.println("Page: " + coordinates.getPageNum());
            System.out.println("Coordonnées (x, y): (" + coordinates.getX() + ", " + coordinates.getY() + ")");
            System.out.println("Dimensions (largeur, hauteur): (" + coordinates.getWidth() + ", " + coordinates.getHeight() + ")");
            System.out.println("------");
        }



        return filePath;
    }
    public static List<Coordinates> retournerListDesCoordonnees(String filePath, String providedImagePath, Integer pageNumber)  {
        try {
            long startTime = System.currentTimeMillis();  // Temps de début

            // Diviser le document PDF en parties
            List<String> splitPdfPaths = SignatureCodeQR.splitPdfIntoMultiplePdfs(filePath);
            int pdfsize = splitPdfPaths.size();

            // Liste pour stocker les coordonnées globales
            List<Coordinates> coordinatesListGlobal = new ArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, pdfsize / 4)); // Utilisation de 10 threads de travail

            List<Future<List<Coordinates>>> futures = new ArrayList<>();
            int numPage = 1;

            try {
                // Soumettre les tâches pour chaque document PDF séparé
                for (String splitPdfPath : splitPdfPaths) {
                    // Vérifier si la page spécifique est fournie, sinon traiter toutes les pages
                    if (pageNumber == null || pageNumber == numPage) {
                        final int currentPageNumber = numPage;  // Capturer le numéro de page dans une variable finale pour l'utiliser dans la lambda

                        Callable<List<Coordinates>> task = () -> {
                            PDImageXObject pdImageXObject = SignatureCodeQR.convertImageToPDImageXObject(providedImagePath);

                            List<Coordinates> coordinatesList = SignatureCodeQR.getCoordinates(splitPdfPath, pdImageXObject);
                            if (!coordinatesList.isEmpty()) {
                                for (Coordinates coordinates : coordinatesList) {
                                    coordinates.setPageNum(currentPageNumber);
                                }
                            }
                            return coordinatesList;
                        };

                        // Soumettre la tâche à l'executor
                        futures.add(executor.submit(task));
                    }
                    numPage++;
                }

                // Attendre que toutes les tâches soient terminées et récupérer les résultats
                for (Future<List<Coordinates>> future : futures) {
                    try {
                        List<Coordinates> coordinatesList = future.get();
                        coordinatesListGlobal.addAll(coordinatesList);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }
                }

            } finally {
                // Supprimer tous les fichiers temporaires, sauf le document final
                for (String tempFilePath : splitPdfPaths) {
                    if (!tempFilePath.equals(filePath)) {
                        SignatureCodeQR.deleteFile(tempFilePath);
                    }
                }
                // Fermer l'executor
                executor.shutdown();
                long endTime = System.currentTimeMillis();  // Temps de fin
                long duration = endTime - startTime;  // Calculer la durée

                System.out.println("Durée d'exécution : " + duration + " millisecondes");
            }

            return coordinatesListGlobal;
        }
        catch (Exception e){
          throw new IllegalArgumentException(e.getMessage());
        }
    }
    public static String addSignatureAllPageNew(String filePath, String providedImagePath, byte[] imagePath,List<Coordinates>coordinatesList) {
       try {


        Locale newLocale = Locale.ROOT;
        Locale.setDefault(newLocale);
        String filePathSI = filePath.replace(".pdf", "_Signature.pdf");


        for (Coordinates coordinates : coordinatesList) {
            addImageByteToPdf(filePath, filePathSI, imagePath, coordinates.getPageNum(), coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight() - 1,providedImagePath);
            deleteFile(filePath);
            renameFile(filePathSI, filePath);
            System.out.println("Page " + coordinates.getPageNum() + " : Num" + coordinates.getPageNum()+" X: "+coordinates.getX()+" Y: "+coordinates.getY());

        }

        return filePath;}
       catch (Exception e){
        throw new IllegalArgumentException(e.getMessage());
       }
    }
    public static void addImageByteToPdf(String inputPdfPath, String outputPdfPath, byte[] imagePath, int pageNumber, float x, float y, float width, float height, String providedImagePath) throws IOException {
        // Charger le fichier PDF existant
        supprimerCadre(inputPdfPath,providedImagePath,pageNumber);
        PdfReader reader = new PdfReader(inputPdfPath);
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Récupérer la page spécifiée
        PdfPage page = pdfDoc.getPage(pageNumber);

        // Créer une instance de l'image à partir du chemin spécifié
        Image image = new Image(ImageDataFactory.create(imagePath));

        // Obtenir les dimensions de l'image
        float currentWidth = image.getImageWidth();
        float currentHeight = image.getImageHeight();

        // Calculer l'échelle en fonction de la contrainte de hauteur
        float scaleHeight = height / currentHeight;
        float scaleWidth = scaleHeight; // Assure le maintien des proportions de l'image

        // Ajuster les dimensions de l'image pour respecter la contrainte de hauteur
        float newWidth = currentWidth * scaleWidth;
        float newHeight = currentHeight * scaleHeight;
        if(newWidth>width){
            newWidth=width;
        }

        // Positionner l'image à la position spécifiée et définir la largeur et la hauteur
        image.scaleToFit(newWidth, newHeight);
        image.setPageNumber(pageNumber);
        float p = (width - newWidth) / 4;
        image.setFixedPosition(p + x, page.getMediaBox().getHeight() - y - newHeight);

        // Ajouter l'image à la page spécifiée du document PDF
        document.add(image);

        // Fermer le document
        document.close();
    }

    // Méthode pour fusionner les fichiers PDF
    public static void mergePdfFiles(List<String> filePaths, String outputFilePath) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFilePath)) {
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            pdfMerger.setDestinationStream(outputStream);

            // Ajouter tous les fichiers à fusionner
            for (String filePath : filePaths) {
                pdfMerger.addSource(new File(filePath));
            }

            // Fusionner les fichiers
            pdfMerger.mergeDocuments(null);
        }
    }

    public static List<String> splitPdfIntoMultiplePdfs1(String filePath) throws IOException {
        List<String> splitPdfPaths = new ArrayList<>();
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            int pageCount = document.getNumberOfPages();

            if (pageCount == 1) {
                // Si le PDF contient une seule page, copier le fichier sans suffixe "_part_1.pdf"
                String singlePagePdfPath = filePath.replace(".pdf", "_part_1.pdf");
                document.save(new File(singlePagePdfPath));
                splitPdfPaths.add(singlePagePdfPath);
            } else {
                // Sinon, diviser le PDF en parties
                String fileName = new File(filePath).getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

                for (int i = 0; i < pageCount; i++) {
                    try (PDDocument splitDocument = new PDDocument()) {
                        PDPage page = document.getPage(i);
                        splitDocument.addPage(page);

                        String splitPdfPath = filePath.replace(fileName, baseName + "_part_" + (i + 1) + ".pdf");
                        splitDocument.save(new File(splitPdfPath));
                        splitPdfPaths.add(splitPdfPath);
                    }
                }
            }
        }

        return splitPdfPaths;
    }


    public static String addSignature(String filePath, String providedImagePath,byte[] imagePath) {
        Locale newLocale = Locale.ROOT;
        Locale.setDefault(newLocale);
        String filePathBlanc = filePath.replace(".pdf", "_Blanc.pdf");
        String filePathSI = filePath.replace(".pdf", "_Signature.pdf");
       // String providedImagePath = "media/signature/cadre4.png";

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            int pageNum = 0; // Numéro de la page où se trouve l'image (indexée à partir de zéro)

            for (PDPage page : document.getPages()) {
                pageNum++;

                PdfDocument spireDoc = new PdfDocument();
                spireDoc.loadFromFile(filePath);
                PdfPageBase spirePage = spireDoc.getPages().get(pageNum - 1);
                PdfImageInfo[] imageInfo = spirePage.getImagesInfo();
                System.out.println("Images:" + (imageInfo.length));
                PDResources resources = page.getResources();

                for (COSName resourceName : resources.getXObjectNames()) {
                    if (resources.isImageXObject(resourceName)) {
                        PDImageXObject imageXObject = (PDImageXObject) resources.getXObject(resourceName);

                        COSBase cosMatrix = imageXObject.getCOSObject().getDictionaryObject(COSName.MATRIX);
                        Matrix imageMatrix = null;
                        if (cosMatrix instanceof COSArray) {
                            imageMatrix = new Matrix((COSArray) cosMatrix);
                        }

                        float x = 0;
                        float y = 0;

                        PDImageXObject pdImageXObject = convertImageToPDImageXObject(providedImagePath);
                        ImageInfo info = findMatchingImageInfo1(pdImageXObject, imageInfo);

                        if (info != null) {

                            x = info.x;
                            y = info.y;
                            float w = (float) (3 + (3 / 4.0) * info.width);
                            float h = (float) (3 + (3 / 4.0) * info.height)-1;
                            System.out.println("Image noire détectée sur la page " + pageNum + ":");
                            System.out.println("Position (x, y) : (" + x + ", " + y + ")");
                            System.out.println("Dimensions (largeur, hauteur) : (" + w + ", " + h + ")");

                            // addQRCodeToPdf(filePath, filePathSI,imagePath,width , height);
                            addBlankImageToPage(filePath, filePathBlanc, pageNum, x, y, w, h);
                            addImageByteToPdf(filePathBlanc, filePathSI, imagePath, pageNum, x, y, w, h-1);
                            x = 0;
                            y = 0;
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(existFile(filePathBlanc)&& existFile(filePathSI))
        {
            deleteFile(filePathBlanc);
            deleteFile(filePath);
            renameFile(filePathSI,filePath);
        }

        return filePath;
    }

    static class ImageInfo {
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public ImageInfo(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }
    }

    private static ImageInfo findMatchingImageInfo1(PDImageXObject imageXObject, PdfImageInfo[] imageInfo) {
        for (PdfImageInfo info : imageInfo) {
            Rectangle2D.Float bounds = (Rectangle2D.Float) info.getBounds();

            float imageX = bounds.x; // Coordonnée X du coin supérieur gauche de l'image
            float imageY = bounds.y; // Coordonnée Y du coin supérieur gauche de l'image
            float imageWidth = (float) ((4 / 3.0) * bounds.width); // Largeur de l'image
            float imageHeight = (float) ((4 / 3.0) * bounds.height); // Hauteur de l'image

            // Comparer les dimensions de l'imageXObject avec celles de l'élément de imageInfo
            // Si elles correspondent, retourner les coordonnées et les dimensions mises à jour
            if (Math.abs(imageXObject.getWidth() - imageWidth) <=1) {
                return new ImageInfo(imageX, imageY, imageWidth, imageHeight);
            }

        }
        // Si aucune correspondance n'est trouvée, retourner null
        return null;
    }

    private static List<ImageInfo> findMatchingImageInfo(PDImageXObject imageXObject, PdfImageInfo[] imageInfo) {
        List<ImageInfo> matchingInfos = new ArrayList<>();
        float imageXObjectWidth = imageXObject.getWidth(); // Pré-calcul de la largeur de l'image
        float imageXObjectHeight = imageXObject.getHeight(); // Pré-calcul de la hauteur de l'image

        // Calculez les dimensions de l'imageXObject une seule fois
        float tolerance = 0.1f;

        for (PdfImageInfo info : imageInfo) {
            Rectangle2D.Float bounds = (Rectangle2D.Float) info.getBounds();
            float imageX = bounds.x;
            float imageY = bounds.y;
            float imageWidth = (float) ((4 / 3.0) * bounds.width);  // Largeur de l'image
            float imageHeight = (float) ((4 / 3.0) * bounds.height);  // Hauteur de l'image

            // Comparer les dimensions de l'image avec une tolérance
            if (Math.abs(imageXObjectWidth - imageWidth) <= tolerance && Math.abs(imageXObjectHeight - imageHeight) <= tolerance) {
                matchingInfos.add(new ImageInfo(imageX, imageY, imageWidth, imageHeight));
            }
        }
        return matchingInfos;
    }

    public static List<ImageInfo> findMatchingImageInfoXls(PDImageXObject imageXObject, PdfImageInfo[] imageInfo) throws IOException {
        List<ImageInfo> matchingInfos = new ArrayList<>();

        // Convertir l'imageXObject en niveaux de gris une seule fois
        BufferedImage grayImageXObject = convertToGrayscale(imageXObject.getImage());
        float[] histogramXObject = getGrayHistogram(grayImageXObject);

        // Cache pour les histogrammes des images extraites
        Map<PdfImageInfo, float[]> histogramCache = new HashMap<>();
        Map<PdfImageInfo, BufferedImage> imageCache = new HashMap<>();

        for (PdfImageInfo info : imageInfo) {
            Rectangle2D.Float bounds = (Rectangle2D.Float) info.getBounds();
            float imageWidth1 = (float) ((4 / 3.0) * bounds.width);

            // Si l'histogramme pour cette image a déjà été calculé, réutiliser
            float[] histogramFromInfo = histogramCache.get(info);
            BufferedImage imageFromInfo = imageCache.get(info);

            // Si ce n'est pas le cas, extraire l'image et calculer l'histogramme
            if (histogramFromInfo == null || imageFromInfo == null) {
                imageFromInfo = extractImageFromPdfImageInfo(info);  // Cette méthode doit être optimisée pour éviter la recréation inutile
                BufferedImage grayImageFromInfo = convertToGrayscale(imageFromInfo);
                histogramFromInfo = getGrayHistogram(grayImageFromInfo);

                // Mise en cache des résultats
                imageCache.put(info, imageFromInfo);
                histogramCache.put(info, histogramFromInfo);
            }

            // Comparer les histogrammes avec une tolérance
            if (areImagesSameColor(info.getImage(), imageXObject.getImage())) {
                float imageX = bounds.x;
                float imageY = bounds.y;
                float imageWidth = (float) ((4 / 3.0) * bounds.width);
                float imageHeight = (float) ((4 / 3.0) * bounds.height);
                matchingInfos.add(new ImageInfo(imageX, imageY, imageWidth, imageHeight));
            }
        }

        return matchingInfos;
    }

    private static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(image, grayImage);
        return grayImage;
    }

    private static float[] getGrayHistogram(BufferedImage image) {
        int totalPixels = image.getWidth() * image.getHeight();
        float[] normalizedHistogram = new float[256]; // Histogramme normalisé de niveaux de gris de 0 à 255
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int gray = image.getRaster().getSample(x, y, 0);
                normalizedHistogram[gray] += 1.0 / totalPixels; // Normalisation
            }
        }
        return normalizedHistogram;
    }

    public static boolean areImagesSameColor(BufferedImage img1, BufferedImage img2) {
        double[] avgColor1 = getAvgLabColor(img1);
        double[] avgColor2 = getAvgLabColor(img2);

        // Normalisation des couleurs en fonction du nombre de pixels
        int numPixels1 = img1.getWidth() * img1.getHeight();
        int numPixels2 = img2.getWidth() * img2.getHeight();

        double normAvgA1 = avgColor1[1] * numPixels1;
        double normAvgB1 = avgColor1[2] * numPixels1;

        double normAvgA2 = avgColor2[1] * numPixels2;
        double normAvgB2 = avgColor2[2] * numPixels2;

        // Comparaison des composantes a et b de LAB
        double deltaA = Math.abs(normAvgA1 - normAvgA2);
        double deltaB = Math.abs(normAvgB1 - normAvgB2);

        // Tolerance à ajuster selon votre besoin
        double tolerance = 0.0001;

        // Vérification de la similarité des couleurs
        return deltaA <= tolerance && deltaB <= tolerance;
    }

    private static double[] getAvgLabColor(BufferedImage img) {
        double sumL = 0, sumA = 0, sumB = 0;
        int numPixels = img.getWidth() * img.getHeight();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                double[] lab = rgbToLab(rgb);
                sumL += lab[0];
                sumA += lab[1];
                sumB += lab[2];
            }
        }

        double avgL = sumL / numPixels;
        double avgA = sumA / numPixels;
        double avgB = sumB / numPixels;

        return new double[]{avgL, avgA, avgB};
    }

    private static double[] rgbToLab(int rgb) {
        // Extraire les composantes R, G et B de l'entier RGB
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // Conversion de RGB vers XYZ
        double rLinear = gammaCorrection(red / 255.0);
        double gLinear = gammaCorrection(green / 255.0);
        double bLinear = gammaCorrection(blue / 255.0);

        double x = rLinear * 0.4124564 + gLinear * 0.3575761 + bLinear * 0.1804375;
        double y = rLinear * 0.2126729 + gLinear * 0.7151522 + bLinear * 0.0721750;
        double z = rLinear * 0.0193339 + gLinear * 0.1191920 + bLinear * 0.9503041;

        // Conversion de XYZ vers LAB
        x /= 0.950456;
        y /= 1.0;
        z /= 1.088754;

        x = pivotXyz(x);
        y = pivotXyz(y);
        z = pivotXyz(z);

        double l = Math.max(0, 116 * y - 16);
        double a = 500 * (x - y);
        double b = 200 * (y - z);

        return new double[]{l, a, b};
    }

    private static double gammaCorrection(double color) {
        if (color <= 0.04045) {
            return color / 12.92;
        } else {
            return Math.pow((color + 0.055) / 1.055, 2.4);
        }
    }

    private static double pivotXyz(double value) {
        if (value > 0.008856) {
            return Math.pow(value, 1.0 / 3.0);
        } else {
            return 7.787037 * value + 16.0 / 116.0;
        }
    }


    private static boolean compareHistograms(float[] hist1, float[] hist2) {
        double sum1 = 0, sum2 = 0, sumMin = 0;
        for (int i = 0; i < hist1.length; i++) {
            sum1 += hist1[i];
            sum2 += hist2[i];
            sumMin += Math.min(hist1[i], hist2[i]);
        }
        double similarity = sumMin / Math.min(sum1, sum2);
        System.out.println("similarity="+similarity);
        return similarity >= (1.0 - HISTOGRAM_TOLERANCE);
    }
    private static BufferedImage extractImageFromPdfImageInfo(PdfImageInfo info) throws IOException {
        // Supposons que PdfImageInfo a une méthode getImageData() qui retourne les données d'image en bytes

        BufferedImage image = info.getImage();
        return image;
    }

    // ajouter une image en pdf
    public static void addImageToPdf(String inputPdfPath, String outputPdfPath, String imagePath, int pageNumber, float x, float y, float width, float height) throws IOException {
        // Charger le fichier PDF existant
        PdfReader reader = new PdfReader(inputPdfPath);
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Récupérer la page spécifiée
        PdfPage page = pdfDoc.getPage(pageNumber);

        // Créer une instance de l'image à partir du chemin spécifié
        Image image = new Image(ImageDataFactory.create(imagePath));

        // Obtenir les dimensions de l'image
        float currentWidth = image.getImageWidth();
        float currentHeight = image.getImageHeight();

        // Calculer l'échelle en fonction de la contrainte de hauteur
        float scaleHeight = height / currentHeight;
        float scaleWidth = scaleHeight; // Assure le maintien des proportions de l'image

        // Ajuster les dimensions de l'image pour respecter la contrainte de hauteur
        float newWidth = currentWidth * scaleWidth;
        float newHeight = currentHeight * scaleHeight;

        // Positionner l'image à la position spécifiée et définir la largeur et la hauteur
        image.scaleToFit(newWidth, newHeight);
        image.setPageNumber(pageNumber);
        float p = (width - newWidth) / 4;
        image.setFixedPosition(p + x, page.getMediaBox().getHeight() - y - newHeight);

        // Ajouter l'image à la page spécifiée du document PDF
        document.add(image);

        // Fermer le document
        document.close();
    }


    public static void addImageByteToPdf(String inputPdfPath, String outputPdfPath, byte[] imagePath, int pageNumber, float x, float y, float width, float height) throws IOException {
        // Charger le fichier PDF existant
        PdfReader reader = new PdfReader(inputPdfPath);
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Récupérer la page spécifiée
        PdfPage page = pdfDoc.getPage(pageNumber);

        // Créer une instance de l'image à partir du chemin spécifié
        Image image = new Image(ImageDataFactory.create(imagePath));

        // Obtenir les dimensions de l'image
        float currentWidth = image.getImageWidth();
        float currentHeight = image.getImageHeight();

        // Calculer l'échelle en fonction de la contrainte de hauteur
        float scaleHeight = height / currentHeight;
        float scaleWidth = scaleHeight; // Assure le maintien des proportions de l'image

        // Ajuster les dimensions de l'image pour respecter la contrainte de hauteur
        float newWidth = currentWidth * scaleWidth;
        float newHeight = currentHeight * scaleHeight;
        if(newWidth>width){
            newWidth=width;
        }

        // Positionner l'image à la position spécifiée et définir la largeur et la hauteur
        image.scaleToFit(newWidth, newHeight);
        image.setPageNumber(pageNumber);
        float p = (width - newWidth) / 4;
        image.setFixedPosition(p + x, page.getMediaBox().getHeight() - y - newHeight);

        // Ajouter l'image à la page spécifiée du document PDF
        document.add(image);

        // Fermer le document
        document.close();
    }
    public static void addImageByteToPdfAndRemove(String inputPdfPath, String outputPdfPath, byte[] imagePath, int pageNumber, float x, float y, float width, float height) throws IOException {
        // Charger le fichier PDF existant
        PdfReader reader = new PdfReader(inputPdfPath);
        PdfWriter writer = new PdfWriter(outputPdfPath);

        // Créer un nouveau document PDF
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Récupérer la page spécifiée
        PdfPage page = pdfDoc.getPage(pageNumber);

        // Créer une instance de l'image à partir du chemin spécifié
        Image image = new Image(ImageDataFactory.create(imagePath));

        // Obtenir les dimensions de l'image
        float currentWidth = image.getImageWidth();
        float currentHeight = image.getImageHeight();

        // Calculer l'échelle en fonction de la contrainte de hauteur
        float scaleHeight = height / currentHeight;
        float scaleWidth = scaleHeight; // Assure le maintien des proportions de l'image

        // Ajuster les dimensions de l'image pour respecter la contrainte de hauteur
        float newWidth = currentWidth * scaleWidth;
        float newHeight = currentHeight * scaleHeight;
        if(newWidth>width){
            newWidth=width;
        }

        // Positionner l'image à la position spécifiée et définir la largeur et la hauteur
        image.scaleToFit(newWidth, newHeight);
        image.setPageNumber(pageNumber);
        float p = (width - newWidth) / 4;
        image.setFixedPosition(p + x, page.getMediaBox().getHeight() - y - newHeight);

        // Ajouter l'image à la page spécifiée du document PDF
        document.add(image);

        // Fermer le document
        document.close();
    }

    public static void processDocxImages(String filePath) {
        try {
            XWPFDocument doc = new XWPFDocument(new FileInputStream(filePath));

            // Parcourt les paragraphes
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                // Obtient les images dans chaque paragraphe
                List<XWPFPicture> pictures = paragraph.getRuns().get(0).getEmbeddedPictures();
                // Parcourt les images dans chaque paragraphe
                for (XWPFPicture picture : pictures) {
                    // Obtient les informations sur l'image
                    XWPFPictureData pictureData = picture.getPictureData();
                    byte[] imageData = pictureData.getData();
                    String imageFilename = pictureData.getFileName();

                    // Faites ce que vous avez à faire avec l'image ici
                    // Par exemple, vérifiez si l'image est un rectangle noir et récupérez ses coordonnées et dimensions
                    if (isDarkRectangleWord(imageData)) {
                        // Obtient les coordonnées et les dimensions de l'image
                        long width = picture.getCTPicture().getSpPr().getXfrm().getExt().getCx();
                        long height = picture.getCTPicture().getSpPr().getXfrm().getExt().getCy();
                        long x = (long) picture.getCTPicture().getSpPr().getXfrm().getOff().getX();
                        long y = (long) picture.getCTPicture().getSpPr().getXfrm().getOff().getY();

                        // Faites quelque chose avec les informations récupérées (par exemple, imprimez-les)
                        System.out.println("Image noire détectée :");
                        System.out.println("Nom du fichier : " + imageFilename);
                        System.out.println("Position (x, y) : (" + x + ", " + y + ")");
                        System.out.println("Dimensions (largeur, hauteur) : (" + width + ", " + height + ")");
                    }
                }
            }

            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ajouter une image a la page
    public static void addBlankImageToPage(String inputPdfPath, String outputPdfPath, int pageNumber, float x, float y, float width, float height) throws IOException {
        // Ouvrir le document PDF existant
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
        System.out.println("Dans blanc Hauteur:" + height);
        // Créer un espace colorimétrique RGB
        PdfColorSpace colorSpace = new PdfDeviceCs.Rgb();

        // Créer une couleur blanche dans l'espace colorimétrique RGB
        com.itextpdf.kernel.colors.Color whiteColor = new DeviceRgb(255, 255, 255);
        PdfPage page = pdfDoc.getPage(pageNumber);
        // Dessiner un rectangle blanc de la taille spécifiée sur la page spécifiée
        PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(pageNumber));
        canvas.saveState()
                .setFillColor(whiteColor)
                .rectangle(x - 2, page.getMediaBox().getHeight() - y - height + 2, width, height)
                .fill()
                .restoreState();

        // Fermer le document
        pdfDoc.close();
    }

    private static boolean isDarkRectangle(BufferedImage image) {
        // Largeur et hauteur de l'image
        int width = image.getWidth();
        int height = image.getHeight();

        // Vérifier la couleur du coin supérieur gauche
        Color colorUpperLeft = new Color(image.getRGB(0, 0));

        // Trouver l'épaisseur du contour
        int contourThickness = 0;

        // Vérifier le contour supérieur
        for (int y = 0; y < height; y++) {
            if (!new Color(image.getRGB(0, y)).equals(colorUpperLeft)) {
                contourThickness = y;
                break;
            }
        }

        // Vérifier le contour inférieur
        for (int y = height - 1; y >= 0; y--) {
            if (!new Color(image.getRGB(0, y)).equals(colorUpperLeft)) {
                contourThickness = Math.max(contourThickness, height - y - 1);
                break;
            }
        }

        // Vérifier le contour gauche
        for (int x = 0; x < width; x++) {
            if (!new Color(image.getRGB(x, 0)).equals(colorUpperLeft)) {
                contourThickness = Math.max(contourThickness, x);
                break;
            }
        }

        // Vérifier le contour droit
        for (int x = width - 1; x >= 0; x--) {
            if (!new Color(image.getRGB(x, 0)).equals(colorUpperLeft)) {
                contourThickness = Math.max(contourThickness, width - x - 1);
                break;
            }
        }

        // Calculer le nombre total de pixels sur le contour
        int contourPixelCount = (contourThickness * 2 + (width + height - 2) * 2);

        // Calculer le nombre de pixels noirs sur le contour
        int blackPixelCount = 0;
        for (int x = 0; x < contourThickness && x < width; x++) {

            for (int y = 0; y < height; y++) {
                if (new Color(image.getRGB(x, y)).equals(colorUpperLeft)) {
                    blackPixelCount++;
                }
            }
        }
        for (int x = Math.max(0, width - contourThickness); x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (new Color(image.getRGB(x, y)).equals(colorUpperLeft)) {
                    blackPixelCount++;
                }
            }
        }
        for (int y = contourThickness; y < height - contourThickness; y++) {
            for (int x = 0; x < width; x++) {
                if (new Color(image.getRGB(x, y)).equals(colorUpperLeft)) {
                    blackPixelCount++;
                }
            }
        }

        // Calculer le pourcentage de pixels noirs sur le contour
        double blackPercentage = (double) blackPixelCount / contourPixelCount * 100;

        // Si le pourcentage de pixels noirs sur le contour est inférieur à 80%, nous n'avons pas un contour suffisant pour un rectangle noir
        if (blackPercentage < 89) {
            System.out.println("Le pourcentage de pixels noirs sur le contour est inférieur à 88%: " + blackPercentage + "%");
            return false;
        }

        // Calculer le nombre total de pixels à l'intérieur du contour
        int innerPixelCount = (width - contourThickness * 2) * (height - contourThickness * 2);

        // Calculer le nombre de pixels blancs à l'intérieur du contour
        int whitePixelCount = 0;
        for (int x = contourThickness; x < width - contourThickness; x++) {
            for (int y = contourThickness; y < height - contourThickness; y++) {
                if (new Color(image.getRGB(x, y)).equals(Color.WHITE)) {
                    whitePixelCount++;
                }
            }
        }

        // Calculer le pourcentage de pixels blancs à l'intérieur du contour
        double whitePercentage = (double) whitePixelCount / innerPixelCount * 100;

        // Si le pourcentage de pixels blancs à l'intérieur du contour est inférieur à 80%, le rectangle n'a pas assez de pixels blancs
        if (whitePercentage < 89) {
           // System.out.println("Le pourcentage de pixels blancs à l'intérieur du contour est inférieur à 88%: " + whitePercentage + "%");
            return false;
        }

        // Si toutes les vérifications sont réussies, c'est un rectangle noir au contour noir avec un intérieur blanc
        return true;
    }


    private static boolean isDarkRectangleWord(byte[] imageData) {
        // Charge l'image à partir des données de l'image
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            BufferedImage image = ImageIO.read(bais);

            // Largeur et hauteur de l'image
            int width = image.getWidth();
            int height = image.getHeight();

            // Vérifier la couleur du coin supérieur gauche
            Color colorUpperLeft = new Color(image.getRGB(0, 0));

            // Trouver l'épaisseur du contour
            int contourThickness = 0;

            // Vérifier le contour supérieur
            for (int y = 0; y < height; y++) {
                if (!new Color(image.getRGB(0, y)).equals(colorUpperLeft)) {
                    contourThickness = y;
                    break;
                }
            }

            // Vérifier le contour inférieur
            for (int y = height - 1; y >= 0; y--) {
                if (!new Color(image.getRGB(0, y)).equals(colorUpperLeft)) {
                    contourThickness = Math.max(contourThickness, height - y - 1);
                    break;
                }
            }

            // Vérifier le contour gauche
            for (int x = 0; x < width; x++) {
                if (!new Color(image.getRGB(x, 0)).equals(colorUpperLeft)) {
                    contourThickness = Math.max(contourThickness, x);
                    break;
                }
            }

            // Vérifier le contour droit
            for (int x = width - 1; x >= 0; x--) {
                if (!new Color(image.getRGB(x, 0)).equals(colorUpperLeft)) {
                    contourThickness = Math.max(contourThickness, width - x - 1);
                    break;
                }
            }

            // Calculer le nombre total de pixels sur le contour
            int contourPixelCount = (contourThickness * 2 + (width + height - 2) * 2);

            // Calculer le nombre de pixels noirs sur le contour
            int blackPixelCount = 0;
            for (int x = 0; x < contourThickness && x < width; x++) {

                for (int y = 0; y < height; y++) {
                    if (new Color(image.getRGB(x, y)).equals(colorUpperLeft)) {
                        blackPixelCount++;
                    }
                }
            }
            for (int x = Math.max(0, width - contourThickness); x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (new Color(image.getRGB(x, y)).equals(colorUpperLeft)) {
                        blackPixelCount++;
                    }
                }
            }
            for (int y = contourThickness; y < height - contourThickness; y++) {
                for (int x = 0; x < width; x++) {
                    if (new Color(image.getRGB(x, y)).equals(colorUpperLeft)) {
                        blackPixelCount++;
                    }
                }
            }

            // Calculer le pourcentage de pixels noirs sur le contour
            double blackPercentage = (double) blackPixelCount / contourPixelCount * 100;

            // Si le pourcentage de pixels noirs sur le contour est inférieur à 80%, nous n'avons pas un contour suffisant pour un rectangle noir
            if (blackPercentage < 89) {
                System.out.println("Le pourcentage de pixels noirs sur le contour est inférieur à 88%: " + blackPercentage + "%");
                return false;
            }

            // Calculer le nombre total de pixels à l'intérieur du contour
            int innerPixelCount = (width - contourThickness * 2) * (height - contourThickness * 2);

            // Calculer le nombre de pixels blancs à l'intérieur du contour
            int whitePixelCount = 0;
            for (int x = contourThickness; x < width - contourThickness; x++) {
                for (int y = contourThickness; y < height - contourThickness; y++) {
                    if (new Color(image.getRGB(x, y)).equals(Color.WHITE)) {
                        whitePixelCount++;
                    }
                }
            }

            // Calculer le pourcentage de pixels blancs à l'intérieur du contour
            double whitePercentage = (double) whitePixelCount / innerPixelCount * 100;

            // Si le pourcentage de pixels blancs à l'intérieur du contour est inférieur à 80%, le rectangle n'a pas assez de pixels blancs
            if (whitePercentage < 89) {
                System.out.println("Le pourcentage de pixels blancs à l'intérieur du contour est inférieur à 88%: " + whitePercentage + "%");
                return false;
            }

            // Si toutes les vérifications sont réussies, c'est un rectangle noir au contour noir avec un intérieur blanc
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            // En cas d'erreur lors de la lecture de l'image, retournez false
            return false;
        }
    }

    /**
     * Ajoute un code QR à un fichier PDF existant.
     *
     * @param inputPdfPath Chemin du fichier PDF d'entrée.
     * @param qrContent    Contenu du code QR.
     * @param qrWidth      Largeur du code QR.
     * @param qrHeight     Hauteur du code QR.
     * @throws IOException En cas d'erreur d'entrée/sortie lors de la manipulation des fichiers.
     */
    public static void addQRCodeToPdf(String inputPdfPath, String qrContent, Float qrWidth, Float qrHeight,
                                      Float positionX, float positionYFromTop, String texte)  {
       try {  // Déterminer le chemin du fichier PDF de sortie
            String outputPdfPath = inputPdfPath.replace(".pdf", "_QR.pdf");

            // Charger le fichier PDF existant
            try (PdfReader reader = new PdfReader(inputPdfPath);
                 PdfWriter writer = new PdfWriter(outputPdfPath);
                 com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
                 Document document = new Document(pdfDoc)) {

                // Générer le code QR avec QRGen
                ByteArrayOutputStream qrStream = QRCode.from(qrContent)
                        .to(ImageType.PNG)
                        .withSize(qrWidth.intValue(), qrHeight.intValue())
                        .stream();
                byte[] qrBytes = qrStream.toByteArray();

                // Créer une image du code QR
                Image qrImage = new Image(ImageDataFactory.create(qrBytes));

                // Calculer la position X dynamique
                float pageWidth = pdfDoc.getPage(1).getPageSize().getWidth();
                float dynamicPositionX = pageWidth - qrWidth - (positionX != null ? positionX : 0);

                // Calculer la position Y (depuis le haut de la page)
                float pageHeight = pdfDoc.getPage(1).getPageSize().getHeight();
                float adjustedPositionY = pageHeight - positionYFromTop - qrHeight;

                // Positionner et ajouter l'image du QR Code
                qrImage.setFixedPosition(dynamicPositionX, adjustedPositionY);
                document.add(qrImage);

                // Ajouter le texte sous le QR Code
                if (texte != null && !texte.isEmpty()) {
                    float textYPosition = adjustedPositionY - 8;
                    Paragraph textParagraph = new Paragraph(texte)
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFixedPosition(dynamicPositionX, textYPosition, qrWidth);

                    document.add(textParagraph);
                }
            }

            // Vérifier et gérer les fichiers
            if (existFile(outputPdfPath)) {
                deleteFile(inputPdfPath); // Supprimer le fichier PDF d'origine
                renameFile(outputPdfPath, inputPdfPath); // Renommer le nouveau fichier avec le même nom
            }
        }catch (Exception e){
         throw new IllegalArgumentException(e.getMessage());
       }
    }

    // Méthode pour calculer la luminosité d'une couleur
    private static int colorBrightness(Color color) {
        return (int) Math.sqrt(
                color.getRed() * color.getRed() * 0.299 +
                        color.getGreen() * color.getGreen() * 0.587 +
                        color.getBlue() * color.getBlue() * 0.114);
    }

    public static String convertToPdfGlobal(String docxFilePath, String outputExtension) throws IOException, InterruptedException {
        String osName = System.getProperty("os.name").toLowerCase();
        String pdfNom="";
        if (osName.contains("windows")||docxFilePath.contains("xlsx")
        ) {
            pdfNom= ConvertToPDFNew(docxFilePath, outputExtension);
        } else if (osName.contains("linux")) {
            pdfNom= convertToPdf(docxFilePath, outputExtension);
        } else {
            System.out.println("Système d'exploitation non pris en charge");
        }
        return pdfNom;
    }



    public static String ConvertToPDFNew(String docPath, String outputExtension) {
        try {
            // Remplacer l'extension du chemin du fichier par l'extension de sortie pour obtenir le chemin du fichier PDF
            String pdfPath = docPath.replace(docPath.substring(docPath.lastIndexOf(".")), outputExtension);
           System.out.println("PDFPATH: "+pdfPath);
            // Vérifier l'extension du fichier pour déterminer le type de document
            if (docPath.endsWith(".docx")) {
                // Charger et convertir un document Word en PDF
                com.spire.doc.Document doc = new com.spire.doc.Document();
                doc.loadFromFile(docPath);

                ToPdfParameterList ppl = new ToPdfParameterList();
                ppl.isEmbeddedAllFonts(true);
                ppl.setDisableLink(true);
                doc.setJPEGQuality(100);


                doc.saveToFile(pdfPath, ppl);
            } else if (docPath.endsWith(".xlsx")) {
                // Charger et convertir un fichier Excel en PDF
                Workbook workbook = new Workbook();
                workbook.loadFromFile(docPath);
                workbook.getConverterSetting().setSheetFitToPage(true);

                workbook.saveToFile(pdfPath, FileFormat.PDF);
            } else if (docPath.endsWith(".pptx")) {
                // Charger et convertir un fichier PowerPoint en PDF
                Presentation presentation = new Presentation();
                presentation.loadFromFile(docPath);

                presentation.saveToFile(pdfPath, com.spire.presentation.FileFormat.PDF);
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }

            // Extraire le nom du fichier PDF à partir du chemin du fichier PDF
            String pdfFileName = new File(pdfPath).getName();

            // Retourner uniquement le nom du fichier PDF
            return pdfFileName;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null; // Retourner null en cas d'erreur
        }
    }


    public static String convertToPdf(String docxFilePath, String outputExtension) throws IOException, InterruptedException {
        // Chemin d'exécution de LibreOffice
        String libreOfficePath = "/usr/bin/libreoffice";

        File docxFile = new File(docxFilePath);
        String parentDirectory = docxFile.getParent();


        // Nom du fichier DOCX sans extension
        String docxFileName = docxFile.getName().replaceFirst("[.][^.]+$", ""); // Retire l'extension du fichier

        // Chemin de sortie du fichier PDF
        String pdfFileName = docxFileName + outputExtension; // Nom du fichier PDF
        String pdfFilePath = parentDirectory + File.separator + pdfFileName; // Chemin complet du fichier PDF
        String pdfPath =  parentDirectory + File.separator +docxFileName +".pdf";
        // Commande pour convertir le fichier DOCX en PDF en utilisant LibreOffice
        //String[] cmd = {libreOfficePath, "--convert-to", "pdf", "--outdir", parentDirectory, docxFilePath};
        String[] cmd = {"libreoffice", "--headless", "--convert-to", "pdf", "--outdir", parentDirectory, docxFilePath};

        // Exécute la commande
        Process process = Runtime.getRuntime().exec(cmd);

        // Attendez que le processus se termine
        int exitCode = process.waitFor();

        // Vérifiez le code de sortie
        if (exitCode == 0) {
            System.out.println("Old pdfFilePath " + pdfPath);
            System.out.println("New pdfFilePath " + pdfFilePath);
            SignatureCodeQR.renameFile(pdfPath,pdfFilePath);
            return pdfFileName; // Retourne le nom du fichier PDF
        } else {
            System.out.println("La conversion a échoué !");
            return null; // Retourne null en cas d'échec de la conversion
        }
    }

    public static void addQRCodeToPdf(String inputPdfPath, String qrContent, Float qrWidth, Float qrHeight, Float positionX, float positionYFromTop)  {
        try {  // Déterminer le chemin du fichier PDF de sortie
            String outputPdfPath = inputPdfPath.replace(".pdf", "_QR.pdf");

            // Charger le fichier PDF existant
            try (PdfReader reader = new PdfReader(inputPdfPath);
                 PdfWriter writer = new PdfWriter(outputPdfPath);
                 com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
                 Document document = new Document(pdfDoc)) {

                // Générer le code QR avec QRGen
                ByteArrayOutputStream qrStream = QRCode.from(qrContent)
                        .to(ImageType.PNG)
                        .withSize(qrWidth.intValue(), qrHeight.intValue())
                        .stream();
                byte[] qrBytes = qrStream.toByteArray();

                // Créer une image du code QR
                Image qrImage = new Image(ImageDataFactory.create(qrBytes));

                // Calculer la position X dynamique
                float pageWidth = pdfDoc.getPage(1).getPageSize().getWidth();
                float dynamicPositionX = pageWidth - qrWidth - (positionX != null ? positionX : 0);

                // Calculer la position Y (depuis le haut de la page)
                float pageHeight = pdfDoc.getPage(1).getPageSize().getHeight();
                float adjustedPositionY = pageHeight - positionYFromTop - qrHeight;

                // Positionner et ajouter l'image du QR Code
                qrImage.setFixedPosition(dynamicPositionX, adjustedPositionY);
                document.add(qrImage);
            }

            // Vérifier et gérer les fichiers
            if (existFile(outputPdfPath)) {
                deleteFile(inputPdfPath); // Supprimer le fichier PDF d'origine
                renameFile(outputPdfPath, inputPdfPath); // Renommer le nouveau fichier avec le même nom
            }
        }catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    private static void removeWatermarkAnnotations(PdfPageBase page, int pageIndex) {
        for (int i = page.getAnnotationsWidget().getCount() - 1; i >= 0; i--) {
            PdfAnnotation annotation = page.getAnnotationsWidget().get(i);

            // Vérifiez si l'annotation est un filigrane
            if (annotation instanceof PdfWatermarkAnnotation) {
                page.getAnnotationsWidget().remove(annotation);
                System.out.println("Filigrane annotation supprimée à la page " + (pageIndex + 1));
            }
        }
    }

    // Supprimer les images filigranes
    private static void removeWatermarkImages(PdfPageBase page, int pageIndex) {
        PdfImageInfo[] images = page.getImagesInfo();

        if (images != null) {
            for (int i = images.length - 1; i >= 0; i--) {
                PdfImageInfo image = images[i];

                if (image != null && image.getImage() != null) {
                    // Vérifiez si l'image est de grande taille (typiquement un filigrane)
                    if (image.getImage().getWidth() > 500 && image.getImage().getHeight() > 500) {
                        page.deleteImage(i);  // Utilisez l'index pour supprimer l'image
                        System.out.println("Image filigrane supprimée à la page " + (pageIndex + 1));
                    }
                }
            }
        }
    }

    @Async
    public static String removeFiligrane(String inputFilePath, String outputFilePath)  {
        try {


            // Diviser le document PDF en plusieurs parties
            List<String> splitPdfPaths = splitPdfIntoMultiplePdfs(inputFilePath);
            int pdfSize = splitPdfPaths.size();

            // Liste pour stocker les documents traités
            ExecutorService executor = Executors.newFixedThreadPool(pdfSize);
            List<Future<String>> futures = new ArrayList<>();

            try {
                // Soumettre les tâches pour chaque document PDF séparé
                for (String splitPdfPath : splitPdfPaths) {
                    Callable<String> task = () -> {
                        PdfDocument document = new PdfDocument();
                        document.loadFromFile(splitPdfPath);
                        for (int pageIndex = 0; pageIndex < document.getPages().getCount(); pageIndex++) {
                            PdfPageBase page = document.getPages().get(pageIndex);

                            // Supprimer les annotations de filigrane
                            removeWatermarkAnnotations(page, pageIndex);

                            // Supprimer les images filigranes
                            removeWatermarkImages(page, pageIndex);
                        }

                        // Sauvegarder le document traité dans un fichier temporaire
                        String tempFilePath = "temp_split_processed_" + UUID.randomUUID().toString() + ".pdf";
                        document.saveToFile(tempFilePath);
                        return tempFilePath;
                    };
                    futures.add(executor.submit(task));
                }

                // Attendre que toutes les tâches soient terminées et récupérer les chemins des fichiers traités
                List<String> processedPdfPaths = new ArrayList<>();
                for (Future<String> future : futures) {
                    try {
                        String tempFilePath = future.get();
                        processedPdfPaths.add(tempFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Fusionner les fichiers traités
                mergePdfFiles(processedPdfPaths, outputFilePath);

                System.out.println("Filigranes supprimés et fichier enregistré sous : " + outputFilePath);
                for (String tempFilePath : processedPdfPaths) {
                    if (!tempFilePath.equals(outputFilePath)) {
                        SignatureCodeQR.deleteFile(tempFilePath);
                    }
                }

            } finally {
                for (String tempFilePath : splitPdfPaths) {
                    if (!tempFilePath.equals(outputFilePath)) {
                        SignatureCodeQR.deleteFile(tempFilePath);
                    }
                }

                // Fermer l'executor
                executor.shutdown();
            }
        }
        catch (Exception e){
        throw new IllegalArgumentException(e.getMessage());
        }
        return outputFilePath;
    }
    public static BufferedImage pretraiterImage(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));

        // Appliquer des transformations sur l'image (exemple : niveaux de gris)
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        grayImage.getGraphics().drawImage(image, 0, 0, null);

        return grayImage;
    }

    // Méthode pour extraire du texte à partir d'une image
    public static String extraireTexteDepuisImage(String imagePath) {
        try {
            BufferedImage image = pretraiterImage(imagePath);  // Prétraiter l'image
            Tesseract tess = getTesseractInstance();
            String text = tess.doOCR(image);
            return text;
        } catch (TesseractException e) {
           log.error("erreur tesseract = "+e.getMessage());
            return "Erreur lors de l'OCR : " + e.getMessage();
        } catch (IOException e) {
            log.error("erreur tesseract = "+e.getMessage());

            return "Erreur lors de la lecture de l'image : " + e.getMessage();
        }
    }

    private static List<ImageInfo> findMatchingImageInfoByOCR(
            BufferedImage referenceImage,
            PdfImageInfo[] imageInfo
    ) {
        List<ImageInfo> matchingInfos = new ArrayList<>();
        log.info("Commencement= ");
        try {


            File tempFile = File.createTempFile("reference_", ".png");
            ImageIO.write(referenceImage, "png", tempFile);
            String referenceText = extraireTexteDepuisImage(tempFile.getAbsolutePath()).trim().toLowerCase();
            log.info("Text= "+referenceText);
            tempFile.delete();

            for (PdfImageInfo info : imageInfo) {
                BufferedImage candidateImage = info.getImage();
                if (candidateImage == null || candidateImage.getWidth() < 5 || candidateImage.getHeight() < 5) continue;
                if ( candidateImage.getHeight() > 500) continue;

                File tempCandidateFile = File.createTempFile("candidate_", ".png");
                ImageIO.write(candidateImage, "png", tempCandidateFile);
                String candidateText = extraireTexteDepuisImage(tempCandidateFile.getAbsolutePath()).trim().toLowerCase();
                tempCandidateFile.delete();

                if (!referenceText.isEmpty() && referenceText.equals(candidateText)) {
                    Rectangle2D.Float bounds = (Rectangle2D.Float) info.getBounds();
                    matchingInfos.add(new ImageInfo(bounds.x, bounds.y, bounds.width, bounds.height));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matchingInfos;
    }
    private static void removeMatchingImageInfoByOCR(
            BufferedImage referenceImage,
            PdfImageInfo[] imageInfo,
            PdfPageBase spirePage
    ) {
        List<ImageInfo> matchingInfos = new ArrayList<>();
        log.info("Commencement= ");
        try {


            File tempFile = File.createTempFile("reference_", ".png");
            ImageIO.write(referenceImage, "png", tempFile);
            String referenceText = extraireTexteDepuisImage(tempFile.getAbsolutePath()).trim().toLowerCase();
            log.info("Text= "+referenceText);
            tempFile.delete();

            for (PdfImageInfo info : imageInfo) {
                BufferedImage candidateImage = info.getImage();
                if (candidateImage == null || candidateImage.getWidth() < 5 || candidateImage.getHeight() < 5) continue;
                if ( candidateImage.getHeight() > 500) continue;

                File tempCandidateFile = File.createTempFile("candidate_", ".png");
                ImageIO.write(candidateImage, "png", tempCandidateFile);
                String candidateText = extraireTexteDepuisImage(tempCandidateFile.getAbsolutePath()).trim().toLowerCase();
                tempCandidateFile.delete();

                if (!referenceText.isEmpty() && referenceText.equals(candidateText)) {
                   spirePage.deleteImage(info.getIndex());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void supprimerCadre(String filePath, String providedImagePath,int pageNumberToProcess) {
        try {
            // Convert reference image
            PDImageXObject pdImageXObject = SignatureCodeQR.convertImageToPDImageXObject(providedImagePath);
            BufferedImage referenceImage = pdImageXObject.getImage();

            // Split PDF into individual pages
            List<String> splitPdfPaths = SignatureCodeQR.splitPdfIntoMultiplePdfs(filePath);

            int threadCount = Math.max(2, splitPdfPaths.size() / 4);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Callable<File>> tasks = new ArrayList<>();
            int pageIndex=0;
            for (String tempPath : splitPdfPaths) {
                int finalPageIndex = pageIndex;
                tasks.add(() -> {
                    PdfDocument onePageDoc = null;
                    try {
                        onePageDoc = new PdfDocument();
                        onePageDoc.loadFromFile(tempPath);  // Load the PDF file
                        boolean shouldProcess = (pageNumberToProcess == -1) || (finalPageIndex == pageNumberToProcess - 1);

                        if (shouldProcess) {
                            PdfPageBase spirePage = onePageDoc.getPages().get(0);
                            PdfImageInfo[] images = Arrays.stream(spirePage.getImagesInfo())
                                    .filter(img -> img.getImage() != null && img.getImage().getHeight() < 500)
                                    .toArray(PdfImageInfo[]::new);

                            SignatureCodeQR.removeMatchingImageInfoByOCR(referenceImage, images, spirePage);
                        }
                        // Create a clean temporary file
                        File cleanedFile = Files.createTempFile("page_cleaned_", ".pdf").toFile();
                        try (FileOutputStream fos = new FileOutputStream(cleanedFile)) {
                            onePageDoc.saveToStream(fos);
                            fos.flush(); // Ensure the stream is fully written
                        }

                        // Verify file integrity before returning
                        int retries = 5;
                        while (retries > 0 && (!cleanedFile.exists() || cleanedFile.length() == 0)) {
                            Thread.sleep(100); // Wait briefly for file system to catch up
                            retries--;
                        }
                        if (!cleanedFile.exists() || cleanedFile.length() == 0) {
                            throw new IOException("Generated PDF file is empty or not created: " + cleanedFile.getAbsolutePath());
                        }

                        // Test with PDFBox to ensure compatibility
                        try (PDDocument testDoc = PDDocument.load(cleanedFile)) {
                            // If it loads successfully, it's likely valid
                        }

                        return cleanedFile;
                    } catch (Exception e) {
                        log.warn("⚠️ Error processing page " + tempPath + ": " + e.getMessage());
                        return null; // Return null to skip invalid pages
                    } finally {
                        if (onePageDoc != null) {
                            onePageDoc.close(); // Manually close the PdfDocument
                        }
                    }
                });
                pageIndex++;
            }

            List<Future<File>> futures = executor.invokeAll(tasks);
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES); // Wait for all tasks to complete

            // Collect valid processed files before merging
            List<File> filesToMerge = new ArrayList<>();
            for (Future<File> future : futures) {
                try {
                    File processedFile = future.get();
                    if (processedFile != null && processedFile.exists() && processedFile.length() > 0) {
                        filesToMerge.add(processedFile);
                    } else {
                        log.warn("⚠️ Empty or missing PDF file ignored: " + (processedFile != null ? processedFile.getAbsolutePath() : "null"));
                    }
                } catch (Exception ex) {
                    log.warn("⚠️ Error on a page: " + ex.getMessage());
                }
            }

            // Merge with PDFBox
            if (!filesToMerge.isEmpty()) {
                PDFMergerUtility merger = new PDFMergerUtility();
                merger.setDestinationFileName(filePath); // Overwrite the original file

                for (File processedFile : filesToMerge) {
                    try (PDDocument docTest = PDDocument.load(processedFile)) {
                        merger.addSource(processedFile);
                    } catch (IOException e) {
                        log.warn("⚠️ PDF ignored (unreadable by PDFBox): " + processedFile.getAbsolutePath());
                    }
                }

                // Perform final merge
                merger.mergeDocuments(null);
                log.info("✅ Processing completed successfully. PDF cleaned.");
            } else {
                log.warn("⚠️ No valid pages to merge.");
            }

            // Clean up processed files after merging
            for (File processedFile : filesToMerge) {
                try {
                    processedFile.delete();
                } catch (Exception ignored) {
                    // Ignore cleanup errors
                }
            }

            // Clean up split pages
            for (String path : splitPdfPaths) {
                new File(path).delete();
            }

        } catch (Exception e) {
            log.error("❌ Global error: " + e.getMessage(), e);
        }
    }
    public static void convertToNonEditablePdfWithoutPassword(String inputPdfPath) throws IOException {
        // Création d'un fichier temporaire
        File tempFile = File.createTempFile("secured_pdf_", ".pdf");
        String tempFilePath = tempFile.getAbsolutePath();

        PdfReader reader = null;
        PdfWriter writer = null;

        try {
            // Configuration des restrictions maximales
            WriterProperties writerProperties = new WriterProperties()
                    .setStandardEncryption(
                            null,  // Pas de mot de passe utilisateur
                            null,  // Pas de mot de passe propriétaire
                            EncryptionConstants.ALLOW_PRINTING,  // Autoriser seulement l'impression
                            EncryptionConstants.ENCRYPTION_AES_256)  // Chiffrement le plus fort
                    .addUAXmpMetadata()
                    .setPdfVersion(PdfVersion.PDF_2_0);

            // Initialisation des objets PDF
            reader = new PdfReader(inputPdfPath);
            writer = new PdfWriter(tempFilePath, writerProperties);

            // Création du document sécurisé
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);

            // Configuration avancée pour bloquer la copie
            PdfViewerPreferences preferences = new PdfViewerPreferences();
            preferences.setPickTrayByPDFSize(true);
            preferences.setPrintScaling(PdfViewerPreferences.PdfViewerPreferencesConstants.NONE);
            pdfDoc.getCatalog().setViewerPreferences(preferences);
            pdfDoc.setFlushUnusedObjects(true);

            // Solution clé pour empêcher la fuite "com.itextpdf..."
            PdfDictionary permissions = new PdfDictionary();
            permissions.put(new PdfName("Copy"), PdfBoolean.FALSE);
            permissions.put(new PdfName("Extract"), PdfBoolean.FALSE);
            pdfDoc.getCatalog().put(PdfName.Perms, permissions);

            // Configuration viewer preferences
            PdfViewerPreferences viewerPrefs = new PdfViewerPreferences();
            viewerPrefs.setPrintScaling(PdfViewerPreferences.PdfViewerPreferencesConstants.NONE);
            pdfDoc.getCatalog().setViewerPreferences(viewerPrefs);

            // Nettoyage des métadonnées
            pdfDoc.setFlushUnusedObjects(true);

            // Marquage comme document final
            pdfDoc.getCatalog().setModified();
            pdfDoc.getCatalog().setPageMode(PdfName.UseNone);
            pdfDoc.close();


            Files.copy(Paths.get(tempFilePath), Paths.get(inputPdfPath), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("PDF sécurisé généré avec succès : " + inputPdfPath);
        } finally {
            // Nettoyage des ressources
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (tempFile.exists()) tempFile.delete();
        }
    }

    public static void convertToImageBasedPdf(String inputPdfPath) throws IOException, InterruptedException {
        // Create a temporary copy of the input file
        File inputFile = new File(inputPdfPath);
        File tempInputFile = File.createTempFile("input_copy_", ".pdf");
        Files.copy(Paths.get(inputPdfPath), Paths.get(tempInputFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);

        try (PDDocument pdDoc = PDDocument.load(tempInputFile)) {
            int pageCount = pdDoc.getNumberOfPages();
            String tempDir = System.getProperty("java.io.tmpdir");
            File[] imageFiles = new File[pageCount];

            int threadCount = Math.max(10, Runtime.getRuntime().availableProcessors());
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                List<Future<?>> futures = new ArrayList<>();
                for (int page = 0; page < pageCount; ++page) {
                    final int currentPage = page;
                    futures.add(executor.submit(() -> {
                        try (PDDocument threadDoc = PDDocument.load(tempInputFile)) {
                            PDFRenderer renderer = new PDFRenderer(threadDoc);
                            BufferedImage bim = renderer.renderImageWithDPI(currentPage, 300);
                            File imageFile = new File(tempDir,
                                    "page_" + currentPage + "_" + Thread.currentThread().getId() + ".png");
                            if (!ImageIO.write(bim, "png", imageFile)) {
                                throw new IOException("Aucun writer PNG disponible");
                            }
                            imageFiles[currentPage] = imageFile;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }

                for (Future<?> future : futures) {
                    future.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Conversion interrompue", e);
            } catch (ExecutionException e) {
                throw new IOException("Erreur lors de la conversion parallèle", e.getCause());
            } finally {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            File tempPdfFile = File.createTempFile("image_based_", ".pdf");
            String tempPdfPath = tempPdfFile.getAbsolutePath();

            try (PdfWriter writer = new PdfWriter(tempPdfPath);
                 com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
                 Document document = new Document(pdfDoc)) {
                for (int i = 0; i < imageFiles.length; i++) {
                    try {
                        ImageData imageData = ImageDataFactory.create(imageFiles[i].getAbsolutePath());
                        Image img = new Image(imageData);
                        float margin = 0;
                        float width = pdfDoc.getDefaultPageSize().getWidth() - margin * 2;
                        float height = pdfDoc.getDefaultPageSize().getHeight() - margin * 2;
                        img.scaleToFit(width, height);
                        img.setFixedPosition(margin, margin);
                        document.add(img);
                        if (i < imageFiles.length - 1) {
                            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                        }
                    } catch (Exception e) {
                        throw new IOException("Erreur lors du chargement de l'image " + imageFiles[i], e);
                    }
                }
            }

            // Move the result back to the original file with retry
            int maxRetries = 5;
            int retryDelayMs = 500;
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    Files.move(Paths.get(tempPdfPath), Paths.get(inputPdfPath), StandardCopyOption.REPLACE_EXISTING);
                    break;
                } catch (IOException e) {
                    if (attempt == maxRetries - 1) {
                        throw new IOException("Impossible de déplacer le fichier après " + maxRetries + " tentatives", e);
                    }
                    Thread.sleep(retryDelayMs);
                }
            }

            // Clean up temporary files
            cleanUpTempFiles(imageFiles, tempPdfFile);
            tempInputFile.delete(); // Delete the temporary input copy
            System.out.println("PDF converti en version image-based avec parallélisation : " + inputPdfPath);
        } catch (IOException | InterruptedException e) {
            throw e; // Re-throw to ensure the caller handles it
        } finally {
            // Ensure temp input file is deleted even if an error occurs
            if (tempInputFile.exists()) {
                tempInputFile.delete();
            }
        }
    }


    private static void cleanUpTempFiles(File[] imageFiles, File pdfFile) {
        // Nettoyage en parallèle
        Arrays.stream(imageFiles)
                .parallel()
                .filter(Objects::nonNull)
                .filter(File::exists)
                .forEach(File::delete);

        if (pdfFile.exists()) {
            pdfFile.delete();
        }
    }
    public static void addTextToPdf(String inputPdfPath, String text, int pageNumber, float x, float y, float width, float height,String providedImagePath)  {
        // Charger le fichier PDF existant
        try {
            supprimerCadre(inputPdfPath, providedImagePath, pageNumber); // Suppression de l'image ou cadre (ici, providedImagePath est null)

        PdfReader reader = new PdfReader(inputPdfPath);
        String tempPdfPath = inputPdfPath + ".temp.pdf";

        PdfWriter writer = new PdfWriter(tempPdfPath);  // Écrire dans le même fichier

        // Créer un nouveau document PDF
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Récupérer la page spécifiée
        PdfPage page = pdfDoc.getPage(pageNumber);

        float posY = page.getMediaBox().getHeight() - y;

        // Créer et ajouter le paragraphe
        Paragraph paragraph = new Paragraph(text)
                .setFontSize(12)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(pageNumber, x, posY, width); // Pas besoin de hauteur ici

        document.add(paragraph);

        // Fermer le document
        document.close();
        Files.move(Paths.get(tempPdfPath), Paths.get(inputPdfPath), StandardCopyOption.REPLACE_EXISTING);  }
        catch (Exception e){
          throw new IllegalArgumentException(e.getMessage() );
        }
    }
    public static void removeWatermarks(String inputFile, String outputFile) throws IOException {
        System.out.println("Début de removeWatermarks");
        try (com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(new PdfReader(inputFile), new PdfWriter(outputFile))) {

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage page = pdfDoc.getPage(i);

                List<PdfStream> watermarksToRemove = new ArrayList<>();

                // Processus pour détecter les images considérées comme filigranes
                PdfCanvasProcessor processor = new PdfCanvasProcessor(new IEventListener() {
                    @Override
                    public void eventOccurred(IEventData data, EventType type) {
                        if (type == EventType.RENDER_IMAGE) {
                            ImageRenderInfo renderInfo = (ImageRenderInfo) data;
                            try {
                                float imageWidth = renderInfo.getImage().getWidth();
                                float imageHeight = renderInfo.getImage().getHeight();

                                // Condition pour identifier les filigranes (dimensions spécifiques)
                                if (imageWidth >= 500 && imageHeight >= 500) {
                                    PdfStream imageStream = renderInfo.getImage().getPdfObject();
                                    watermarksToRemove.add(imageStream);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur lors de la lecture d'une image : " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public Set<EventType> getSupportedEvents() {
                        return Set.of(EventType.RENDER_IMAGE);
                    }
                });
                processor.processPageContent(page);

                // Suppression des filigranes identifiés
                for (PdfStream watermark : watermarksToRemove) {
                    try {
                        watermark.clear();
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression du filigrane : " + e.getMessage());
                    }
                }
            }

            System.out.println("Filigranes supprimés avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression des filigranes : " + e.getMessage());
            throw new IOException("Erreur lors du traitement du fichier PDF.", e);
        }
    }

}