package com.skillforge.dto;

public class LeaderboardResponse {

    private Integer rank;
    private Long userId;
    private String fullName;
    private Integer totalScore;
    private Integer passedChallenges;
    private Integer totalSubmissions;

    public LeaderboardResponse() {
    }

    public LeaderboardResponse(
            Integer rank,
            Long userId,
            String fullName,
            Integer totalScore,
            Integer passedChallenges,
            Integer totalSubmissions) {

        this.rank = rank;
        this.userId = userId;
        this.fullName = fullName;
        this.totalScore = totalScore;
        this.passedChallenges = passedChallenges;
        this.totalSubmissions = totalSubmissions;
    }

    public Integer getRank() {
        return rank;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public Integer getPassedChallenges() {
        return passedChallenges;
    }

    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }
}