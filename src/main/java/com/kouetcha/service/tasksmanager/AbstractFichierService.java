package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.base.BaseVS;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Slf4j
@Transactional
public abstract class AbstractFichierService<F extends BaseVS, T>
        implements FichierService<F, T> {

    protected abstract void evictParentCache(Long parentId);
    protected final JpaRepository<F, Long> fichierRepository;
    protected final JpaRepository<T, Long> parentRepository;
    @Autowired
    private  RestTemplate restTemplate;

    protected AbstractFichierService(
            JpaRepository<F, Long> fichierRepository,
            JpaRepository<T, Long> parentRepository) {

        this.fichierRepository = fichierRepository;
        this.parentRepository = parentRepository;

    }

    protected abstract F createEntity();

    protected abstract void setParent(F fichier, T parent);

    protected abstract List<F> findByParentEntity(T parent);

    protected abstract void processFileStorage(F fichier, MultipartFile file,String nomFichier) ;
    protected abstract void processFileStorage(F fichier, byte[] fileBytes);
    //protected abstract  Map<String, Integer> saveDocumentOnLyOffice(Long id, Map<String, Object> payload);

    @Override
    public F upload(Long parentId, MultipartFile file, String fichierNom) {

        T parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        try {
            F fichier = createEntity();
            setParent(fichier, parent);

            processFileStorage(fichier, file,fichierNom);

            F saved = fichierRepository.save(fichier);
            evictParentCache(parentId);
            return saved;

        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur upload fichier: "+e.getMessage());
        }
    }

    @Override
    public void delete(Long fichierId) {
        fichierRepository.deleteById(fichierId);

    }

    @Override
    public List<F> findByParent(Long parentId) {

        T parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        return findByParentEntity(parent);
    }



    @Override
    public Map<String, Integer> saveDocumentOnLyOffice(Long id, Map<String, Object> payload) {
        try {

            Integer status = (Integer) payload.get("status");

            // Traitement selon le statut
            return switch (status) {
                case 1 -> {
                    log.debug("Document {} en cours d'édition", id);
                    yield Map.of("error", 0);
                }
                case 2 -> {
                    log.info("Document {} prêt pour sauvegarde", id);
                    // C'est ici qu'on doit télécharger et sauvegarder le document
                    yield saveDocumentFromUrl(id, payload);
                    // C'est ici qu'on doit télécharger et sauvegarder le document
                }
                case 3 -> {
                    log.error("Erreur de sauvegarde pour le document {} - Payload: {}", id, payload);
                    yield Map.of("error", 1);
                }
                case 4 -> {
                    log.info("Document {} fermé sans modifications", id);
                    yield Map.of("error", 0);
                }
                case 6 -> {
                    log.debug("État actuel du document {} sauvegardé pendant l'édition", id);
                    // Optionnel: on pourrait sauvegarder ici pour une sauvegarde automatique
                    // Pour l'instant, on retourne juste success
                    yield Map.of("error", 0);
                    // Optionnel: on pourrait sauvegarder ici pour une sauvegarde automatique
                    // Pour l'instant, on retourne juste success
                }
                case 7 -> {
                    log.error("Erreur lors de la sauvegarde forcée du document {}", id);
                    yield Map.of("error", 1);
                }
                default -> {
                    log.warn("Statut inconnu {} pour le document {}", status, id);
                    yield Map.of("error", 0);
                }
            };

        } catch (Exception e) {
            log.error("Erreur lors du traitement du callback OnlyOffice pour document {}: {}",
                    id, e.getMessage(), e);
            return Map.of("error", 1);
        }
    }

    private Map<String, Integer> saveDocumentFromUrl(Long id, Map<String, Object> payload) {
        F document = fichierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable: " + id));

        String updatedFileUrl = (String) payload.get("url");
        if (updatedFileUrl == null || updatedFileUrl.isEmpty()) {
            log.error("URL manquante pour le document {}", id);
            return Map.of("error", 1);
        }

        try {
            log.info("Téléchargement du document {} depuis: {}", id, updatedFileUrl);

            // Télécharger le fichier
            ResponseEntity<byte[]> response = restTemplate.getForEntity(updatedFileUrl, byte[].class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Échec du téléchargement pour le document {} - Status: {}",
                        id, response.getStatusCode());
                return Map.of("error", 1);
            }

            byte[] fileBytes = response.getBody();

            if (fileBytes.length == 0) {
                log.error("Fichier vide téléchargé pour le document {}", id);
                return Map.of("error", 1);
            }

            log.info("Fichier téléchargé pour document {} - Taille: {} octets", id, fileBytes.length);

            // Sauvegarder le fichier


            processFileStorage(document, fileBytes);

            // Mettre à jour les métadonnées
            document.setDateModification(new Date());

            fichierRepository.save(document);

            log.info("Document {} sauvegardé avec succès", id);
            return Map.of("error", 0);

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde du fichier pour le document {}: {}",
                    id, e.getMessage(), e);
            return Map.of("error", 1);
        }
    }



}
