package com.ideacollection.service;

import com.ideacollection.model.Idea;
import com.ideacollection.repository.IdeaRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class IdeaService {
    private final IdeaRepository ideaRepository;
    private final SubTopicService subTopicService;

    public IdeaService(IdeaRepository ideaRepository, SubTopicService subTopicService) {
        this.ideaRepository = ideaRepository;
        this.subTopicService = subTopicService;
    }

    public List<Idea> getIdeasBySubTopic(String subTopicId, String sortBy) {
        List<Idea> ideas = switch (sortBy) {
            case "recent" -> ideaRepository.findBySubTopicIdOrderByCreatedAtDesc(subTopicId);
            case "most_liked" -> ideaRepository.findBySubTopicIdOrderByLikeCountDesc(subTopicId);
            case "most_discussed" -> ideaRepository.findBySubTopicIdOrderByLastCommentAtDesc(subTopicId);
            default -> ideaRepository.findBySubTopicIdOrderByCreatedAtDesc(subTopicId);
        };

        // Sort: completed (Done) at bottom
        ideas.sort(Comparator.comparing((Idea i) ->
            "Done".equals(i.getStageStatus()))
            .thenComparing((Idea i) -> calculateScore(i)).reversed());

        return ideas;
    }

    private double calculateScore(Idea idea) {
        long hoursSinceCreation = java.time.Duration.between(idea.getCreatedAt(), Instant.now()).toHours();
        int recentComments = idea.getLastCommentAt() != null ?
            (int) java.time.Duration.between(idea.getLastCommentAt(), Instant.now()).toHours() : 100;

        return idea.getLikeCount() * 2 + (100 - Math.min(hoursSinceCreation, 100)) + (100 - Math.min(recentComments, 100));
    }

    public Idea createIdea(String subTopicId, String title, String description, String author) {
        Idea idea = new Idea();
        idea.setSubTopicId(subTopicId);
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setAuthor(author);

        Idea saved = ideaRepository.save(idea);
        subTopicService.incrementCardCount(subTopicId);

        return saved;
    }

    public Idea updateStatus(String id, String stage, String stageStatus) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Idea not found"));

        if (stage != null) idea.setStage(stage);
        if (stageStatus != null) idea.setStageStatus(stageStatus);
        idea.setUpdatedAt(Instant.now());

        return ideaRepository.save(idea);
    }

    public Idea toggleLike(String id, String userId) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Idea not found"));

        if (idea.getLikes().contains(userId)) {
            idea.getLikes().remove(userId);
            idea.setLikeCount(Math.max(0, idea.getLikeCount() - 1));
        } else {
            idea.getLikes().add(userId);
            idea.setLikeCount(idea.getLikeCount() + 1);
        }

        return ideaRepository.save(idea);
    }

    public boolean hasUserLiked(Idea idea, String userId) {
        return idea.getLikes().contains(userId);
    }

    public Idea getIdeaById(String id) {
        return ideaRepository.findById(id).orElse(null);
    }
}
