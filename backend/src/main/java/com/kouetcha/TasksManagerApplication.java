package com.kouetcha;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.utilisateur.UtilisateurDto;
import com.kouetcha.model.enums.UserCategory;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication()
//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@EnableSpringDataWebSupport(
    pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class TasksManagerApplication implements ApplicationRunner {
   private final UtilisateurService utilisateurService;
   @Value("${admin.email}")
   private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;
   
    public static void main(String[] args) {
        SpringApplication.run(TasksManagerApplication.class, args);
    }

    public void run(ApplicationArguments args){
        if (!utilisateurService.existsByEmail(adminEmail)) {
            UserContext.setCurrentUser("admin");
            UtilisateurDto adminDto=new UtilisateurDto();
            adminDto.setEmail(adminEmail);
            adminDto.setMotdepasse(adminPassword);
            adminDto.setNom("KOUETCHA");
            adminDto.setPrenom("Admin");
            Utilisateur administrateur=utilisateurService.create(adminDto);
            administrateur.setAdmin(true);
            administrateur.setCategory(UserCategory.ADMIN);
            utilisateurService.save(administrateur);
            UserContext.clearCurrentUser();
    }
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}