
package com.skillforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.dto.SubmissionRequest;
import com.skillforge.dto.SubmissionResponse;
import com.skillforge.entity.Challenge;
import com.skillforge.entity.Progress;
import com.skillforge.entity.Submission;
import com.skillforge.entity.User;
import com.skillforge.repository.ChallengeRepository;
import com.skillforge.repository.ProgressRepository;
import com.skillforge.repository.SubmissionRepository;
import com.skillforge.repository.UserRepository;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ChallengeRepository challengeRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ChallengeRepository challengeRepository,
            ProgressRepository progressRepository,
            UserRepository userRepository) {

        this.submissionRepository = submissionRepository;
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SubmissionResponse createSubmission(
            SubmissionRequest request,
            String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Long userId = user.getId();
        Long challengeId = request.getChallengeId();

        Challenge challenge = challengeRepository.findById(challengeId)
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() ->
                        new RuntimeException(
                                "Challenge not found or inactive"));

        String answer = request.getAnswer().trim();

        String expected = challenge.getExpectedAnswer() == null
                ? ""
                : challenge.getExpectedAnswer().trim();

        boolean passed = answer.equals(expected);
        int score = passed ? challenge.getPoints() : 0;

        // Check previous successful attempts before saving this one.
        boolean alreadyPassed = passed
                && submissionRepository
                        .existsByUser_IdAndChallenge_IdAndStatus(
                                userId, challengeId, "PASSED");

        Submission submission = new Submission();
        submission.setAnswer(answer);
        submission.setScore(score);
        submission.setStatus(passed ? "PASSED" : "FAILED");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setUser(user);
        submission.setChallenge(challenge);

        Submission saved = submissionRepository.save(submission);

        Progress progress = progressRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Progress p = new Progress();
                    p.setUser(user);
                    p.setTotalSubmissions(0);
                    p.setPassedChallenges(0);
                    p.setTotalScore(0);
                    p.setUpdatedAt(LocalDateTime.now());
                    return p;
                });

        // Count every attempt.
        progress.setTotalSubmissions(
                progress.getTotalSubmissions() + 1);

        // Preserve the existing scoring rule.
        progress.setTotalScore(
                progress.getTotalScore() + score);

        // Count each challenge only once per user.
        if (passed && !alreadyPassed) {
            progress.setPassedChallenges(
                    progress.getPassedChallenges() + 1);
        }

        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return submissionRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long id) {

        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Submission not found"));

        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByChallenge(
            Long challengeId) {

        return submissionRepository.findByChallengeId(challengeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SubmissionResponse toResponse(Submission submission) {

        User user = submission.getUser();
        Challenge challenge = submission.getChallenge();

        return new SubmissionResponse(
                submission.getId(),
                submission.getAnswer(),
                submission.getScore(),
                submission.getStatus(),
                submission.getSubmittedAt(),
                user.getId(),
                challenge.getId(),
                challenge.getTitle());
    }
}
