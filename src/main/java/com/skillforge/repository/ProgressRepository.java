package com.skillforge.repository;

import com.skillforge.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository
        extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserId(Long userId);

    List<Progress> findAllByOrderByTotalScoreDesc();
}