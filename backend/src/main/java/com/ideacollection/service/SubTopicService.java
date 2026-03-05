package com.ideacollection.service;

import com.ideacollection.model.SubTopic;
import com.ideacollection.repository.SubTopicRepository;
import com.ideacollection.repository.IdeaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubTopicService {
    private final SubTopicRepository subTopicRepository;
    private final IdeaRepository ideaRepository;

    public SubTopicService(SubTopicRepository subTopicRepository, IdeaRepository ideaRepository) {
        this.subTopicRepository = subTopicRepository;
        this.ideaRepository = ideaRepository;
    }

    public List<SubTopic> getAllSubTopics() {
        return subTopicRepository.findAllByOrderByCreatedAtAsc();
    }

    public SubTopic createSubTopic(String name) {
        SubTopic subTopic = new SubTopic();
        subTopic.setName(name);
        subTopic.setCardCount(0);
        return subTopicRepository.save(subTopic);
    }

    public SubTopic updateSubTopic(String id, String name) {
        SubTopic subTopic = subTopicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SubTopic not found"));
        subTopic.setName(name);
        return subTopicRepository.save(subTopic);
    }

    public void deleteSubTopic(String id) {
        ideaRepository.deleteBySubTopicId(id);
        subTopicRepository.deleteById(id);
    }

    public void incrementCardCount(String subTopicId) {
        SubTopic subTopic = subTopicRepository.findById(subTopicId)
            .orElseThrow(() -> new RuntimeException("SubTopic not found"));
        subTopic.setCardCount(subTopic.getCardCount() + 1);
        subTopicRepository.save(subTopic);
    }

    public void decrementCardCount(String subTopicId) {
        SubTopic subTopic = subTopicRepository.findById(subTopicId)
            .orElseThrow(() -> new RuntimeException("SubTopic not found"));
        subTopic.setCardCount(Math.max(0, subTopic.getCardCount() - 1));
        subTopicRepository.save(subTopic);
    }
}
