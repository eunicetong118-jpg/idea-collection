package com.ideacollection.repository;

import com.ideacollection.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByIdeaIdOrderByCreatedAtAsc(String ideaId);
}