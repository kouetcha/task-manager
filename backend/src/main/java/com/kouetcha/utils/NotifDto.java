package com.kouetcha.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotifDto {
    Long personnelId;
    String email;
    String objet;
    String message;

    public NotifDto(Long personnelId, String email, String objet, String message){
     this.personnelId=personnelId;
     this.email=email;
     this.objet=objet;
     this.message=message;
    }
}