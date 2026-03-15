package com.kouetcha.config.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileDownloader {

    public Resource telechargerFichier(String media, String fileCode) throws IOException {
        Path uploadDirectory = Paths.get(media);
        Optional<Path> foundFile = Files.list(uploadDirectory)
                .filter(file -> file.getFileName().toString().startsWith(fileCode))
                .findFirst();

        if (foundFile.isPresent()) {
            return new UrlResource(foundFile.get().toUri());
        }
        return null;
    }
}