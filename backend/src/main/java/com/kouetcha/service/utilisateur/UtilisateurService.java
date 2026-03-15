package com.kouetcha.service.utilisateur;

import com.kouetcha.dto.utilisateur.*;
import com.kouetcha.model.utilisateur.Utilisateur;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UtilisateurService {
    Utilisateur create(@Valid UtilisateurDto dto);

    Utilisateur update(Long id, @Valid UtilisateurUpdateDto dto);

    Utilisateur modifierProfilPicture(Long userId, MultipartFile photo);

    ResponseEntity<Resource> recupererImage(String fileCode);

    Utilisateur changeEtat(Long id);

    void delete(Long id);

    Utilisateur updatePassWord(@Valid MotdePasseDto dto);

    AuthentificationDto seConnecter(@Valid ConnexionDto dto);

    List<Utilisateur> findAll();

    Utilisateur findById(Long id);

    Utilisateur findByEmail(String email);

    boolean existsByEmail(String mail);

    Utilisateur save(Utilisateur utilisateur);
}