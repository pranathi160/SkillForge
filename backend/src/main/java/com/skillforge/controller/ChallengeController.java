
package com.skillforge.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.dto.ChallengeResponse;
import com.skillforge.entity.Challenge;
import com.skillforge.repository.ChallengeRepository;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;

    public ChallengeController(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    private ChallengeResponse toResponse(Challenge c) {
        return new ChallengeResponse(
            c.getId(),
            c.getTitle(),
            c.getDescription(),
            c.getCategory(),
            c.getDifficulty() == null
                ? null : c.getDifficulty().toString(),
            c.getPoints(),
            c.getActive(),
            c.getCreatedAt()
        );
    }

    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        List<ChallengeResponse> result =
            challengeRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChallengeById(@PathVariable Long id) {
        return challengeRepository.findById(id)
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .<ResponseEntity<?>>map(c ->
                ResponseEntity.ok(toResponse(c)))
            .orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Active challenge not found")));
    }

    @PostMapping
    public ResponseEntity<?> createChallenge(
            @RequestBody Challenge request,
            Authentication authentication) {

        if (authentication == null ||
            authentication.getAuthorities().stream()
                .noneMatch(a ->
                    "ROLE_ADMIN".equals(a.getAuthority()))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                    "message", "Only admins can create challenges"
                ));
        }

        if (request.getTitle() == null ||
            request.getTitle().isBlank() ||
            request.getDescription() == null ||
            request.getDescription().isBlank() ||
            request.getCategory() == null ||
            request.getCategory().isBlank() ||
            request.getDifficulty() == null ||
            request.getExpectedAnswer() == null ||
            request.getExpectedAnswer().isBlank() ||
            request.getPoints() == null ||
            request.getPoints() <= 0) {

            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message",
                    "Provide all challenge fields and positive points"
                ));
        }

        Challenge challenge = new Challenge();
        challenge.setTitle(request.getTitle().trim());
        challenge.setDescription(request.getDescription().trim());
        challenge.setCategory(request.getCategory().trim());
        challenge.setDifficulty(request.getDifficulty());
        challenge.setPoints(request.getPoints());
        challenge.setExpectedAnswer(request.getExpectedAnswer());
        challenge.setActive(true);

        Challenge saved = challengeRepository.save(challenge);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toResponse(saved));
    }
}
