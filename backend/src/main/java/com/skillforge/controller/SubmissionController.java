package com.skillforge.controller;

import com.skillforge.dto.SubmissionRequest;
import com.skillforge.dto.SubmissionResponse;
import com.skillforge.service.SubmissionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(
            SubmissionService submissionService) {

        this.submissionService = submissionService;
    }

    // =========================
    // CREATE SUBMISSION
    // LEARNER ONLY
    // =========================

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_LEARNER')")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @Valid @RequestBody SubmissionRequest request,
            org.springframework.security.core.Authentication authentication) {

        SubmissionResponse response =
                submissionService.createSubmission(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================
    // GET MY SUBMISSIONS
    // LEARNER ONLY
    // =========================

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_LEARNER')")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                submissionService.getMySubmissions(
                        authentication.getName()
                )
        );
    }

    // =========================
    // GET SUBMISSION BY ID
    // AUTHENTICATED USERS
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                submissionService.getSubmissionById(id)
        );
    }

    // =========================
    // GET SUBMISSIONS FOR CHALLENGE
    // ADMIN / MENTOR
    // =========================

    @GetMapping("/challenge/{challengeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MENTOR')")
    public ResponseEntity<List<SubmissionResponse>>
    getSubmissionsByChallenge(
            @PathVariable Long challengeId) {

        return ResponseEntity.ok(
                submissionService.getSubmissionsByChallenge(
                        challengeId
                )
        );
    }
}