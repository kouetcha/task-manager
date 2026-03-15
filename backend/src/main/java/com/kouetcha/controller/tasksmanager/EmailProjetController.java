package com.kouetcha.controller.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.service.tasksmanager.EmailProjetServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Emails Projet")
@RequestMapping("/projets/{projetId}/emails")
@RequiredArgsConstructor
public class EmailProjetController {

    private final EmailProjetServiceImpl emailProjetService;

    @PostMapping
    public ResponseEntity<EmailProjet> addEmail(
            @PathVariable Long projetId,
            @RequestParam String email) {

        EmailProjet created = emailProjetService.addEmail(projetId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PatchMapping("/{emailId}")
    public ResponseEntity<EmailProjet> updateEmail(
            @PathVariable Long emailId,
            @RequestParam String email) {

        EmailProjet updated = emailProjetService.updateEmail(emailId, email);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> removeEmail(
            @PathVariable Long emailId) {

        emailProjetService.removeEmail(emailId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{emailId}/activate")
    public ResponseEntity<EmailProjet> activateEmail(
            @PathVariable Long emailId) {

        EmailProjet updated = emailProjetService.activateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{emailId}/deactivate")
    public ResponseEntity<EmailProjet> deactivateEmail(
            @PathVariable Long emailId) {

        EmailProjet updated = emailProjetService.deactivateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<EmailProjet>> findByProjet(
            @PathVariable Long projetId) {

        List<EmailProjet> emails = emailProjetService.findByParent(projetId);
        return ResponseEntity.ok(emails);
    }
}