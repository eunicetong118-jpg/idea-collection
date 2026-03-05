package com.ideacollection.service;

import com.ideacollection.model.Comment;
import com.ideacollection.model.Idea;
import com.ideacollection.repository.CommentRepository;
import com.ideacollection.repository.IdeaRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final IdeaRepository ideaRepository;

    public CommentService(CommentRepository commentRepository, IdeaRepository ideaRepository) {
        this.commentRepository = commentRepository;
        this.ideaRepository = ideaRepository;
    }

    public List<Comment> getCommentsByIdea(String ideaId) {
        return commentRepository.findByIdeaIdOrderByCreatedAtAsc(ideaId);
    }

    public Comment addComment(String ideaId, String content, String author) {
        Comment comment = new Comment();
        comment.setIdeaId(ideaId);
        comment.setContent(content);
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);

        // Update idea's lastCommentAt
        Idea idea = ideaRepository.findById(ideaId).orElse(null);
        if (idea != null) {
            idea.setLastCommentAt(Instant.now());
            ideaRepository.save(idea);
        }

        return saved;
    }
}
