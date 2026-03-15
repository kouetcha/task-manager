package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.tasksmanager.*;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.model.tasksmanager.FichierCommentaire;
import com.kouetcha.repository.tasksmanager.CommentaireActiviteRepository;
import com.kouetcha.repository.tasksmanager.FichierCommentaireRepository;
import com.kouetcha.utils.FileUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FichierCommentaireActiviteServiceImpl
        extends AbstractFichierService<FichierCommentaire, CommentaireActivite> {
    private final CommentaireActiviteServiceImpl commentaireActiviteService;

    private final FichierCommentaireRepository repository;
    @Value("${media.commentaire.activite}")
    private String mediaDocument;
    public FichierCommentaireActiviteServiceImpl(
            FichierCommentaireRepository repository,
             CommentaireActiviteRepository activiteRepository,
            @Lazy CommentaireActiviteServiceImpl commentaireActiviteService) {

        super(repository, activiteRepository);
        this.repository = repository;
        this.commentaireActiviteService = commentaireActiviteService;
    }
    @Override
    protected void evictParentCache(Long commentaireId) {
        CommentaireActivite commentaire = parentRepository.findById(commentaireId)
                .orElseThrow();
        commentaireActiviteService.evictCache(commentaire.getActivite().getId());
        log.info("[CACHE EVICT] fichier activite → activiteId={}", commentaire.getActivite().getId());
    }
    @Override
    protected FichierCommentaire createEntity() {
        return new FichierCommentaire();
    }

    @Override
    protected void setParent(FichierCommentaire fichier, CommentaireActivite parent) {
        fichier.setCommentaireActivite(parent);
    }

    @Override
    protected List<FichierCommentaire> findByParentEntity(CommentaireActivite parent) {
        return repository.findByCommentaireActivite(parent);
    }
    @Override
    public FichierCommentaire upload(Long parentId, MultipartFile file, String fichierNom) {

        CommentaireActivite parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        try {
            FichierCommentaire fichier = createEntity();
            fichier.setCommentaireActivite(parent);

            processFileStorage(fichier, file,fichierNom);

            fichier= fichierRepository.save(fichier);
            evictParentCache(parentId);
            return fichier;


        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur upload fichier: "+e.getMessage());
        }
    }
    @Override
    protected void processFileStorage(FichierCommentaire fichier, byte[] fileBytes) {

        try {

            String nomFinal = fichier.getNomFichier();

            Path directory = Paths.get(mediaDocument);
            Files.createDirectories(directory);

            Path filePath = directory.resolve(nomFinal);

            Files.write(filePath, fileBytes);

            fichier.setCheminFichier(filePath.toString());
            fichier.setDateUpload(new Date());

        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage fichier", e);
        }
    }

    @Override
    protected void processFileStorage(FichierCommentaire fichier,
                                      MultipartFile file,
                                      String fichierNom) {

        String nomFinal = resolveFileName(file, fichierNom);

        String cheminStockage = FileUtility.enregistrerFichierWithName(
                file,
                mediaDocument,
                nomFinal
        );

        fichier.setNomFichier(nomFinal);
        fichier.setCheminFichier(cheminStockage);
        fichier.setDateUpload(new Date());
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String fileCode) {
        Path path = Path.of(mediaDocument, fileCode);
        return FileUtility.downloadFileOnlyOffice(path);
    }

    @Override
    public void deleteAllByParentId(Long parentId) {

        List<FichierCommentaire> fichiers =
                repository.findByCommentaireActiviteId(parentId);

        deleteFilesAndEntities(fichiers);
        evictParentCache(parentId);
    }

    @Override
    public void deleteById(Long fichierId) {

        FichierCommentaire fichier = repository.findById(fichierId)
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable"));
        Long parentId = fichier.getParentId();
        deleteFilesAndEntities(List.of(fichier));
        evictParentCache(parentId);
    }



    /* ==============================
            MÉTHODES FACTORISÉES
       ============================== */

    private String resolveFileName(MultipartFile file, String fichierNom) {
        return (fichierNom != null && !fichierNom.isBlank())
                ? fichierNom
                : file.getOriginalFilename();
    }

    private void deleteFilesAndEntities(List<FichierCommentaire> fichiers) {

        List<String> chemins = fichiers.stream()
                .map(FichierCommentaire::getCheminFichier)
                .toList();

        repository.deleteAll(fichiers);

        chemins.forEach(chemin ->
                FileUtility.supprimerFichier(mediaDocument, chemin));
    }
}
