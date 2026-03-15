package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.ApiResponse;
import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireResponseDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.model.tasksmanager.CommentaireProjet;
import com.kouetcha.service.tasksmanager.CommentaireProjetServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Commentaire Projet")
@RequiredArgsConstructor
@RequestMapping("commentaires-projet")
public class CommentaireProjetController {

    private final CommentaireProjetServiceImpl commentaireProjetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentaireProjet>create( @Valid @ModelAttribute CommentaireDto dto){
        CommentaireProjet commentaireProjet=commentaireProjetService.create(dto);
        return ResponseEntity.ok(commentaireProjet);
    }

    @PatchMapping
    public ResponseEntity<CommentaireProjet> changeContenu(@RequestBody CommentaireUpdateDto dto){
        CommentaireProjet commentaireProjet=commentaireProjetService.changeContenu(dto);
        return ResponseEntity.ok(commentaireProjet);
    }

    @DeleteMapping("/{comId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long comId){
        commentaireProjetService.delete(comId);
        return ResponseEntity.ok(new ApiResponse("Commentaire supprimé avec succès"));
    }

    // ✅ Retourne des DTOs depuis le cache
    @GetMapping("{parentId}")
    public ResponseEntity<List<CommentaireResponseDto>> findByProjet(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentaireProjetService.findByParentDto(parentId));
    }
}
