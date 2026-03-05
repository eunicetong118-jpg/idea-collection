package com.ideacollection.controller;

import com.ideacollection.model.Idea;
import com.ideacollection.service.IdeaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ideas")
public class IdeaController {
    private final IdeaService ideaService;

    public IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @GetMapping
    public ResponseEntity<List<Idea>> getIdeas(
            @RequestParam String subTopicId,
            @RequestParam(defaultValue = "default") String sortBy) {
        return ResponseEntity.ok(ideaService.getIdeasBySubTopic(subTopicId, sortBy));
    }

    @PostMapping
    public ResponseEntity<?> createIdea(@Valid @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username) {
        try {
            Idea idea = ideaService.createIdea(
                request.get("subTopicId"),
                request.get("title"),
                request.get("description"),
                username
            );
            return ResponseEntity.ok(idea);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            Idea idea = ideaService.updateStatus(id, request.get("stage"), request.get("stageStatus"));
            return ResponseEntity.ok(idea);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        try {
            Idea idea = ideaService.toggleLike(id, userId);
            return ResponseEntity.ok(Map.of(
                "likeCount", idea.getLikeCount(),
                "hasLiked", idea.getLikes().contains(userId)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
