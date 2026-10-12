package com.skillforge.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5000)
    private String answer;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // =========================
    // USER RELATIONSHIP
    // =========================

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // =========================
    // CHALLENGE RELATIONSHIP
    // =========================

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    // =========================
    // CONSTRUCTORS
    // =========================

    public Submission() {
    }

    public Submission(
            Long id,
            String answer,
            Integer score,
            String status,
            LocalDateTime submittedAt,
            User user,
            Challenge challenge) {

        this.id = id;
        this.answer = answer;
        this.score = score;
        this.status = status;
        this.submittedAt = submittedAt;
        this.user = user;
        this.challenge = challenge;
    }

    // =========================
    // PRE PERSIST
    // =========================

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();

        if (score == null) {
            score = 0;
        }

        if (status == null) {
            status = "SUBMITTED";
        }
    }

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }
}