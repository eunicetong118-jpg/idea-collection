package com.ideacollection.controller;

import com.ideacollection.model.Comment;
import com.ideacollection.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ideas")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String id) {
        return ResponseEntity.ok(commentService.getCommentsByIdea(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String id,
            @Valid @RequestBody Map<String, String> request,
            @RequestHeader("X-Username") String username) {
        try {
            Comment comment = commentService.addComment(id, request.get("content"), username);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
