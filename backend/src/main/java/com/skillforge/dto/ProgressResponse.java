package com.skillforge.dto;

import java.time.LocalDateTime;

public class ProgressResponse {

    private Long userId;
    private String fullName;
    private String email;

    private Integer totalSubmissions;
    private Integer passedChallenges;
    private Integer totalScore;

    private LocalDateTime updatedAt;

    public ProgressResponse() {
    }

    public ProgressResponse(
            Long userId,
            String fullName,
            String email,
            Integer totalSubmissions,
            Integer passedChallenges,
            Integer totalScore,
            LocalDateTime updatedAt) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.totalSubmissions = totalSubmissions;
        this.passedChallenges = passedChallenges;
        this.totalScore = totalScore;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }

    public Integer getPassedChallenges() {
        return passedChallenges;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}