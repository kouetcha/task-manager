package com.kouetcha.controller.tasksmanager;

import com.fasterxml.jackson.annotation.JsonView;
import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.service.tasksmanager.CommentaireActiviteServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Commentaire Activite")
@RequiredArgsConstructor
@RequestMapping("commentaires-activite")
public class CommentaireActiviteController {

    private final CommentaireActiviteServiceImpl commentaireActiviteService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentaireActivite>create( @Valid @ModelAttribute CommentaireDto dto){
        CommentaireActivite commentaireActivite=commentaireActiviteService.create(dto);
        return ResponseEntity.ok(commentaireActivite);
    }
    
    @PatchMapping
    public ResponseEntity<CommentaireActivite> changeContenu(@RequestBody CommentaireUpdateDto dto){
        CommentaireActivite commentaireActivite=commentaireActiviteService.changeContenu(dto);
        return ResponseEntity.ok(commentaireActivite);
    }
    
    @DeleteMapping("/{comId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long comId){
        commentaireActiviteService.delete(comId);
        return ResponseEntity.ok(new ApiResponse("Commentaire supprimé avec succès"));
    }
    // ✅ Retourne des DTOs depuis le cache
    @GetMapping("{parentId}")
    public ResponseEntity<List<CommentaireResponseDto>> findByProjet(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentaireActiviteService.findByParentDto(parentId));
    }
}
