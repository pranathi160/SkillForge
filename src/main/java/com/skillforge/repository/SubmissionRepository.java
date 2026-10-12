
package com.skillforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillforge.entity.Submission;

@Repository
public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    List<Submission> findByUserId(Long userId);

    List<Submission> findByChallengeId(Long challengeId);

    boolean existsByUser_IdAndChallenge_IdAndStatus(
            Long userId,
            Long challengeId,
            String status);
}
