package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.EmailActivite;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ActiviteServiceImpl implements ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final ProjetRepository projetRepository;
    private final EmailActiviteServiceImpl emailActiviteService;
    private final UtilisateurRepository utilisateurRepository;
    private final FichierActiviteServiceImpl fichierActiviteService;
    private final FichierService<FichierEntityGestion, Activite> fichierService;

    @Override
    public Activite create(BaseEntityGestionDto dto, Long projetId) {
        Utilisateur createur = recupererUtilisateur(dto.getCreateurId());
        Projet projet = recupererProjet(projetId);

        verifierConditionCreation(dto, projet);

        Activite activite = new Activite();
        activite.setProjet(projet);
        activite.setCreateur(createur)
                .setDesignation(dto.getDesignation())
                .setDescription(dto.getDescription())
                .setDateDebut(dto.getDateDebut())
                .setDateFin(dto.getDateFin())
                .setStatus(dto.getStatus())
        ;

        activite = activiteRepository.save(activite);
        if (dto.getFichiers() != null && !dto.getFichiers().isEmpty()) {
            List<FichierEntityGestion>fichierEntityGestions=new ArrayList<>();
            for (FichierDTO fichierDTO : dto.getFichiers()) {
                fichierEntityGestions.add(fichierService.upload(activite.getId(),
                        fichierDTO.getFichier(),
                        fichierDTO.getNomFichier()));
            }
            activite.setFichiers(fichierEntityGestions);
        }

        if(dto.getEmails()!=null&&!dto.getEmails().isEmpty())
        {  List<EmailActivite>emailActivites=new ArrayList<>();
            log.info(String.valueOf(dto.getEmails()));
            for(String email:dto.getEmails()) {
                if(!email.isBlank())
                { log.info("Email::");
                    log.info(email.strip());
                    emailActivites.add(emailActiviteService.addEmail(activite.getId(),email.strip()));
                }
            }
            if(activite.getEmails().isEmpty()) {

                activite.getEmails().addAll(emailActivites);
            }
        }
        return activite;
    }

    @Override
    public Activite update(Long id, @Valid BaseEntityGestionDto dto) {

        Activite activite = retrieveActivite(id);
        Utilisateur utilisateur = recupererUtilisateur(dto.getCreateurId());

        activite.setCreateur(utilisateur)
                .setDesignation(dto.getDesignation() != null && !dto.getDesignation().isEmpty()
                        ? dto.getDesignation() : activite.getDesignation())
                .setDescription(dto.getDescription() != null && !dto.getDescription().isEmpty()
                        ? dto.getDescription() : activite.getDescription())
                .setDateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : activite.getDateDebut())
                .setDateFin(dto.getDateFin() != null ? dto.getDateFin() : activite.getDateFin())
                .setStatus(dto.getStatus() != null ? dto.getStatus() : activite.getStatus());

        if (dto.getFichiers() != null && !dto.getFichiers().isEmpty()) {
            for (FichierDTO fichierDTO : dto.getFichiers()) {
                fichierActiviteService.upload(activite.getId(),
                        fichierDTO.getFichier(),
                        fichierDTO.getNomFichier());
            }
        }
        if(dto.getEmails()!=null&&!dto.getEmails().isEmpty())
        {  for(String email:dto.getEmails()) {
            if(!email.isBlank())
            { log.info("Email::");
                log.info(email.strip());
                emailActiviteService.addEmail(activite.getId(),email.strip() );
            }
        }
        }

        return activiteRepository.save(activite);
    }
    @Override
    public Activite updateDesignation(Long id, @Valid TexteDto dto) {

        Activite activite = retrieveActivite(id);
        if(dto.getTexte()==null||dto.getTexte().isEmpty()){
            throw new IllegalArgumentException("La désignation ne peut pas etre nulle");
        }
        activite.setDesignation(dto.getTexte());

        return activiteRepository.save(activite);
    }
    @Override
    public Activite updateDescription(Long id, @Valid TexteDto dto) {
        Activite activite = retrieveActivite(id);

        activite.setDescription(dto.getTexte().strip());

        return activiteRepository.save(activite);
    }
    @Override
    public Activite updateDateDebut(Long id, @Valid DateDto dto) {
        Activite activite = retrieveActivite(id);

        Date nouvelleDateDebut = dto.getDate();

        // Vérification : dateDebut doit être avant dateFin si dateFin existe
        if (activite.getDateFin() != null && !nouvelleDateDebut.before(activite.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin.");
        }

        activite.setDateDebut(nouvelleDateDebut);
        return activiteRepository.save(activite);
    }

    @Override
    public Activite updateDateFin(Long id, @Valid DateDto dto) {
        Activite activite = retrieveActivite(id);

        Date nouvelleDateFin = dto.getDate();


        if (activite.getDateDebut() != null && !nouvelleDateFin.after(activite.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }

        activite.setDateFin(nouvelleDateFin);
        return activiteRepository.save(activite);
    }
    @Override
    public void delete(Long id) {
        Activite activite = retrieveActivite(id);
        fichierService.deleteAllByParentId(activite.getId());
        activiteRepository.delete(activite);
    }

    @Override
    public void addFichier(Long activiteId, FichierDTO fichierDTO) {
        fichierService.upload(activiteId, fichierDTO.getFichier(), fichierDTO.getNomFichier());
    }

    @Override
    public void deleteFichier(Long activiteId, Long fichierId) {
        retrieveActivite(activiteId);
        fichierService.deleteById(fichierId);
    }

    @Override
    public Activite findById(Long id) {
        return activiteRepository.findById(id).orElse(null);
    }

    @Override
    public List<Activite> findByProjetId(Long projetId) {
        return activiteRepository.findByProjetId(projetId);
    }
    @Override
    public List<Activite> findByProjetIdAndEmail(Long projetId, String email) {
        return activiteRepository.findByProjetIdAndEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrProjetIdAndCreateurEmail(projetId,email,projetId,email);
    }
    @Override
    public Page<Activite> findByEmail(String email, Pageable pageable) {
        return activiteRepository.findDistinctByEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrCreateurEmail(email, email,pageable);
    }

    @Override
    public List<BaseEntityDto>findDtoActiveByEmail(String email){
        return activiteRepository.findActiveByEmail(email);
    }
    @Override
    public Page<BaseEntityDto>findDtoActiveByEmail(String email, Pageable pageable){
        return activiteRepository.findActiveByEmail(email,pageable);
    }
    @Override
    public long countDtoActiveByEmail(String email){
        return activiteRepository.countActiveByEmail(email);
    }

    private Activite retrieveActivite(Long id) {
        return activiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activité introuvable"));
    }

    private Projet recupererProjet(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable"));
    }

    private Utilisateur recupererUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    private void verifierConditionCreation(BaseEntityGestionDto dto, Projet projet) {
        boolean existe = activiteRepository.existsByDesignationAndCreateurIdAndProjetId(
                dto.getDesignation(), dto.getCreateurId(), projet.getId());
        if (existe) {
            throw new IllegalArgumentException("Une activité avec cette désignation existe déjà dans ce projet");
        }
    }
}