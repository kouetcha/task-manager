package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireResponseDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.dto.tasksmanager.FichierDTO;
import com.kouetcha.model.tasksmanager.*;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.tasksmanager.CommentaireActiviteRepository;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import com.kouetcha.utils.FileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentaireActiviteServiceImpl
        extends AbstractCommentaireService<CommentaireActivite, Activite> {

    private static final String CACHE = "commentaires-activite";

    private final CommentaireActiviteRepository repository;
    private final FichierCommentaireActiviteServiceImpl fichierCommentaireActiviteService;

    @Autowired
    @Lazy
    private CommentaireActiviteServiceImpl self;

    @Value("${media.commentaire.activite}")
    private String mediaCommentaire;

    public CommentaireActiviteServiceImpl(
            CommentaireActiviteRepository repository,
            ActiviteRepository activiteRepository,
            UtilisateurRepository utilisateurRepository,
            FichierCommentaireActiviteServiceImpl fichierCommentaireActiviteService) {
        super(repository, activiteRepository, utilisateurRepository);
        this.repository = repository;
        this.fichierCommentaireActiviteService = fichierCommentaireActiviteService;
    }

    @Override
    @CacheEvict(value = CACHE, key = "#result.activite.id")
    public CommentaireActivite create(CommentaireDto dto) {

        Activite parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        Utilisateur auteur = utilisateurRepository.findById(dto.getAuteurId())
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        CommentaireActivite commentaire = createEntity();
        commentaire.setContenu(dto.getContenu());
        commentaire.setDate(new Date());
        commentaire.setAuteur(auteur);

        setParent(commentaire, parent);
        commentaire = commentaireRepository.save(commentaire);

        if (dto.getFichiers() != null) {
            List<FichierCommentaire> fichierCommentaires = new ArrayList<>();
            for (FichierDTO fileDto : dto.getFichiers()) {
                fichierCommentaires.add(
                        fichierCommentaireActiviteService.upload(
                                commentaire.getId(),
                                fileDto.getFichier(),
                                fileDto.getNomFichier()
                        )
                );
            }
            if (commentaire.getFichiers().isEmpty()) {
                commentaire.getFichiers().addAll(fichierCommentaires);
            }
        }

        return commentaireRepository.save(commentaire);
    }

    @Override
    @CacheEvict(value = CACHE, key = "#result.activite.id")
    public CommentaireActivite changeContenu(CommentaireUpdateDto dto) {
        return super.changeContenu(dto);
    }

    @Override
    public void delete(Long commentaireId) {
        CommentaireActivite commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));
        Long activiteId = commentaire.getActivite().getId();
        commentaireRepository.deleteById(commentaireId);
        self.evictCache(activiteId);
    }

    @CacheEvict(value = CACHE, key = "#activiteId")
    public void evictCache(Long activiteId) {}

    // ✅ Retourne des DTOs → plus de problème Lazy/Session JPA
    @Cacheable(value = CACHE, key = "#parentId")
    public List<CommentaireResponseDto> findByParentDto(Long parentId) {
        List<CommentaireActivite> commentaires = super.findByParent(parentId);
        return commentaires.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Mapper entité → DTO
    private CommentaireResponseDto toDto(CommentaireActivite c) {
        CommentaireResponseDto dto = new CommentaireResponseDto();
        dto.setId(c.getId());
        dto.setContenu(c.getContenu());
        dto.setDate(c.getDate());

        if (c.getAuteur() != null) {
            CommentaireResponseDto.AuteurDto auteurDto = new CommentaireResponseDto.AuteurDto();
            auteurDto.setId(c.getAuteur().getId());
            auteurDto.setNom(c.getAuteur().getNom());
            auteurDto.setPrenom(c.getAuteur().getPrenom());
            auteurDto.setEmail(c.getAuteur().getEmail());
            auteurDto.setProfilePicture(c.getAuteur().getProfilePicture());
            dto.setAuteur(auteurDto);
        }

        if (c.getFichiers() != null) {
            List<CommentaireResponseDto.FichierInfoDto> fichiers = c.getFichiers()
                    .stream()
                    .map(f -> new CommentaireResponseDto.FichierInfoDto(
                            f.getId(),
                            f.getNomFichier(),
                            f.getCheminFichier(),
                            f.getType(),
                            f.getUrl(),
                            f.getCallbackurl()
                    ))
                    .collect(Collectors.toList());
            dto.setFichiers(fichiers);
        }

        return dto;
    }

    @Override
    protected CommentaireActivite createEntity() {
        return new CommentaireActivite();
    }

    @Override
    protected void setParent(CommentaireActivite commentaire, Activite parent) {
        commentaire.setActivite(parent);
    }

    @Override
    protected List<CommentaireActivite> findByParentEntity(Activite parent) {
        return repository.findByActiviteOrderByDateAsc(parent);
    }

    @Override
    public String saveFile(MultipartFile file) {
        return FileUtility.enregistrerFichier(file, mediaCommentaire);
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String fileCode) {
        return FileUtility.downloadFile(Path.of(mediaCommentaire + fileCode));
    }
}