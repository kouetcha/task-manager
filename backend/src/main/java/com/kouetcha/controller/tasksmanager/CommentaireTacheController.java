package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.ApiResponse;
import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireResponseDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.model.tasksmanager.CommentaireTache;
import com.kouetcha.service.tasksmanager.CommentaireTacheServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Commentaire Tache")
@RequiredArgsConstructor
@RequestMapping("commentaires-tache")
public class CommentaireTacheController {

    private final CommentaireTacheServiceImpl commentaireTacheService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentaireTache>create( @Valid @ModelAttribute CommentaireDto dto){
        CommentaireTache commentaireTache=commentaireTacheService.create(dto);
        return ResponseEntity.ok(commentaireTache);
    }
    
    @PatchMapping
    public ResponseEntity<CommentaireTache> changeContenu(@RequestBody CommentaireUpdateDto dto){
        CommentaireTache commentaireTache=commentaireTacheService.changeContenu(dto);
        return ResponseEntity.ok(commentaireTache);
    }
    
    @DeleteMapping("/{comId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long comId){
        commentaireTacheService.delete(comId);
        return ResponseEntity.ok(new ApiResponse("Commentaire supprimé avec succès"));
    }
    // ✅ Retourne des DTOs depuis le cache
    @GetMapping("{parentId}")
    public ResponseEntity<List<CommentaireResponseDto>> findByProjet(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentaireTacheService.findByParentDto(parentId));
    }
}
