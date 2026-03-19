package com.kouetcha.controller.tasksmanager;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "DashBoard")
@RequiredArgsConstructor
@RequestMapping("activites")
public class DashBoardController {
}
