package com.skillforge.service;

import com.skillforge.dto.ChallengeRequest;
import com.skillforge.dto.ChallengeResponse;
import com.skillforge.entity.Challenge;
import com.skillforge.repository.ChallengeRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    // =========================
    // CREATE CHALLENGE
    // =========================

    public ChallengeResponse createChallenge(
            ChallengeRequest request) {

        Challenge challenge = new Challenge(
                null,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getDifficulty(),
                request.getPoints(),
                request.getExpectedAnswer(),
                true,
                null
        );

        Challenge savedChallenge =
                challengeRepository.save(challenge);

        return convertToResponse(savedChallenge);
    }

    // =========================
    // GET ALL CHALLENGES
    // =========================

    public List<ChallengeResponse> getAllChallenges() {

        return challengeRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================
    // GET CHALLENGE BY ID
    // =========================

    public ChallengeResponse getChallengeById(Long id) {

        Challenge challenge =
                challengeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Challenge not found"));

        return convertToResponse(challenge);
    }

    // =========================
    // UPDATE CHALLENGE
    // =========================

    public ChallengeResponse updateChallenge(
            Long id,
            ChallengeRequest request) {

        Challenge challenge =
                challengeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Challenge not found"));

        challenge.setTitle(request.getTitle());
        challenge.setDescription(request.getDescription());
        challenge.setCategory(request.getCategory());
        challenge.setDifficulty(request.getDifficulty());
        challenge.setPoints(request.getPoints());
        challenge.setExpectedAnswer(
                request.getExpectedAnswer()
        );

        Challenge updatedChallenge =
                challengeRepository.save(challenge);

        return convertToResponse(updatedChallenge);
    }

    // =========================
    // DELETE CHALLENGE
    // =========================

    public void deleteChallenge(Long id) {

        if (!challengeRepository.existsById(id)) {
            throw new RuntimeException(
                    "Challenge not found");
        }

        challengeRepository.deleteById(id);
    }

    // =========================
    // CONVERT ENTITY TO RESPONSE
    // =========================

    private ChallengeResponse convertToResponse(
            Challenge challenge) {

        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getCategory(),
                challenge.getDifficulty(),
                challenge.getPoints(),
                challenge.getActive(),
                challenge.getCreatedAt()
        );
    }
}