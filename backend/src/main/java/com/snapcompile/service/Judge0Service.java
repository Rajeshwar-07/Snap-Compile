package com.snapcompile.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Judge0Service - Sends code to Judge0 API for execution
 *
 * Judge0 is a free online code execution API.
 * API Docs: https://ce.judge0.com/
 *
 * Language IDs (Judge0):
 * - Java      = 62
 * - Python 3  = 71
 * - JavaScript = 63
 * - C++        = 54
 * - C          = 50
 */
@Service
public class Judge0Service {

    @Value("${judge0.api.url}")
    private String judge0Url;

    @Value("${judge0.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Map language name to Judge0 language ID
    private static final Map<String, Integer> LANGUAGE_IDS = new HashMap<>();
    static {
        LANGUAGE_IDS.put("java", 62);
        LANGUAGE_IDS.put("python", 71);
        LANGUAGE_IDS.put("javascript", 63);
        LANGUAGE_IDS.put("cpp", 54);
        LANGUAGE_IDS.put("c", 50);
        LANGUAGE_IDS.put("csharp", 51);
        LANGUAGE_IDS.put("go", 60);
        LANGUAGE_IDS.put("rust", 73);
        LANGUAGE_IDS.put("typescript", 74);
        LANGUAGE_IDS.put("kotlin", 78);
    }

    /**
     * Result object returned after code execution
     */
    public static class ExecutionResult {
        public String output;
        public String errorMessage;
        public String status;       // "ACCEPTED", "WRONG_ANSWER", "RUNTIME_ERROR", etc.
        public String runtimeMs;
        public String memoryUsed;
    }

    /**
     * Execute code using Judge0 API
     *
     * @param code     - source code to run
     * @param language - "java", "python", "javascript", etc.
     * @param stdin    - optional standard input
     * @return ExecutionResult with output and stats
     */
    public ExecutionResult executeCode(String code, String language, String stdin) {

        ExecutionResult result = new ExecutionResult();

        try {
            // Get Judge0 language ID
            Integer languageId = LANGUAGE_IDS.getOrDefault(language.toLowerCase(), 71);

            // Build request body
            // Judge0 requires code to be Base64 encoded
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("source_code", Base64.getEncoder().encodeToString(code.getBytes()));
            requestBody.put("language_id", languageId);
            requestBody.put("encode_output", false);
            if (stdin != null && !stdin.isEmpty()) {
                requestBody.put("stdin", Base64.getEncoder().encodeToString(stdin.getBytes()));
            }

            // Set headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // POST to Judge0 - submit code
            ResponseEntity<String> submitResponse = restTemplate.postForEntity(
                    judge0Url + "/submissions?base64_encoded=false&wait=true",
                    entity,
                    String.class
            );

            // Parse response
            JsonNode responseJson = objectMapper.readTree(submitResponse.getBody());

            // Extract output
            result.output = responseJson.has("stdout") && !responseJson.get("stdout").isNull()
                    ? responseJson.get("stdout").asText().trim()
                    : "";

            // Extract error
            if (responseJson.has("stderr") && !responseJson.get("stderr").isNull()) {
                result.errorMessage = responseJson.get("stderr").asText();
            }
            if (responseJson.has("compile_output") && !responseJson.get("compile_output").isNull()) {
                result.errorMessage = responseJson.get("compile_output").asText();
            }

            // Extract runtime and memory
            result.runtimeMs = responseJson.has("time")
                    ? responseJson.get("time").asText() + "s"
                    : "N/A";
            result.memoryUsed = responseJson.has("memory")
                    ? responseJson.get("memory").asText() + " KB"
                    : "N/A";

            // Determine status
            if (responseJson.has("status")) {
                int statusId = responseJson.get("status").get("id").asInt();
                result.status = mapStatus(statusId);
            }

        } catch (Exception e) {
            result.status = "RUNTIME_ERROR";
            result.errorMessage = "Failed to connect to code execution service: " + e.getMessage();
        }

        return result;
    }

    /**
     * Map Judge0 status ID to readable status
     * Reference: https://ce.judge0.com/#statuses-and-languages-status-get
     */
    private String mapStatus(int statusId) {
        return switch (statusId) {
            case 3 -> "ACCEPTED";
            case 4 -> "WRONG_ANSWER";
            case 5 -> "TIME_LIMIT_EXCEEDED";
            case 6 -> "COMPILE_ERROR";
            case 7, 8, 9, 10, 11, 12 -> "RUNTIME_ERROR";
            default -> "UNKNOWN";
        };
    }
}
