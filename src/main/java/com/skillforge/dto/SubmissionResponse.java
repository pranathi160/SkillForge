package com.skillforge.dto;

import java.time.LocalDateTime;

public class SubmissionResponse {

    private Long id;
    private String answer;
    private Integer score;
    private String status;
    private LocalDateTime submittedAt;

    private Long userId;
    private Long challengeId;
    private String challengeTitle;

    public SubmissionResponse() {
    }

    public SubmissionResponse(
            Long id,
            String answer,
            Integer score,
            String status,
            LocalDateTime submittedAt,
            Long userId,
            Long challengeId,
            String challengeTitle) {

        this.id = id;
        this.answer = answer;
        this.score = score;
        this.status = status;
        this.submittedAt = submittedAt;
        this.userId = userId;
        this.challengeId = challengeId;
        this.challengeTitle = challengeTitle;
    }

    public Long getId() {
        return id;
    }

    public String getAnswer() {
        return answer;
    }

    public Integer getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public String getChallengeTitle() {
        return challengeTitle;
    }
}