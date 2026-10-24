package com.snapcompile.controller;

import com.snapcompile.dto.RunCodeRequest;
import com.snapcompile.model.Submission;
import com.snapcompile.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SubmissionController - Handles code run and history endpoints
 *
 * Base URL: /api/submissions
 * All endpoints REQUIRE JWT authentication
 */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * POST /api/submissions/run
     * Run code and save to history
     *
     * Headers: Authorization: Bearer <token>
     * Request body: { "code": "...", "language": "java", "problemTitle": "Fibonacci" }
     * Response: Full submission object with output, runtime, status
     */
    @PostMapping("/run")
    public ResponseEntity<Submission> runCode(
            @Valid @RequestBody RunCodeRequest request,
            @AuthenticationPrincipal UserDetails userDetails  // Injected from JWT automatically
    ) {
        Submission submission = submissionService.runAndSave(request, userDetails.getUsername());
        return ResponseEntity.ok(submission);
    }

    /**
     * GET /api/submissions/history
     * Get all submissions for logged-in user (History page)
     *
     * Headers: Authorization: Bearer <token>
     * Response: List of submission objects, newest first
     */
    @GetMapping("/history")
    public ResponseEntity<List<Submission>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<Submission> history = submissionService.getUserHistory(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/submissions/history?language=java
     * Get history filtered by language
     */
    @GetMapping("/history/filter")
    public ResponseEntity<List<Submission>> getHistoryByLanguage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String language
    ) {
        List<Submission> history = submissionService.getUserHistoryByLanguage(
                userDetails.getUsername(), language);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/submissions/{id}
     * Get single submission detail (Submission Detail page)
     *
     * Headers: Authorization: Bearer <token>
     * Response: Full submission with code, output, test cases, complexity
     */
    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Submission submission = submissionService.getSubmissionById(id, userDetails.getUsername());
        return ResponseEntity.ok(submission);
    }
}
