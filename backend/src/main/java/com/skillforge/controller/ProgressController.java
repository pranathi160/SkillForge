package com.skillforge.controller;

import com.skillforge.dto.LeaderboardResponse;
import com.skillforge.dto.ProgressResponse;
import com.skillforge.service.ProgressService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(
            ProgressService progressService) {

        this.progressService = progressService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_LEARNER')")
    public ResponseEntity<ProgressResponse> getMyProgress(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                progressService.getMyProgress(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {

        return ResponseEntity.ok(
                progressService.getLeaderboard()
        );
    }
}