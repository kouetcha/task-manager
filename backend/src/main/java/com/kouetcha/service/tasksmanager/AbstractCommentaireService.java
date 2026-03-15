package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.dto.tasksmanager.FichierDTO;
import com.kouetcha.model.base.BaseCommentaire;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.model.tasksmanager.CommentaireProjet;
import com.kouetcha.model.tasksmanager.CommentaireTache;
import com.kouetcha.model.tasksmanager.FichierCommentaire;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;

@Transactional
public abstract class AbstractCommentaireService<C extends BaseCommentaire<T>, T>
        implements CommentaireService<C, T> {

    protected final JpaRepository<C, Long> commentaireRepository;
    protected final JpaRepository<T, Long> parentRepository;
    protected final UtilisateurRepository utilisateurRepository;

    protected AbstractCommentaireService(
            JpaRepository<C, Long> commentaireRepository,
            JpaRepository<T, Long> parentRepository,
            UtilisateurRepository utilisateurRepository) {

        this.commentaireRepository = commentaireRepository;
        this.parentRepository = parentRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    protected abstract C createEntity();
    protected abstract void setParent(C commentaire, T parent);
    protected abstract List<C> findByParentEntity(T parent);

    @Override
    public C create(CommentaireDto dto) {

        T parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        Utilisateur auteur = utilisateurRepository.findById(dto.getAuteurId())
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        C commentaire = createEntity();
        commentaire.setContenu(dto.getContenu());
        commentaire.setDate(new Date());
        commentaire.setAuteur(auteur);

        setParent(commentaire, parent);

        // Gestion des fichiers
        if (dto.getFichiers() != null) {
            for (FichierDTO fileDto : dto.getFichiers()) {

                FichierCommentaire fichier = new FichierCommentaire();

                String fileName = fileDto.getNomFichier();
                String path = saveFile(fileDto.getFichier());

                fichier.setNomFichier(fileName);
                fichier.setCheminFichier(path);
                if (commentaire instanceof CommentaireProjet) {
                    fichier.setCommentaireProjet((CommentaireProjet) commentaire);
                } else if (commentaire instanceof CommentaireActivite) {
                    fichier.setCommentaireActivite((CommentaireActivite) commentaire);
                } else if (commentaire instanceof CommentaireTache) {
                    fichier.setCommentaireTache((CommentaireTache) commentaire);
                }


            }
        }

        return commentaireRepository.save(commentaire);
    }

    @Override
    public C update(CommentaireDto dto) {

        C commentaire = commentaireRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));

        commentaire.setContenu(dto.getContenu());

        return commentaireRepository.save(commentaire);
    }

    @Override
    public C changeContenu(CommentaireUpdateDto dto) {

        C commentaire = commentaireRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));

        commentaire.setContenu(dto.getContenu());

        return commentaireRepository.save(commentaire);
    }

    @Override
    public void delete(Long commentaireId) {
        commentaireRepository.deleteById(commentaireId);
    }

    @Override
    public List<C> findByParent(Long parentId) {

        T parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        return findByParentEntity(parent);
    }
    @Override
    public String saveFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/";
            String filePath = uploadDir + file.getOriginalFilename();

            File dest = new File(filePath);
            file.transferTo(dest);

            return filePath;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier");
        }
    }

}