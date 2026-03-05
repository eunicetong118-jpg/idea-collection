package com.ideacollection.service;

import com.ideacollection.model.Idea;
import com.ideacollection.model.Comment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastNewIdea(Idea idea) {
        messagingTemplate.convertAndSend("/topic/ideas/" + idea.getSubTopicId(), idea);
    }

    public void broadcastIdeaUpdate(Idea idea) {
        messagingTemplate.convertAndSend("/topic/ideas/" + idea.getSubTopicId(), idea);
    }

    public void broadcastLikeUpdate(String ideaId, String subTopicId, int likeCount) {
        messagingTemplate.convertAndSend("/topic/ideas/" + ideaId + "/likes",
            java.util.Map.of("likeCount", likeCount));
    }

    public void broadcastNewComment(Comment comment, String subTopicId) {
        messagingTemplate.convertAndSend("/topic/ideas/" + comment.getIdeaId() + "/comments", comment);
    }
}
