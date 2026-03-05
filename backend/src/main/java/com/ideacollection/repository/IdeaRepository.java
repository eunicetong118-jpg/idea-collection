package com.ideacollection.repository;

import com.ideacollection.model.Idea;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface IdeaRepository extends MongoRepository<Idea, String> {
    List<Idea> findBySubTopicIdOrderByCreatedAtDesc(String subTopicId);
    List<Idea> findBySubTopicIdOrderByLikeCountDesc(String subTopicId);
    List<Idea> findBySubTopicIdOrderByLastCommentAtDesc(String subTopicId);
    void deleteBySubTopicId(String subTopicId);
}