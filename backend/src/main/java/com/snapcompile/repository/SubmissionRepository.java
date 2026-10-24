package com.snapcompile.repository;

import com.snapcompile.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * SubmissionRepository - DB queries for user submissions (history)
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Get all submissions for a user, newest first (for History page)
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    // Get submissions filtered by language
    List<Submission> findByUserIdAndLanguageOrderBySubmittedAtDesc(Long userId, String language);

    // Count total submissions by user
    long countByUserId(Long userId);

    // Count accepted submissions only
    long countByUserIdAndStatus(Long userId, String status);
}
