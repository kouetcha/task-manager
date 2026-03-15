package com.kouetcha.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PdfDocumentBundle implements AutoCloseable{
   private Document document;
   private PdfDocument srcDoc;
    private PdfDocument srcDoc1;
    public PdfDocumentBundle(Document document,PdfDocument srcDoc,PdfDocument srcDoc1){
       this.document=document;
       this.srcDoc=srcDoc;
       this.srcDoc1=srcDoc1;
    }
    @Override
    public void close() {
        // Ferme d'abord le document principal
        if (document != null) document.close();
        // Puis les documents sources
        if (srcDoc != null) srcDoc.close();
        if (srcDoc1 != null) srcDoc1.close();
    }
}