package com.kouetcha.dto.utilisateur;

import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
public class AuthentificationDto {

    private String email;

    private String token;

    public AuthentificationDto(String email,String token){
      this.email=email;
      this.token=token;
    }
}