package com.ideacollection.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ideas")
public class Idea {
    @Id
    private String id;

    @Indexed
    private String subTopicId;

    private String title;
    private String description;
    private String author;
    private String stage;
    private String stageStatus;
    private List<String> likes;
    private int likeCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastCommentAt;

    public Idea() {
        this.likes = new ArrayList<>();
        this.likeCount = 0;
        this.stage = "Review";
        this.stageStatus = "New";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubTopicId() { return subTopicId; }
    public void setSubTopicId(String subTopicId) { this.subTopicId = subTopicId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStageStatus() { return stageStatus; }
    public void setStageStatus(String stageStatus) { this.stageStatus = stageStatus; }
    public List<String> getLikes() { return likes; }
    public void setLikes(List<String> likes) { this.likes = likes; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastCommentAt() { return lastCommentAt; }
    public void setLastCommentAt(Instant lastCommentAt) { this.lastCommentAt = lastCommentAt; }
}
