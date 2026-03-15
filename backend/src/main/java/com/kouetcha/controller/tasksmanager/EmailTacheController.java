package com.kouetcha.controller.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.service.tasksmanager.EmailTacheServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Emails Tache")
@RequestMapping("/taches/{tacheId}/emails")
@RequiredArgsConstructor
public class EmailTacheController {

    private final EmailTacheServiceImpl emailTacheService;

    @PostMapping
    public ResponseEntity<EmailTache> addEmail(
            @PathVariable Long tacheId,
            @RequestParam String email) {

        EmailTache created = emailTacheService.addEmail(tacheId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> removeEmail(
            @PathVariable Long emailId) {

        emailTacheService.removeEmail(emailId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{emailId}/activate")
    public ResponseEntity<EmailTache> activateEmail(
            @PathVariable Long emailId) {

        EmailTache updated = emailTacheService.activateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{emailId}/deactivate")
    public ResponseEntity<EmailTache> deactivateEmail(
            @PathVariable Long emailId) {

        EmailTache updated = emailTacheService.deactivateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<EmailTache>> findByTache(
            @PathVariable Long tacheId) {

        List<EmailTache> emails = emailTacheService.findByParent(tacheId);
        return ResponseEntity.ok(emails);
    }
    @PatchMapping("/{emailId}")
    public ResponseEntity<EmailTache> updateEmail(
            @PathVariable Long emailId,
            @RequestParam String email) {

        EmailTache updated = emailTacheService.updateEmail(emailId, email);
        return ResponseEntity.ok(updated);
    }
}