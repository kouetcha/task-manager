package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.dto.tasksmanager.websocket.NotificationEvent;
import com.kouetcha.model.enums.Status;

import com.kouetcha.model.enums.Type;
import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import com.kouetcha.service.tasksmanager.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProjetServiceImpl implements ProjetService {

    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FichierService<FichierEntityGestion, Projet> fichierService;
    private final EmailProjetServiceImpl emailProjetService;
    private final WebSocketService webSocketService;

    @Override
    public Projet create(@Valid BaseEntityGestionDto dto) {
        Utilisateur utilisateur = recupererUtilisateur(dto.getCreateurId());
        verifierConditionCreation(dto);

        Projet projet = new Projet();
        projet.setCreateur(utilisateur)
                .setDesignation(dto.getDesignation())
                .setDescription(dto.getDescription())
                .setDateDebut(dto.getDateDebut())
                .setDateFin(dto.getDateFin())
                .setStatus(dto.getStatus());

        projet = projetRepository.save(projet);

        if (dto.getFichiers() != null && !dto.getFichiers().isEmpty()) {
            List<FichierEntityGestion>fichierEntityGestions=new ArrayList<>();
            for (FichierDTO fichierDTO : dto.getFichiers()) {
                fichierEntityGestions.add(fichierService.upload(projet.getId(),
                        fichierDTO.getFichier(),
                        fichierDTO.getNomFichier()));
            }
            projet.setFichiers(fichierEntityGestions);
        }

        if(dto.getEmails()!=null&&!dto.getEmails().isEmpty())
        {  List<EmailProjet>emailProjets=new ArrayList<>();
            List<String> emails=new ArrayList<>();
            log.info(String.valueOf(dto.getEmails()));
            for(String email:dto.getEmails()) {
            if(!email.isBlank())
            {
                emails.add(email.strip());
                emailProjets.add(emailProjetService.addEmail(projet.getId(),email.strip()));
            }
        }
            if(projet.getEmails().isEmpty()) {

                projet.getEmails().addAll(emailProjets);
            }
             Projet finalProjet = projet;
            emails.forEach(((email) ->{
                 createProjetNoti(email.strip(), finalProjet);
             } ));
        }
        return projet;
    }

    @Override
    public Projet update(Long id, @Valid BaseEntityGestionDto dto) {

        Projet projet = retrieveProjet(id);
        Utilisateur utilisateur = recupererUtilisateur(dto.getCreateurId());

        projet.setCreateur(utilisateur)
                .setDesignation(dto.getDesignation() != null && !dto.getDesignation().isEmpty()
                        ? dto.getDesignation() : projet.getDesignation())
                .setDescription(dto.getDescription() != null && !dto.getDescription().isEmpty()
                        ? dto.getDescription() : projet.getDescription())
                .setDateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : projet.getDateDebut())
                .setDateFin(dto.getDateFin() != null ? dto.getDateFin() : projet.getDateFin())
                .setStatus(dto.getStatus() != null ? dto.getStatus() : projet.getStatus());

        if (dto.getFichiers() != null && !dto.getFichiers().isEmpty()) {
            for (FichierDTO fichierDTO : dto.getFichiers()) {
                fichierService.upload(projet.getId(),
                        fichierDTO.getFichier(),
                        fichierDTO.getNomFichier());
            }
        }
        if(dto.getEmails()!=null&&!dto.getEmails().isEmpty())
        {  for(String email:dto.getEmails()) {
            if(!email.isBlank())
            { log.info("Email::");
                log.info(email.strip());
                emailProjetService.addEmail(projet.getId(),email.strip() );
            }
        }
        }
        updateProjetNoti(projet);
        return projetRepository.save(projet);
    }
    private NotificationEvent buildProjetEvent(String type, Projet projet) {
        return NotificationEvent.builder()
                .type(type)
                .message("Projet : *" + projet.getDesignation()+"* mise à jour")
                .entiteId(projet.getId())
                .entiteType(Type.PROJET)
                .timestamp(Instant.now())
                .build();
    }
    private void createProjetNoti(String email, Projet projet) {
        NotificationEvent event = buildProjetEvent("PROJET_ASSIGNE", projet);

        webSocketService.sendNotification(email, event);
    }
    private void updateProjetNoti(Projet projet) {

        NotificationEvent event = buildProjetEvent("PROJET_MODIFIE", projet);
        log.info("Emails:: "+projet.getEmails());

        List<String> emails = projet.getEmails().stream()
                .filter(EmailProjet::isActive) // important
                .map(EmailProjet::getEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .distinct()
                .toList();

        if (emails.isEmpty()) return;



        emails.forEach(email ->
                        webSocketService.sendNotification(email.strip(), event)
                );

        log.info("Mise à jour du projet:: {} {}", projet.getDesignation(), projet.getId());

        // 🔥 broadcast projet
        webSocketService.sendProjetUpdate(projet.getId(), event);
    }

    @Override
    public Projet updateDesignation(Long id, @Valid TexteDto dto) {

        Projet projet = retrieveProjet(id);
        if(dto.getTexte()==null||dto.getTexte().isEmpty()){
            throw new IllegalArgumentException("La désignation ne peut pas etre nulle");
        }
        projet.setDesignation(dto.getTexte());

        projet= projetRepository.save(projet);
        updateProjetNoti(projet);
        return projet;
    }
    @Override
    public Projet updateDescription(Long id, @Valid TexteDto dto) {
        Projet projet = retrieveProjet(id);

        projet.setDescription(dto.getTexte().strip());

        projet= projetRepository.save(projet);
        updateProjetNoti(projet);
        return projet;
    }
    @Override
    public Projet updateDateDebut(Long id, @Valid DateDto dto) {
        Projet projet = retrieveProjet(id);

        Date nouvelleDateDebut = dto.getDate();

        // Vérification : dateDebut doit être avant dateFin si dateFin existe
        if (projet.getDateFin() != null && !nouvelleDateDebut.before(projet.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin.");
        }

        projet.setDateDebut(nouvelleDateDebut);

        projet= projetRepository.save(projet);
        updateProjetNoti(projet);
        return projet;
    }

    @Override
    public Projet updateDateFin(Long id, @Valid DateDto dto) {
        Projet projet = retrieveProjet(id);

        Date nouvelleDateFin = dto.getDate();


        if (projet.getDateDebut() != null && !nouvelleDateFin.after(projet.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }

        projet.setDateFin(nouvelleDateFin);
        projet= projetRepository.save(projet);
        updateProjetNoti(projet);
        return projet;
    }
    @Override
    public void delete(Long id) {
        Projet projet = retrieveProjet(id);

        // Supprimer les fichiers liés
        fichierService.deleteAllByParentId(projet.getId());

        projetRepository.delete(projet);
    }

    @Override
    public void deleteByCreateur(Long userId) {
        List<Projet> projets = projetRepository.findByCreateurId(userId);

        for (Projet projet : projets) {
            fichierService.deleteAllByParentId(projet.getId());
        }

        projetRepository.deleteAll(projets);
    }

    @Override
    public Projet changeStatus(Long id, Status status) {
        Projet projet = retrieveProjet(id);
        projet.setStatus(status);
        return projetRepository.save(projet);
    }

    @Override
    public void addFichier(Long projetId, FichierDTO fichierDTO) {
        Projet projet = retrieveProjet(projetId);

        fichierService.upload(projet.getId(),
                fichierDTO.getFichier(),
                fichierDTO.getNomFichier());
    }

    @Override
    public void deleteFichier(Long projetId, Long fichierId) {
        retrieveProjet(projetId); // Vérifie que le projet existe
        fichierService.deleteById(fichierId);
    }

    @Override
    public Page<Projet> findByEmail(String email, Pageable pageable) {
        return projetRepository.findDistinctByEmailsEmailIgnoreCaseAndEmailsActiveIsTrueOrCreateurEmail(email,email, pageable);
    }
    @Override
    public Projet findById(Long id) {
        return projetRepository.findById(id).orElse(null);
    }

    @Override
    public List<Projet> findByCreateurId(Long userId) {
        return projetRepository.findByCreateurId(userId);
    }
   @Override
   public List<BaseEntityDto>findDtoActiveByEmail(String email){
        return projetRepository.findActiveByEmail(email);
    }
    @Override
    public Page<BaseEntityDto>findDtoActiveByEmail(String email, Pageable pageable){
        return projetRepository.findActiveByEmail(email,pageable);
    }
    @Override
    public long countDtoActiveByEmail(String email){
        return projetRepository.countActiveByEmail(email);
    }
    @Override
    public List<Projet> findAll() {
        return projetRepository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return projetRepository.existsById(id);
    }

    @Override
    public long count() {
        return projetRepository.count();
    }

    private Utilisateur recupererUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Projet retrieveProjet(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private void verifierConditionCreation(BaseEntityGestionDto dto) {
        boolean existeDeja = projetRepository
                .existsByDesignationAndCreateurId(dto.getDesignation(), dto.getCreateurId());

        if (existeDeja) {
            throw new IllegalArgumentException(
                    "Un projet avec la même désignation existe déjà");
        }
    }
}