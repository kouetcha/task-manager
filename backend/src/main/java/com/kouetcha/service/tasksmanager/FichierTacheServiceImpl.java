package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.model.tasksmanager.Tache;
import com.kouetcha.repository.tasksmanager.FichierEntityGestionRepository;
import com.kouetcha.repository.tasksmanager.TacheRepository;
import com.kouetcha.utils.FileUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;


@Service
public class FichierTacheServiceImpl
        extends AbstractFichierService<FichierEntityGestion, Tache> {

    private final FichierEntityGestionRepository repository;
    @Value("${media.document.tache}")
    private String mediaDocument;
    public FichierTacheServiceImpl(
            FichierEntityGestionRepository repository,
            TacheRepository tacheRepository) {

        super(repository, tacheRepository);
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
    protected void setParent(FichierEntityGestion fichier, Tache parent) {
        fichier.setTache(parent);
    }

    @Override
    protected List<FichierEntityGestion> findByParentEntity(Tache parent) {
        return repository.findByTache(parent);
    }

    @Override
    protected void processFileStorage(FichierEntityGestion fichier, MultipartFile file,String fichierNom)
           {

        String fileNom = fichierNom!=null?fichierNom:file.getOriginalFilename();

        String fileName=FileUtility.enregistrerFichierWithName(file,mediaDocument,fileNom);


        fichier.setNomFichier(fileNom);
        fichier.setCheminFichier(fileName);
        fichier.setDateUpload(new Date());
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
}
