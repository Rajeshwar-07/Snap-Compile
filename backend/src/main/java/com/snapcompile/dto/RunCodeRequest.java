package com.snapcompile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for run code request - sent from frontend editor
 */
@Data
public class RunCodeRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Language is required")
    private String language;   // "java", "python", "javascript", "sql"

    private String problemTitle;   // Optional title like "Fibonacci"

    // Optional: stdin input for the program
    private String stdin;

    // Optional: test cases to validate output
    // Format: [{"input":"5","expected":"120"}]
    private String testCases;

    // Optional: time and space complexity user entered
    private String timeComplexity;
    private String spaceComplexity;
}
