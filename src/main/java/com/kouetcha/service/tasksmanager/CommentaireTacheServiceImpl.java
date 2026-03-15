package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireResponseDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.dto.tasksmanager.FichierDTO;
import com.kouetcha.model.tasksmanager.*;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.tasksmanager.CommentaireTacheRepository;
import com.kouetcha.repository.tasksmanager.TacheRepository;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import com.kouetcha.utils.FileUtility;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CommentaireTacheServiceImpl
        extends AbstractCommentaireService<CommentaireTache, Tache> {

    private static final String CACHE = "commentaires-tache";

    private final CommentaireTacheRepository repository;
    private final FichierCommentaireTacheServiceImpl fichierCommentaireTacheService;

    @Autowired
    @Lazy
    private CommentaireTacheServiceImpl self;

    @Value("${media.commentaire.tache}")
    private String mediaCommentaire;

    public CommentaireTacheServiceImpl(
            CommentaireTacheRepository repository,
            TacheRepository tacheRepository,
            UtilisateurRepository utilisateurRepository,
            FichierCommentaireTacheServiceImpl fichierCommentaireTacheService) {
        super(repository, tacheRepository, utilisateurRepository);
        this.repository = repository;
        this.fichierCommentaireTacheService = fichierCommentaireTacheService;
    }

    @Override
    @CacheEvict(value = CACHE, key = "#result.tache.id")
    public CommentaireTache create(CommentaireDto dto) {

        Tache parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        Utilisateur auteur = utilisateurRepository.findById(dto.getAuteurId())
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        CommentaireTache commentaire = createEntity();
        commentaire.setContenu(dto.getContenu());
        commentaire.setDate(new Date());
        commentaire.setAuteur(auteur);

        setParent(commentaire, parent);
        commentaire = commentaireRepository.save(commentaire);

        if (dto.getFichiers() != null) {
            List<FichierCommentaire> fichierCommentaires = new ArrayList<>();
            for (FichierDTO fileDto : dto.getFichiers()) {
                fichierCommentaires.add(
                        fichierCommentaireTacheService.upload(
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
    @CacheEvict(value = CACHE, key = "#result.tache.id")
    public CommentaireTache changeContenu(CommentaireUpdateDto dto) {
        return super.changeContenu(dto);
    }

    @Override
    public void delete(Long commentaireId) {
        CommentaireTache commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));
        Long tacheId = commentaire.getTache().getId();
        commentaireRepository.deleteById(commentaireId);
        self.evictCache(tacheId);
    }

    @CacheEvict(value = CACHE, key = "#tacheId")
    public void evictCache(Long tacheId) {}

    // ✅ Retourne des DTOs → plus de problème Lazy/Session JPA

    @Cacheable(value = CACHE, key = "#parentId")
    public List<CommentaireResponseDto> findByParentDto(Long parentId) {
        log.info("[CACHE MISS] Chargement depuis DB pour tacheId={}", parentId);
        List<CommentaireTache> commentaires = super.findByParent(parentId);
        log.info("[CACHE MISS] {} commentaire(s) chargé(s) pour tacheId={}", commentaires.size(), parentId);
        return commentaires.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Mapper entité → DTO
    private CommentaireResponseDto toDto(CommentaireTache c) {
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
    protected CommentaireTache createEntity() {
        return new CommentaireTache();
    }

    @Override
    protected void setParent(CommentaireTache commentaire, Tache parent) {
        commentaire.setTache(parent);
    }

    @Override
    protected List<CommentaireTache> findByParentEntity(Tache parent) {
        return repository.findByTacheOrderByDateAsc(parent);
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