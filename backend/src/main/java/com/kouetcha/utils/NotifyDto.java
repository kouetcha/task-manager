package com.kouetcha.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotifyDto {
    Long personnelId;
    String email;
    String objet;
    String message;
    String messageSimple;

    public NotifyDto(Long personnelId, String email, String objet, String message, String messageSimple){
     this.personnelId=personnelId;
     this.email=email;
     this.objet=objet;
     this.message=message;
     this.messageSimple=messageSimple;
    }
}