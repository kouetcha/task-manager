package com.kouetcha.service.tasksmanager;
import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;

import com.kouetcha.model.tasksmanager.Tache;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;

import com.kouetcha.repository.tasksmanager.TacheRepository;
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
public class TacheServiceImpl implements TacheService {

    private final TacheRepository tacheRepository;
    private final ActiviteRepository activiteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailTacheServiceImpl emailTacheService;
    private final FichierService<FichierEntityGestion, Tache> fichierService;

    @Override
    public Tache create(BaseEntityGestionDto dto, Long activiteId) {
        Utilisateur createur = recupererUtilisateur(dto.getCreateurId());
        Activite activite = recupererActivite(activiteId);

        verifierConditionCreation(dto, activite);

        Tache tache = new Tache();
        tache.setActivite(activite);
        tache.setCreateur(createur)
                .setDesignation(dto.getDesignation())
                .setDescription(dto.getDescription())
                .setDateDebut(dto.getDateDebut())
                .setDateFin(dto.getDateFin())
                .setStatus(dto.getStatus());

        tache = tacheRepository.save(tache);


        if (dto.getFichiers() != null && !dto.getFichiers().isEmpty()) {
            List<FichierEntityGestion>fichierEntityGestions=new ArrayList<>();
            for (FichierDTO fichierDTO : dto.getFichiers()) {
                fichierEntityGestions.add(fichierService.upload(tache.getId(),
                        fichierDTO.getFichier(),
                        fichierDTO.getNomFichier()));
            }
            tache.setFichiers(fichierEntityGestions);
        }
        if(dto.getEmails()!=null&&!dto.getEmails().isEmpty())
        {  List<EmailTache>emailTaches=new ArrayList<>();
            for(String email:dto.getEmails()) {
            if(!email.isBlank())
            { log.info("Email::");
                log.info(email.strip());
              emailTaches.add( emailTacheService.addEmail(tache.getId(),email.strip() ));
            }
        }
            if(tache.getEmails().isEmpty()) {

            tache.getEmails().addAll(emailTaches);
        }
        }


        return tache;
    }

    @Override
    public Tache update(Long id, BaseEntityGestionDto dto) {
        Tache tache = retrieveTache(id);
        Utilisateur createur = recupererUtilisateur(dto.getCreateurId());

        tache.setCreateur(createur)
                .setDesignation(dto.getDesignation() != null ? dto.getDesignation() : tache.getDesignation())
                .setDescription(dto.getDescription() != null ? dto.getDescription() : tache.getDescription())
                .setDateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : tache.getDateDebut())
                .setDateFin(dto.getDateFin() != null ? dto.getDateFin() : tache.getDateFin())
                .setStatus(dto.getStatus() != null ? dto.getStatus() : tache.getStatus());

        if (dto.getFichiers() != null) {
            for (FichierDTO f : dto.getFichiers()) {
                fichierService.upload(tache.getId(), f.getFichier(), f.getNomFichier());
            }
        }

        return tacheRepository.save(tache);
    }

    @Override
    public Tache updateDesignation(Long id, @Valid TexteDto dto) {

        Tache tache = retrieveTache(id);
        if(dto.getTexte()==null||dto.getTexte().isEmpty()){
            throw new IllegalArgumentException("La désignation ne peut pas etre nulle");
        }
        tache.setDesignation(dto.getTexte());

        return tacheRepository.save(tache);
    }
    @Override
    public Tache updateDescription(Long id, @Valid TexteDto dto) {
        Tache tache = retrieveTache(id);

        tache.setDescription(dto.getTexte().strip());

        return tacheRepository.save(tache);
    }
    @Override
    public Tache updateDateDebut(Long id, @Valid DateDto dto) {
        Tache tache = retrieveTache(id);

        Date nouvelleDateDebut = dto.getDate();

        // Vérification : dateDebut doit être avant dateFin si dateFin existe
        if (tache.getDateFin() != null && !nouvelleDateDebut.before(tache.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin.");
        }

        tache.setDateDebut(nouvelleDateDebut);
        return tacheRepository.save(tache);
    }

    @Override
    public Tache updateDateFin(Long id, @Valid DateDto dto) {
        Tache tache = retrieveTache(id);

        Date nouvelleDateFin = dto.getDate();


        if (tache.getDateDebut() != null && !nouvelleDateFin.after(tache.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }

        tache.setDateFin(nouvelleDateFin);
        return tacheRepository.save(tache);
    }

    @Override
    public void delete(Long id) {
        Tache tache = retrieveTache(id);
        fichierService.deleteAllByParentId(tache.getId());
        tacheRepository.delete(tache);
    }

    @Override
    public void addFichier(Long tacheId, FichierDTO fichierDTO) {
        fichierService.upload(tacheId, fichierDTO.getFichier(), fichierDTO.getNomFichier());
    }

    @Override
    public void deleteFichier(Long tacheId, Long fichierId) {
        retrieveTache(tacheId);
        fichierService.deleteById(fichierId);
    }

    @Override
    public Page<Tache> findByEmail(String email, Pageable pageable) {
        return tacheRepository.findDistinctByEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrCreateurEmail(email,email, pageable);
    }
    @Override
    public List<BaseEntityDto>findDtoActiveByEmail(String email){
        return tacheRepository.findActiveByEmail(email);
    }
    @Override
    public Page<BaseEntityDto>findDtoActiveByEmail(String email, Pageable pageable){
        return tacheRepository.findActiveByEmail(email,pageable);
    }
    @Override
    public long countDtoActiveByEmail(String email){
        return tacheRepository.countActiveByEmail(email);
    }

    @Override
    public Tache findById(Long id) {
        return tacheRepository.findById(id).orElse(null);
    }

    @Override
    public List<Tache> findByActiviteId(Long activiteId) {
        return tacheRepository.findByActiviteId(activiteId);
    }
    @Override
    public List<Tache> findByActiviteIdAndEmail(Long activiteId, String email) {
        return tacheRepository.findByActiviteIdAndEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrActiviteIdAndCreateurEmail(activiteId,email,activiteId,email);
    }

    private Tache retrieveTache(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));
    }

    private Activite recupererActivite(Long id) {
        return activiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activité introuvable"));
    }

    private Utilisateur recupererUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    private void verifierConditionCreation(BaseEntityGestionDto dto, Activite activite) {
        boolean existe = tacheRepository.existsByDesignationAndCreateurIdAndActiviteId(
                dto.getDesignation(), dto.getCreateurId(), activite.getId());
        if (existe) {
            throw new IllegalArgumentException("Une tâche avec cette désignation existe déjà dans cette activité");
        }
    }
    
    
}