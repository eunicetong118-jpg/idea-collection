package com.ideacollection.repository;

import com.ideacollection.model.SubTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SubTopicRepository extends MongoRepository<SubTopic, String> {
    List<SubTopic> findAllByOrderByCreatedAtAsc();
}