package com.snapcompile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Submission entity - every time a user runs code, it is saved here.
 * This powers the History and Submission Detail pages.
 */
@Entity
@Table(name = "submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user made this submission
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Problem/title that the user gave (optional, e.g. "Fibonacci")
    @Column(name = "problem_title")
    private String problemTitle;

    // Programming language used (e.g. "java", "python", "javascript")
    @Column(nullable = false)
    private String language;

    // The actual source code - stored as TEXT to handle large code
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    // Status: "ACCEPTED", "WRONG_ANSWER", "RUNTIME_ERROR", "COMPILE_ERROR"
    @Column(nullable = false)
    private String status;

    // Program output
    @Column(columnDefinition = "TEXT")
    private String output;

    // Error message if compilation/runtime failed
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Execution time in milliseconds (e.g. "420ms")
    @Column(name = "runtime_ms")
    private String runtimeMs;

    // Memory used (e.g. "14 MB")
    @Column(name = "memory_used")
    private String memoryUsed;

    // Time complexity entered by user or calculated (e.g. "O(n log n)")
    @Column(name = "time_complexity")
    private String timeComplexity;

    // Space complexity (e.g. "O(n)")
    @Column(name = "space_complexity")
    private String spaceComplexity;

    // Test cases result stored as JSON string
    // Example: [{"input":"5","expected":"120","actual":"120","passed":true}]
    @Column(name = "test_cases_result", columnDefinition = "TEXT")
    private String testCasesResult;

    // Total test cases and how many passed
    @Column(name = "tests_passed")
    private Integer testsPassed;

    @Column(name = "tests_total")
    private Integer testsTotal;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
