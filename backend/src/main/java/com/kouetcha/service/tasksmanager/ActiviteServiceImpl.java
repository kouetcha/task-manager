package com.kouetcha.service.tasksmanager;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.dto.tasksmanager.websocket.NotificationEvent;
import com.kouetcha.model.enums.Type;
import com.kouetcha.model.enums.TypeEvent;
import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.model.tasksmanager.EmailActivite;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
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
public class ActiviteServiceImpl implements ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final ProjetRepository projetRepository;
    private final EmailActiviteServiceImpl emailActiviteService;
    private final UtilisateurRepository utilisateurRepository;
    private final FichierActiviteServiceImpl fichierActiviteService;
    private final FichierService<FichierEntityGestion, Activite> fichierService;
    private final WebSocketService webSocketService;
    private final NotificationService notificationService;

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
            List<String> emails=new ArrayList<>();
            log.info(String.valueOf(dto.getEmails()));
            for(String email:dto.getEmails()) {
                if(!email.isBlank())
                { log.info("Email::");
                    log.info(email.strip());
                    emails.add(email.strip());
                    emailActivites.add(emailActiviteService.addEmail(activite.getId(),email.strip()));
                }
            }
            if(activite.getEmails().isEmpty()) {

                activite.getEmails().addAll(emailActivites);
            }
            List<Utilisateur>utilisateurs=utilisateurRepository.findByEmailIn(emails);
            createActiviteNoti(utilisateurs,activite);
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
        updateActiviteNoti(activite,"L'activité : *" + activite.getDesignation()+"* a été mise à jour");
        return activiteRepository.save(activite);
    }
    @Override
    public Activite updateDesignation(Long id, @Valid TexteDto dto) {

        Activite activite = retrieveActivite(id);
        if(dto.getTexte()==null||dto.getTexte().isEmpty()){
            throw new IllegalArgumentException("La désignation ne peut pas etre nulle");
        }
        activite.setDesignation(dto.getTexte());
         updateActiviteNoti(activite,"La désignation de l'activité * "+activite.getDesignation()+"* a été mise à jour");
        return activiteRepository.save(activite);
    }
    @Override
    public Activite updateDescription(Long id, @Valid TexteDto dto) {
        Activite activite = retrieveActivite(id);

        activite.setDescription(dto.getTexte().strip());
        updateActiviteNoti(activite,"La description de l'activité * "+activite.getDesignation()+"* a été mise à jour");

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
        updateActiviteNoti(activite,"La date de début de l'activité * "+activite.getDesignation()+"* a été mise à jour");

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
        updateActiviteNoti(activite,"La date de fin de l'activité * "+activite.getDesignation()+"* a été mise à jour");
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


    private void sendAndPersistNotification(Utilisateur emetteur,
                                            Utilisateur recepteur,
                                            String message,
                                            Long parentId,
                                            TypeEvent eventType) {

        NotificationDto dto = new NotificationDto()
                .setEmetteur(emetteur)
                .setReceveur(recepteur)
                .setType(Type.ACTIVITE)
                .setMessage(message)
                .setParentId(parentId)
                .setEvent(eventType);

        notificationService.create(dto);
    }
    private NotificationEvent buildActiviteEvent(TypeEvent type, Activite activite, String message) {
        return NotificationEvent.builder()
                .type(type)
                .message(message)
                .entiteId(activite.getId())
                .entiteType(Type.ACTIVITE)
                .timestamp(Instant.now())
                .build();
    }
    private void createActiviteNoti(List<Utilisateur> utilisateurs, Activite activite) {

        if (utilisateurs == null || utilisateurs.isEmpty()) return;

        String message = "Vous avez été associé à l'activité : *" + activite.getDesignation() + "*";
        NotificationEvent event = buildActiviteEvent(TypeEvent.ACTIVITE_ASSIGNEE, activite, message);

        Utilisateur emetteur = UserContext.getUtilisaeurConnecte();

        utilisateurs.forEach(user -> {
            String email = user.getEmail().strip().toLowerCase();

            webSocketService.sendNotification(email, event);
            sendAndPersistNotification(emetteur, user, message, activite.getId(), TypeEvent.ACTIVITE_ASSIGNEE);
        });
    }
    private void updateActiviteNoti(Activite activite, String message) {

        if (activite.getEmails() == null || activite.getEmails().isEmpty()) return;

        NotificationEvent event = buildActiviteEvent(TypeEvent.ACTIVITE_MODIFIEE, activite, message);

        List<String> emails = activite.getEmails().stream()
                .filter(EmailActivite::isActive)
                .map(EmailActivite::getEmail)
                .filter(Objects::nonNull)
                .map(e -> e.strip().toLowerCase())
                .distinct()
                .toList();

        if (emails.isEmpty()) return;

        log.info("Mise à jour activite {} - {}", activite.getDesignation(), activite.getId());


        List<Utilisateur> utilisateurs = utilisateurRepository.findByEmailIn(emails);

        Utilisateur emetteur = UserContext.getUtilisaeurConnecte();

        utilisateurs.forEach(user -> {
            String email = user.getEmail().strip().toLowerCase();

            webSocketService.sendNotification(email, event);
            sendAndPersistNotification(emetteur, user, message, activite.getId(), TypeEvent.ACTIVITE_MODIFIEE);
        });

        webSocketService.sendActiviteUpdate(activite.getId(), event);
    }


}