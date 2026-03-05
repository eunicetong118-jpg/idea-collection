package com.ideacollection.controller;

import com.ideacollection.model.SubTopic;
import com.ideacollection.service.SubTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subtopics")
public class SubTopicController {
    private final SubTopicService subTopicService;

    public SubTopicController(SubTopicService subTopicService) {
        this.subTopicService = subTopicService;
    }

    @GetMapping
    public ResponseEntity<List<SubTopic>> getAllSubTopics() {
        return ResponseEntity.ok(subTopicService.getAllSubTopics());
    }

    @PostMapping
    public ResponseEntity<?> createSubTopic(@RequestBody Map<String, String> request) {
        try {
            SubTopic subTopic = subTopicService.createSubTopic(request.get("name"));
            return ResponseEntity.ok(subTopic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubTopic(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            SubTopic subTopic = subTopicService.updateSubTopic(id, request.get("name"));
            return ResponseEntity.ok(subTopic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubTopic(@PathVariable String id) {
        try {
            subTopicService.deleteSubTopic(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
