package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.model.tasksmanager.Activite;
import com.kouetcha.repository.tasksmanager.FichierEntityGestionRepository;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
import com.kouetcha.utils.FileUtility;
import org.springframework.beans.factory.annotation.Value;
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


@Service
public class FichierActiviteServiceImpl
        extends AbstractFichierService<FichierEntityGestion, Activite> {

    private final FichierEntityGestionRepository repository;
    @Value("${media.document.activite}")
    private String mediaDocument;
    public FichierActiviteServiceImpl(
            FichierEntityGestionRepository repository,
            ActiviteRepository activiteRepository) {

        super(repository, activiteRepository);
        this.repository = repository;
    }

    @Override
    protected void evictParentCache(Long parentId) {

    }

    @Override
    protected FichierEntityGestion createEntity() {
        return new FichierEntityGestion();
    }

    @Override
    protected void setParent(FichierEntityGestion fichier, Activite parent) {
        fichier.setActivite(parent);
    }

    @Override
    protected List<FichierEntityGestion> findByParentEntity(Activite parent) {
        return repository.findByActivite(parent);
    }
    @Override
    public FichierEntityGestion upload(Long parentId, MultipartFile file, String fichierNom) {

        Activite parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        try {
            FichierEntityGestion fichier = createEntity();
            fichier.setActivite(parent);

            processFileStorage(fichier, file,fichierNom);

            return fichierRepository.save(fichier);

        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur upload fichier: "+e.getMessage());
        }
    }


    @Override
    protected void processFileStorage(FichierEntityGestion fichier,
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
    protected void processFileStorage(FichierEntityGestion fichier, byte[] fileBytes) {

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
    public ResponseEntity<Resource> downloadFile(String fileCode) {
        Path path = Path.of(mediaDocument, fileCode);
        return FileUtility.downloadFileOnlyOffice(path);
    }

    @Override
    public void deleteAllByParentId(Long parentId) {

        List<FichierEntityGestion> fichiers =
                repository.findByProjetId(parentId);

        deleteFilesAndEntities(fichiers);
    }

    @Override
    public void deleteById(Long fichierId) {

        FichierEntityGestion fichier = repository.findById(fichierId)
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable"));

        deleteFilesAndEntities(List.of(fichier));
    }

    /* ==============================
            MÉTHODES FACTORISÉES
       ============================== */

    private String resolveFileName(MultipartFile file, String fichierNom) {
        return (fichierNom != null && !fichierNom.isBlank())
                ? fichierNom
                : file.getOriginalFilename();
    }

    private void deleteFilesAndEntities(List<FichierEntityGestion> fichiers) {

        List<String> chemins = fichiers.stream()
                .map(FichierEntityGestion::getCheminFichier)
                .toList();

        repository.deleteAll(fichiers);

        chemins.forEach(chemin ->
                FileUtility.supprimerFichier(mediaDocument, chemin));
    }
}
