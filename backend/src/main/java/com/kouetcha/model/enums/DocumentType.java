package com.kouetcha.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.beans.PropertyEditorSupport;

public enum DocumentType {
    WORD("word"),
    CELL("cell"),
    SLIDE("slide"),
    PDF("pdf"),
    UNKNOWN("unknown");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DocumentType fromValue(String value) {
        if (value == null) return UNKNOWN;
        for (DocumentType type : DocumentType.values()) {
            if (type.value.equalsIgnoreCase(value) ||
                    type.name().equalsIgnoreCase(value)) { // ← Ajouter cette ligne
                return type;
            }
        }
        return UNKNOWN;
    }

    // Ajouter un PropertyEditorSupport pour Spring MVC
    public static class DocumentTypeEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(DocumentType.fromValue(text));
        }
    }
    /**
     * Retourne le type de document en fonction d'une extension (.docx, .pdf, .xlsx, ...)
     */
    public static DocumentType getTypeByExtension(String extension) {

        if (extension == null || extension.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = extension.trim().toLowerCase();
        if (value.contains(".")) {
            value = value.substring(value.lastIndexOf(".") + 1);
        }

        return switch (value) {
            case "doc", "docx", "odt" -> WORD;
            case "xls", "xlsx", "ods" -> CELL;
            case "ppt", "pptx", "odp" -> SLIDE;
            case "pdf" -> PDF;
            default -> UNKNOWN;
        };

    }
}