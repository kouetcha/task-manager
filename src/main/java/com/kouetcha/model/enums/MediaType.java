package com.kouetcha.model.enums;

import java.util.Arrays;

public enum MediaType {
    AUDIO("Audio", new String[]{"mp3", "wav", "aac"}),
    IMAGE("Image", new String[]{"jpg", "jpeg", "png", "gif"}),
    VIDEO("Vidéo", new String[]{"mp4", "avi", "mov", "mkv"}),
    DOCUMENT("Document", new String[]{"pdf", "docx", "doc", "xlsx", "xls", "pptx", "ppt"}),
    LIEN("Lien vidéo", new String[]{}); // Pas besoin d'extensions ici

    private final String label;
    private final String[] extensions;

    MediaType(String label, String[] extensions) {
        this.label = label;
        this.extensions = extensions;
    }

    public String getLabel() {
        return label;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public boolean supportsExtension(String extension) {
        if (this == LIEN) {
            return extension != null && (extension.startsWith("http://") || extension.startsWith("https://"));
        }

        return Arrays.stream(extensions)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }
}