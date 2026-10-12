package com.skillforge.service;

import com.skillforge.dto.LeaderboardResponse;
import com.skillforge.dto.ProgressResponse;
import com.skillforge.entity.Progress;
import com.skillforge.entity.User;
import com.skillforge.repository.ProgressRepository;
import com.skillforge.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public ProgressService(
            ProgressRepository progressRepository,
            UserRepository userRepository) {

        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    public ProgressResponse getMyProgress(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Progress progress = progressRepository
                .findByUserId(user.getId())
                .orElseGet(() -> createProgress(user));

        return convertToResponse(progress);
    }

    public List<LeaderboardResponse> getLeaderboard() {

        List<Progress> progressList =
                progressRepository
                        .findAllByOrderByTotalScoreDesc();

        List<LeaderboardResponse> leaderboard =
                new ArrayList<>();

        int rank = 1;

        for (Progress progress : progressList) {

            User user = progress.getUser();

            leaderboard.add(
                    new LeaderboardResponse(
                            rank,
                            user.getId(),
                            user.getFullName(),
                            progress.getTotalScore(),
                            progress.getPassedChallenges(),
                            progress.getTotalSubmissions()
                    )
            );

            rank++;
        }

        return leaderboard;
    }

    public Progress createProgress(User user) {

        Progress progress = new Progress(
                null,
                0,
                0,
                0,
                null,
                user
        );

        return progressRepository.save(progress);
    }

    private ProgressResponse convertToResponse(
            Progress progress) {

        User user = progress.getUser();

        return new ProgressResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                progress.getTotalSubmissions(),
                progress.getPassedChallenges(),
                progress.getTotalScore(),
                progress.getUpdatedAt()
        );
    }
}