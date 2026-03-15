package com.kouetcha.utils;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;

public class RoundedBorderCellRenderer extends CellRenderer {
    private boolean roundLeftCorner;
    private boolean roundRightCorner;
    private int raduis;

    public RoundedBorderCellRenderer(Cell modelElement, boolean roundLeftCorner, boolean roundRightCorner, int raduis) {
        super(modelElement);
        this.roundLeftCorner = roundLeftCorner;
        this.roundRightCorner = roundRightCorner;
        this.raduis=raduis;

    }

    @Override
    public void drawBorder(DrawContext drawContext) {
        Rectangle rect = getOccupiedAreaBBox();
        PdfCanvas canvas = drawContext.getCanvas();
        canvas.saveState();

        // Define corner radius
        float radius = raduis;

        // Start drawing the rounded border
        if (roundLeftCorner && roundRightCorner) {
            // Both left and right corners rounded (for top or bottom borders)
            canvas.roundRectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), radius);
        } else if (roundLeftCorner) {
            // Only the left corner rounded
            canvas.moveTo(rect.getX() + radius, rect.getY());
            canvas.lineTo(rect.getX() + rect.getWidth(), rect.getY());
            canvas.lineTo(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
            canvas.lineTo(rect.getX() + radius, rect.getY() + rect.getHeight());
            canvas.arc(rect.getX(), rect.getY() + rect.getHeight() - radius, rect.getX() + radius, rect.getY() + rect.getHeight(), 180, 90);
            canvas.lineTo(rect.getX(), rect.getY() + radius);
            canvas.arc(rect.getX(), rect.getY(), rect.getX() + radius, rect.getY() + radius, 90, 90);
        } else if (roundRightCorner) {
            // Only the right corner rounded
            canvas.moveTo(rect.getX(), rect.getY());
            canvas.lineTo(rect.getX() + rect.getWidth() - radius, rect.getY());
            canvas.arc(rect.getX() + rect.getWidth() - radius, rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + radius, 0, 90);
            canvas.lineTo(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight() - radius);
            canvas.arc(rect.getX() + rect.getWidth() - radius, rect.getY() + rect.getHeight() - radius, rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), 90, 90);
            canvas.lineTo(rect.getX(), rect.getY() + rect.getHeight());
        } else {
            // No rounded corners, draw a regular rectangle
            canvas.rectangle(rect);
        }

        canvas.stroke();
        canvas.restoreState();
    }
}