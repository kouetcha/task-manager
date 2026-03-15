package com.kouetcha.utils;

import com.itextpdf.kernel.colors.Color;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class InfoCellDto {
    private String label;
    private String value;
    private Color textColor;
    private Color backgroundColor;

    public InfoCellDto(String label, String value) {
        this(label, value, null, null);
    }

    public InfoCellDto(String label, String value, Color textColor) {
        this(label, value, textColor, null);
    }

    public InfoCellDto(String label, String value, Color textColor, Color backgroundColor) {
        this.label = label;
        this.value = value;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

}