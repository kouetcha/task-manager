package com.kouetcha.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.kouetcha.exception.RequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Slf4j
public class FileUtility {

    private static final Logger LOGGER = Logger.getLogger(FileUtility.class.getName());
    private static  int RANDOM_BYTES_LENGTH ;
    @Value("${kouetcha.simple.securite.randomBytesLength:20}")
    private int securiterandomBytesLength;


    @PostConstruct
    private void init() {
        RANDOM_BYTES_LENGTH=securiterandomBytesLength;
    }

    public static String enregistrerFichier(MultipartFile multipartFile, String dossierPath) {
       try {
            Path dossier = prepareDirectory(dossierPath);

            String originalFilename = sanitizeFilename(multipartFile.getOriginalFilename());
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("Le nom du fichier est invalide.");
            }

            String fileCode = generateRandomCode();
            String newFilename = fileCode + "-" + originalFilename;

            try (InputStream inputStream = multipartFile.getInputStream()) {
                Path filePath = dossier.resolve(newFilename);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement du fichier : {0}", originalFilename);
                throw new IOException("Impossible d'enregistrer le fichier : " + originalFilename, e);
            }

            return newFilename;
        }catch (Exception e) {
          throw new IllegalArgumentException(e.getMessage());
       }
    }
    public static String copierFichier(String sourcePathStr, String dossierDestination)  {
      try {
            Path sourcePath = Paths.get(sourcePathStr);
            if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
                throw new IllegalArgumentException("Le fichier source n'existe pas ou n'est pas un fichier valide : " + sourcePathStr);
            }

            Path dossierDest = prepareDirectory(dossierDestination);
            String originalFilename = sourcePath.getFileName().toString();

            String fileCode = generateRandomCode();
            String newFilename = fileCode + "-" + originalFilename;
            Path destinationPath = dossierDest.resolve(newFilename);

            try {
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la copie du fichier : {0}", sourcePathStr);
                throw new IOException("Erreur lors de la copie du fichier : " + sourcePathStr, e);
            }

            return newFilename;
        }catch (Exception e){
         throw new IllegalArgumentException(e.getMessage());
      }
    }

    public static ResponseEntity<Resource> telechargementFichier(String dossierChemin, String fileName) {
        try {
            Resource resource = telechargerFichier(dossierChemin, fileName);

            MediaType mediaType = MediaTypeFactory
                    .getMediaType(resource.getFilename())
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            ContentDisposition inlineDisposition = ContentDisposition
                    .inline()
                    .filename(resource.getFilename(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, inlineDisposition.toString())
                    .body(resource);

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du téléchargement du fichier : " + ex.getMessage(), ex
            );
        }
    }

    public static ResponseEntity<Resource> telechargementFichierAttachement(String dossierChemin, String fileName) {
        try {
            Resource resource = telechargerFichier(dossierChemin, fileName);

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Fichier introuvable ou illisible : " + fileName);
            }

            String extension = getFileExtension(fileName).toLowerCase();
            String contentType;

            // Déterminer le type MIME selon l'extension
            switch (extension) {
                case "pdf":
                    contentType = "application/pdf";
                    break;
                case "png":
                    contentType = "image/png";
                    break;
                case "jpg":
                case "jpeg":
                    contentType = "image/jpeg";
                    break;
                case "gif":
                    contentType = "image/gif";
                    break;
                default:
                    contentType = "application/octet-stream"; // fallback générique
            }

            String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } catch (Exception e) {
            throw new IllegalArgumentException("Une erreur est survenue : " + e.getMessage(), e);
        }
    }


    public static String fournirFichierWithName(MultipartFile multipartFile, String nom) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Le nom du fichier est invalide.");
        }

        String extension = getFileExtension(originalFilename);
        boolean alreadyExtension = nom.endsWith(extension);

        // Générer un code aléatoire
        String fileCode = generateRandomCode();

        // Supprimer les caractères spéciaux et normaliser le nom
        String nomNettoye = nom.replaceAll("[^a-zA-Z0-9\\-_.]", "_");

        // Construire le nom final
        return fileCode + "-" + nomNettoye + (alreadyExtension ? "" : extension);
    }

    public static String enregistrerFichierWithName(MultipartFile multipartFile, String dossierPath, String nom) {
        try {
            Path dossier = prepareDirectory(dossierPath);

            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("Le nom du fichier est invalide.");
            }

            // Sanitize le nom fourni
            String sanitizedNom = sanitizeFilename(nom);

            String extension = getFileExtension(originalFilename);
            String fileCode = generateRandomCode();
            boolean alreadyExtension = sanitizedNom.endsWith(extension);
            String newFilename = fileCode +(!sanitizedNom.isEmpty()? "-":"") + sanitizedNom + (alreadyExtension ? "" : "."+extension);

            try (InputStream inputStream = multipartFile.getInputStream()) {
                Path filePath = dossier.resolve(newFilename);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                log.info("PATH FICHIER : "+filePath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement du fichier avec nom : {0}", originalFilename);
                throw new IllegalArgumentException("Impossible d'enregistrer le fichier : " + originalFilename, e);
            }

            return newFilename;
        } catch (Exception e) {
            throw new IllegalArgumentException("Une erreur est survenue : " + e);
        }
    }

    private static String sanitizeFilename(String input) {
        if (input == null) return "fichier";
        // Supprimer les caractères non valides
        return input.replaceAll("[\\\\/:*?\"<>|]", "") // Windows forbidden characters
                .replaceAll("[\\p{Cntrl}]", "")   // Caractères de contrôle
                .replaceAll("\\s+", "_")          // Espaces en underscores
                .replaceAll("_+", "_")            // Nettoyage des underscores répétés
                .trim();
    }

    public static ByteArrayResource convertToByteArrayResource(Resource resource) throws IOException {
        // Vérifier si la ressource est valide
        if (resource == null || !resource.exists() || !resource.isReadable()) {
            throw new IOException("La ressource n'est pas valide ou illisible.");
        }

        // Lire le contenu de la ressource dans un tableau d'octets
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] content = inputStream.readAllBytes(); // Lire tout le contenu dans un tableau d'octets
            return new ByteArrayResource(content); // Créer et retourner un ByteArrayResource
        }
    }

    public static Resource telechargerFichier(String dossierPath, String fileCode)  {
        Path dossier = Paths.get(dossierPath);
        Path filePath =dossier;
        if(fileCode!=null && !fileCode.isEmpty()) {
           filePath = dossier.resolve(fileCode);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("Le fichier est introuvable ou illisible : " + fileCode);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "URL du fichier mal formée : {0}", fileCode);
            throw new IllegalArgumentException("Fichier non trouvé : " + fileCode, e);
        }
    }

    public static void moveFile(String sourceFilePath, String targetFilePath) throws IOException {
        Path sourcePath = Paths.get(sourceFilePath);
        Path targetPath = Paths.get(targetFilePath);

        Path targetDirectory = targetPath.getParent();
        prepareDirectory(targetDirectory.toString());

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info(() -> "Fichier déplacé avec succès de " + sourceFilePath + " à " + targetFilePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du déplacement du fichier de {0} à {1}", new Object[]{sourceFilePath, targetFilePath});
            throw new IOException("Erreur lors du déplacement du fichier.", e);
        }
    }

    public static void supprimerFichier(String dossierPath, String fileName) {
        Path filePath = Paths.get(dossierPath).resolve(fileName);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Impossible de supprimer le fichier : {0}    "+e.getMessage(), filePath);
            throw new IllegalArgumentException("Erreur lors de la suppression du fichier.", e);
        }
    }

    public static boolean fichierExiste(String dossierPath, String fileName) {
        Path filePath = Paths.get(dossierPath).resolve(fileName);
        return Files.exists(filePath);
    }
    public static boolean fichierCheminCompletExiste(Path dossierPath) {

        return Files.exists(dossierPath);
    }
    public static boolean fichierCheminCompletExiste(String cheminFichier) {
        if (cheminFichier == null || cheminFichier.isEmpty()) {
           return false;
        }
        Path cheminPath = Path.of(cheminFichier);
        return Files.exists(cheminPath);
    }


    public static ResponseEntity<Resource> downloadFile(Path filePath) {
        try {
            Resource fileResource = new UrlResource(filePath.toUri());

            if (!fileResource.exists() || !fileResource.isReadable()) {
                LOGGER.warning("Fichier introuvable ou illisible : " + filePath);
                return ResponseEntity.notFound().build();
            }

            // Déterminer le type MIME
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileResource.getFilename() + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileResource);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la lecture du fichier : {0}", filePath);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    public static ResponseEntity<Resource> downloadFileOnlyOffice(Path filePath) {

        try {

            if (filePath == null || !Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Streaming resource (ne charge PAS tout en mémoire)
            Resource resource = new UrlResource(filePath.toUri());

            String fileName = filePath.getFileName().toString();

            // 🔹 Nettoyage ASCII pour compatibilité ancienne (évite l’erreur Tomcat)
            String asciiFileName = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[\\r\\n\"]", ""); // sécurité header injection

            // 🔹 ContentDisposition RFC 5987 (UTF-8 propre)
            ContentDisposition contentDisposition = ContentDisposition
                    .inline()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build();

            String contentType = getOfficeMimeType(filePath);

            long fileSize = Files.size(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                            HttpHeaders.CONTENT_DISPOSITION + ", " + HttpHeaders.CONTENT_LENGTH)
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(fileSize)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    private static String getOfficeMimeType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fileName.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".doc")) {
            return "application/msword";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        }

        // Fallback
        try {
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    // Méthodes auxiliaires privées

    private static Path prepareDirectory(String dossierPath) throws IOException {
        Path dossier = Paths.get(dossierPath);
        if (!Files.exists(dossier)) {
            Files.createDirectories(dossier);
        }
        return dossier;
    }

    public static String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[RANDOM_BYTES_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public static void convertPowerPointToPdf(File pptFile, File pdfFile) {
        try {
            LOGGER.info("Début de la conversion PowerPoint vers PDF");
            com.spire.presentation.Presentation presentation = new com.spire.presentation.Presentation();
            presentation.loadFromFile(pptFile.getPath());

            // Conversion vers PDF
            presentation.saveToFile(pdfFile.getPath(), com.spire.presentation.FileFormat.PDF);

            // Vérification que le fichier PDF a bien été créé
            if (!pdfFile.exists() || pdfFile.length() == 0) {
                throw new RequestException("La conversion n'a pas généré de fichier PDF valide");
            }

            LOGGER.info("Conversion PowerPoint vers PDF terminée avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la conversion PowerPoint vers PDF: {}", e.getMessage());
            throw new RequestException("Erreur lors de la conversion PowerPoint vers PDF: " + e.getMessage());
        }
    }

    public static void convertWordToPdf(File wordFile, File pdfFile) {
        try {
            LOGGER.info("Début de la conversion Word vers PDF");
            com.spire.doc.Document doc = new com.spire.doc.Document();
            doc.loadFromFile(wordFile.getPath());
            doc.saveToFile(pdfFile.getPath(), com.spire.doc.FileFormat.PDF);

            // Vérification que le fichier PDF a bien été créé
            if (!pdfFile.exists() || pdfFile.length() == 0) {
                throw new RequestException("La conversion n'a pas généré de fichier PDF valide");
            }

            LOGGER.info("Conversion Word vers PDF terminée avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la conversion Word vers PDF: {}", e.getMessage());
            throw new RequestException("Erreur lors de la conversion Word vers PDF: " + e.getMessage());
        }
    }

    public static void convertTextToPdf(File textFile, File pdfFile) {
        try {
            LOGGER.info("Début de la conversion Text vers PDF");
            Document document = new Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            document.add(new com.itextpdf.text.Paragraph(FileUtils.readFileToString(textFile, "UTF-8")));
            document.close();

            // Vérification que le fichier PDF a bien été créé
            if (!pdfFile.exists() || pdfFile.length() == 0) {
                throw new RequestException("La conversion n'a pas généré de fichier PDF valide");
            }

            LOGGER.info("Conversion Text vers PDF terminée avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la conversion Text vers PDF: {}", e.getMessage());
            throw new RequestException("Erreur lors de la conversion Text vers PDF: " + e.getMessage());
        }
    }

    public static void convertExcelToPdf(File excelFile, File pdfFile) {
        FileInputStream input = null;
        Workbook workbook = null;
        try {
            LOGGER.info("Début de la conversion Excel vers PDF");
            input = new FileInputStream(excelFile);
            if (excelFile.getName().toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(input);
            } else {
                workbook = new HSSFWorkbook(input);
            }

            // Créer le document PDF
            Document document = new Document(PageSize.A4.rotate());
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Convertir chaque feuille
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (i > 0) {
                    document.newPage();
                }

                Sheet sheet = workbook.getSheetAt(i);
                PdfPTable table = new PdfPTable(getMaxColumns(sheet));
                table.setWidthPercentage(100);

                // Traiter chaque ligne
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue));
                        table.addCell(pdfCell);
                    }
                }

                document.add(table);
            }

            document.close();

            // Vérification que le fichier PDF a bien été créé
            if (!pdfFile.exists() || pdfFile.length() == 0) {
                throw new RequestException("La conversion n'a pas généré de fichier PDF valide");
            }

            LOGGER.info("Conversion Excel vers PDF terminée avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la conversion Excel vers PDF: {}", e.getMessage());
            throw new RequestException("Erreur lors de la conversion Excel vers PDF: " + e.getMessage());
        } finally {
            try {
                if (workbook != null) workbook.close();
                if (input != null) input.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture des ressources: {}", e.getMessage());
            }
        }
    }

    private static int getMaxColumns(Sheet sheet) {
        int maxColumns = 0;
        for (Row row : sheet) {
            maxColumns = Math.max(maxColumns, row.getLastCellNum());
        }
        return maxColumns;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                    }
                    // Éviter l'affichage scientifique des nombres
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.format("%d", (long) value);
                    }
                    return String.format("%.2f", value);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                                }
                                double formulaValue = cell.getNumericCellValue();
                                if (formulaValue == (long) formulaValue) {
                                    return String.format("%d", (long) formulaValue);
                                }
                                return String.format("%.2f", formulaValue);
                            case STRING:
                                return cell.getStringCellValue();
                            case BOOLEAN:
                                return String.valueOf(cell.getBooleanCellValue());
                            case ERROR:
                                return "#ERROR#";
                            default:
                                return "";
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Erreur lors de l'évaluation de la formule: {}", e.getMessage());
                        return "#ERROR#";
                    }
                case ERROR:
                    return "#ERROR#";
                case BLANK:
                    return "";
                default:
                    return "";
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la lecture de la cellule: {}", e.getMessage());
            return "#ERROR#";
        }
    }
    public static File convertToPdf(MultipartFile file,String libreOfficePathWindows,String libreOfficePathLinus)  {
        try {


            String originalFilename = file.getOriginalFilename();
            String ext = FilenameUtils.getExtension(originalFilename).toLowerCase();

            File tempFile = File.createTempFile("temp_", "." + ext);
            FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);

            // Convertir en PDF selon le type de fichier
            File pdfFile = File.createTempFile("converted_", ".pdf");
            String osName = System.getProperty("os.name").toLowerCase();
            String LIBREOFFICE_PATH;
            if (osName.contains("windows") ) {
                LIBREOFFICE_PATH=libreOfficePathWindows;
            } else if (osName.contains("linux")) {
                LIBREOFFICE_PATH=libreOfficePathLinus;
            } else {
                throw new UnsupportedOperationException("Système d'exploitation non pris en charge.");
            }
            try {
                SignatureCodeQR.convertFile(LIBREOFFICE_PATH,ext,tempFile,pdfFile);
            }
            catch (Exception e){
                log.error("Une erreur est survenue: "+e.getMessage());
                throw new IllegalArgumentException("Une erreur est survenue: "+e.getMessage());
            }


            tempFile.delete();
            return pdfFile;
        }catch (Exception e){
            log.error("Une erreur est survenue: "+e.getMessage());
            throw new IllegalArgumentException("Une erreur est survenue: "+e.getMessage());

        }
    }

}