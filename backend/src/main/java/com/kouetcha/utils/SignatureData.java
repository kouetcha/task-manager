package com.kouetcha.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureData {
    private String title;
    private String name;
    private byte[] imagePath;

    // Constructors
    public SignatureData() {}

    public SignatureData(String title, String name, byte[] imagePath) {
        this.title = title;
        this.name = name;
        this.imagePath = imagePath;
    }

}