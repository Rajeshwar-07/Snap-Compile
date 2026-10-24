package com.snapcompile.service;

import com.snapcompile.dto.RunCodeRequest;
import com.snapcompile.model.Submission;
import com.snapcompile.model.User;
import com.snapcompile.repository.SubmissionRepository;
import com.snapcompile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SubmissionService - Handles code execution and submission history
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final Judge0Service judge0Service;

    /**
     * Run code and save to history
     *
     * Steps:
     * 1. Send code to Judge0 for execution
     * 2. Get output, runtime, memory, status
     * 3. Save submission to DB (for history)
     * 4. Return submission with results
     */
    public Submission runAndSave(RunCodeRequest request, String userEmail) {

        // Get user from DB
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Execute code via Judge0
        ExecutionResult executionResult =
                judge0Service.executeCode(request.getCode(), request.getLanguage(), request.getStdin());

        // Build submission object
        Submission submission = Submission.builder()
                .user(user)
                .problemTitle(request.getProblemTitle() != null ? request.getProblemTitle() : "Untitled")
                .language(request.getLanguage())
                .code(request.getCode())
                .status(executionResult.status)
                .output(executionResult.output)
                .errorMessage(executionResult.errorMessage)
                .runtimeMs(executionResult.runtimeMs)
                .memoryUsed(executionResult.memoryUsed)
                .timeComplexity(request.getTimeComplexity())
                .spaceComplexity(request.getSpaceComplexity())
                .build();

        // Save and return
        return submissionRepository.save(submission);
    }

    /**
     * Get all submissions for a user (History page)
     */
    public List<Submission> getUserHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId());
    }

    /**
     * Get a single submission by ID (Submission Detail page)
     * Also ensures the submission belongs to the requesting user
     */
    public Submission getSubmissionById(Long submissionId, String userEmail) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Security check: make sure this submission belongs to the logged-in user
        if (!submission.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }

        return submission;
    }

    /**
     * Get history filtered by language
     */
    public List<Submission> getUserHistoryByLanguage(String userEmail, String language) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return submissionRepository.findByUserIdAndLanguageOrderBySubmittedAtDesc(user.getId(), language);
    }
}
