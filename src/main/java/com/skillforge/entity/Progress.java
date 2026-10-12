package com.skillforge.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer totalSubmissions = 0;

    @Column(nullable = false)
    private Integer passedChallenges = 0;

    @Column(nullable = false)
    private Integer totalScore = 0;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Progress() {
    }

    public Progress(
            Long id,
            Integer totalSubmissions,
            Integer passedChallenges,
            Integer totalScore,
            LocalDateTime updatedAt,
            User user) {

        this.id = id;
        this.totalSubmissions = totalSubmissions;
        this.passedChallenges = passedChallenges;
        this.totalScore = totalScore;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Integer totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Integer getPassedChallenges() {
        return passedChallenges;
    }

    public void setPassedChallenges(Integer passedChallenges) {
        this.passedChallenges = passedChallenges;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}