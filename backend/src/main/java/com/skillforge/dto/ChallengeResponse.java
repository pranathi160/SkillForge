
package com.skillforge.dto;

import java.time.LocalDateTime;

public class ChallengeResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer points;
    private Boolean active;
    private LocalDateTime createdAt;

    public ChallengeResponse() {
    }

    public ChallengeResponse(
            Long id,
            String title,
            String description,
            String category,
            String difficulty,
            Integer points,
            Boolean active,
            LocalDateTime createdAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getPoints() {
        return points;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
