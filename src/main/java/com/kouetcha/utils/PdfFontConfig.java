package com.kouetcha.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;




@Component
public class PdfFontConfig {

    @Value("${pdf.fonts.bold}")
    private String bold;

    @Value("${pdf.fonts.regular}")
    private String regular;

    @Value("${pdf.fonts.italic}")
    private String italic;

    @PostConstruct
    public void init() {
        PdfDocumentUtil.setFontPathBold(bold);
        PdfDocumentUtil.setFontPathRegular(regular);
        PdfDocumentUtil.setFontPathItalic(italic);
    }
}