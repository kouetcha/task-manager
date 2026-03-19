package com.kouetcha.controller.tasksmanager;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.tasksmanager.CountMenuDto;
import com.kouetcha.dto.tasksmanager.DashboardDto;
import com.kouetcha.security.service.UserDetailsImpl;
import com.kouetcha.service.tasksmanager.StatistiqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Statistiques")
@RequiredArgsConstructor
@RequestMapping("statistiques")
public class StatistiqueController {
   private final StatistiqueService service;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal UserDetails userDetails) throws IllegalAccessException {
        if(UserContext.getUtilisaeurConnecte()==null){
            throw new IllegalAccessException("Vous n'avez pas le droit d'acceder à la ressource");
        }
        Long userId = UserContext.getUtilisaeurConnecte().getId();
        String userEmail=UserContext.getUtilisaeurConnecte().getEmail();
        return ResponseEntity.ok(service.getDashboard(userId,userEmail));
    }
    @GetMapping("/menu")
    public ResponseEntity<CountMenuDto> getMenu(@AuthenticationPrincipal UserDetails userDetails) throws IllegalAccessException {
        if(UserContext.getUtilisaeurConnecte()==null){
            throw new IllegalAccessException("Vous n'avez pas le droit d'acceder à la ressource");
        }
        String userEmail=UserContext.getUtilisaeurConnecte().getEmail();
        return ResponseEntity.ok(service.recupereMenuCount(userEmail));
    }
}
