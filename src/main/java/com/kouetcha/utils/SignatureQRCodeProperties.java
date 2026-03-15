package com.kouetcha.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "signature.qr")
public class SignatureQRCodeProperties {

    private float width;
    private float height;
    private float marginX;
    private float marginY;
}