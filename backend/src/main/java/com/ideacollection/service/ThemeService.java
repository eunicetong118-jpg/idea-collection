package com.ideacollection.service;

import com.ideacollection.model.Theme;
import com.ideacollection.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme getTheme() {
        return themeRepository.findAll().stream().findFirst().orElse(null);
    }

    public Theme updateTheme(String name) {
        Theme theme = getTheme();
        if (theme == null) {
            theme = new Theme();
        }
        theme.setName(name);
        theme.setUpdatedAt(Instant.now());
        return themeRepository.save(theme);
    }
}
