package com.kouetcha.service.utilisateur;

import com.kouetcha.dto.utilisateur.*;
import com.kouetcha.model.enums.UserCategory;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import com.kouetcha.security.jwt.JwtUtils;
import com.kouetcha.utils.FileUtility;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Path;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService{
  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  @Value("${media.user-profil}")
  private String mediaUserProfil;
  @Override
  public Utilisateur create(@Valid UtilisateurDto dto){
      if(dto.getCategory()==null){
          dto.setCategory(UserCategory.NORMAL);
      }
    if(utilisateurRepository.existsByEmail(dto.getEmail())){
       throw new IllegalArgumentException("Le mail est déjà utilisé" );
    }
    Utilisateur utilisateur=new Utilisateur();
    utilisateur
            .setNom(dto.getNom())
            .setPrenom(dto.getPrenom())
            .setEmail(dto.getEmail())
            .setTelephone(dto.getTelephone())
            .setCategory(dto.getCategory())
            .setRoleProjet(dto.getRoleProjet())
            .setMotdepasse(passwordEncoder.encode(dto.getMotdepasse()));
    return utilisateurRepository.save(utilisateur);

  }


@Override
  public Utilisateur update(Long id, @Valid UtilisateurUpdateDto dto){
        Utilisateur utilisateur=trouverUtilisateurParId(id);
      utilisateur
              .setNom(dto.getNom())
              .setPrenom(dto.getPrenom())
              .setEmail(dto.getEmail())
              .setTelephone(dto.getTelephone());

    return utilisateurRepository.save(utilisateur);
  }
  @Override
  public Utilisateur modifierProfilPicture(Long userId, MultipartFile photo){
      Utilisateur utilisateur=recupererUtilisateur(userId);
      System.out.println("IMAGE PATH:: "+userId);
      String ancienChemin=utilisateur.getProfilePicture();
      if (photo!=null){
          String imagePath=saveImage(photo);
          utilisateur.setProfilePicture(imagePath);

          if(ancienChemin!=null) {
              FileUtility.supprimerFichier(mediaUserProfil, ancienChemin);
          }
      }

      utilisateur= utilisateurRepository.save(utilisateur);
      System.out.println("IMAGE PATH:: "+utilisateur.getProfilePicture());
      return utilisateur;
  }
    @Override
    public ResponseEntity<Resource> recupererImage(String fileCode){
        return FileUtility.downloadFile(Path.of(mediaUserProfil + fileCode));
    }
    private String saveImage(MultipartFile image){
        return   FileUtility.enregistrerFichierWithName(image,mediaUserProfil,"");
    }
  private Utilisateur recupererUtilisateur(Long id){
      return utilisateurRepository.findById(id).orElseThrow(
              ()->new IllegalArgumentException("Utilisateur non trouvé")
      );
  }
  @Override
  public Utilisateur changeEtat(Long id){
      Utilisateur utilisateur=trouverUtilisateurParId(id);
      utilisateur.setEstActif(!utilisateur.isEstActif());
      return utilisateurRepository.save(utilisateur);
  }
  @Override
  public void delete(Long id){
      Utilisateur utilisateur=trouverUtilisateurParId(id);
      utilisateurRepository.delete(utilisateur);
  }
  @Override
    public Utilisateur updatePassWord(@Valid MotdePasseDto dto) {
        Utilisateur utilisateur = trouverUtilisateurParEmail(dto.getEmail());


        if (!passwordEncoder.matches(dto.getAncienmotdepasse(), utilisateur.getMotdepasse())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect.");
        }

        utilisateur.setMotdepasse(passwordEncoder.encode(dto.getMotdepasse()));

        return utilisateurRepository.save(utilisateur);
    }
    @Override
    public AuthentificationDto seConnecter(@Valid ConnexionDto dto) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getMotdepasse())
            );


            Utilisateur utilisateur = trouverUtilisateurParEmail(dto.getEmail());

            // Générer le token JWT
            String token = jwtUtils.generateToken(utilisateur);

            return new AuthentificationDto(dto.getEmail(), token);

        } catch (Exception ex) {
            throw new IllegalArgumentException("Le mail ou le mot de passe est incorrect:  "+ex);
        }
    }


    @Override
    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id).orElse(null);
    }
    @Override
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String mail) {
        return utilisateurRepository.existsByEmail(mail);
    }

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    private Utilisateur trouverUtilisateurParId(Long id){
      return  utilisateurRepository.findById(id).orElseThrow(
              ()->new IllegalArgumentException("Utilisateur non trouvé")
      );
  }
    private Utilisateur trouverUtilisateurParEmail(String email){
        return  utilisateurRepository.findByEmail(email).orElseThrow(
                ()->new IllegalArgumentException("Utilisateur non trouvé")
        );
    }
}