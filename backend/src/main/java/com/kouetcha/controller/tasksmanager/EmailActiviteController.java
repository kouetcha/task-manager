package com.kouetcha.controller.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailActivite;
import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.service.tasksmanager.EmailActiviteServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Emails Activite")
@RequestMapping("/activites/{activiteId}/emails")
@RequiredArgsConstructor
public class EmailActiviteController {

    private final EmailActiviteServiceImpl emailActiviteService;

    @PostMapping
    public ResponseEntity<EmailActivite> addEmail(
            @PathVariable Long activiteId,
            @RequestParam String email) {

        EmailActivite created = emailActiviteService.addEmail(activiteId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> removeEmail(
            @PathVariable Long emailId) {

        emailActiviteService.removeEmail(emailId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{emailId}/activate")
    public ResponseEntity<EmailActivite> activateEmail(
            @PathVariable Long emailId) {

        EmailActivite updated = emailActiviteService.activateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{emailId}/deactivate")
    public ResponseEntity<EmailActivite> deactivateEmail(
            @PathVariable Long emailId) {

        EmailActivite updated = emailActiviteService.deactivateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<EmailActivite>> findByActivite(
            @PathVariable Long activiteId) {

        List<EmailActivite> emails = emailActiviteService.findByParent(activiteId);
        return ResponseEntity.ok(emails);
    }

    @PatchMapping("/{emailId}")
    public ResponseEntity<EmailActivite> updateEmail(
            @PathVariable Long emailId,
            @RequestParam String email) {

        EmailActivite updated = emailActiviteService.updateEmail(emailId, email);
        return ResponseEntity.ok(updated);
    }
}