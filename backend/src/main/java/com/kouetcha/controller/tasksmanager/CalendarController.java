package com.kouetcha.controller.tasksmanager;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.tasksmanager.CalendarEventDto;
import com.kouetcha.service.tasksmanager.CalendarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;


@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Calendar")
@RequiredArgsConstructor
@RequestMapping("calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<List<CalendarEventDto>> getEvents(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate end,
            @AuthenticationPrincipal UserDetails userDetails) throws IllegalAccessException {
        if(UserContext.getUtilisaeurConnecte()==null){
            throw new IllegalAccessException("Vous n'avez pas le droit d'acceder à la ressource");
        }
        Long userId = UserContext.getUtilisaeurConnecte().getId();
        String userEmail=UserContext.getUtilisaeurConnecte().getEmail();
        return ResponseEntity.ok(calendarService.getCalendarEvents(userId, userEmail,start, end));
    }
}
