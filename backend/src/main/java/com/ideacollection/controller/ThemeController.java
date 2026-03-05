package com.ideacollection.controller;

import com.ideacollection.model.Theme;
import com.ideacollection.service.ThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/theme")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<?> getTheme() {
        Theme theme = themeService.getTheme();
        return ResponseEntity.ok(Map.of("name", theme != null ? theme.getName() : ""));
    }

    @PutMapping
    public ResponseEntity<?> updateTheme(@RequestBody Map<String, String> request) {
        try {
            Theme theme = themeService.updateTheme(request.get("name"));
            return ResponseEntity.ok(Map.of("name", theme.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
