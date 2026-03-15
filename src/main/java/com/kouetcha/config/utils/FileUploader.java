package com.kouetcha.config.utils;

import org.apache.commons.codec.binary.Hex;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class FileUploader {

    private Path foundFile;

    public static String enregistrerFichier(String path, String fileName, MultipartFile multipartFile) throws IOException {
        Integer lastId = 0;
        Path uploadDirectory = Paths.get(path);
        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }

        //String fileCode = RandomStringUtils.randomAlphabetic(8);

        String fileCode;
        try {
            Instant instant = Instant.now();
            String date = instant.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(date.getBytes());
            fileCode = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Impossible de créer le code de fichier", e);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadDirectory.resolve(fileCode + "-" + fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Impossible d'enregistrer le fichier: " + fileName, ioe);
        }
        return fileCode + "-" + fileName;
    }

    public Resource telechargerFichier(String media, String fileCode) throws IOException {
        Path uploadDirectory = Paths.get(media);
        Files.list(uploadDirectory).forEach(file -> {
            if (file.getFileName().toString().startsWith(fileCode)) {
                foundFile = file;
                return;
            }
        });
        if (foundFile != null) {
            return new UrlResource(foundFile.toUri());
        }
        return null;
    }

    public static void supprimerFichier(String media, String fileCode) {
        // Récupérer le chemin absolu du fichier en utilisant le code de fichier
        String filePath = media + fileCode;
        System.out.println("Fichier à supprimer : " + filePath);

        // Supprimer le fichier s'il existe
        File file = new File(filePath);
        System.out.println("Le fichier existe : " + file.exists());
        if (file.exists()) {
            try {
                boolean deleted = file.delete();
                if (deleted){
                    System.out.println("Le fichier a été supprimé avec succès.");
                } else {
                    System.out.println("Impossible de supprimer le fichier.");
                }
            } catch (SecurityException e) {
                System.out.println("Permission refusée pour supprimer le fichier.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Le fichier n'existe pas.");
        }
    }

    public static void supprimerFichierr(String media , String fileCode) {
        String path = media + fileCode;
        File file = new File(path);
        if (file.exists()) {
            try {
                boolean delete = file.delete();
                if (delete){
                    System.out.println("le fichier a ete supprimer");
                }else {
                    System.out.println("Impossible de supprimer le fichier.");
                }
            }catch (SecurityException e) {
                System.out.println("Impossible de supprimer le fichier.");
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Le fichier n'existe pas.");
        }
    }
}