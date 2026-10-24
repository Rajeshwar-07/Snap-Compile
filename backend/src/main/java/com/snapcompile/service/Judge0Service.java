package com.snapcompile.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class Judge0Service {

    private static final String GLOT_URL = "https://glot.io/api/run/%s/latest";

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
        "java", "java",
        "python", "python",
        "javascript", "javascript",
        "cpp", "cpp",
        "c", "c",
        "go", "go"
    );

    private static final Map<String, String> FILENAME_MAP = Map.of(
        "java", "Main.java",
        "python", "main.py",
        "javascript", "main.js",
        "cpp", "main.cpp",
        "c", "main.c",
        "go", "main.go"
    );

    public ExecutionResult executeCode(String code, String language, String stdin) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String lang = LANGUAGE_MAP.getOrDefault(language.toLowerCase(), "python");
            String filename = FILENAME_MAP.getOrDefault(language.toLowerCase(), "main.py");
            String url = String.format(GLOT_URL, lang);

            Map<String, Object> file = new HashMap<>();
            file.put("name", filename);
            file.put("content", code);

            Map<String, Object> body = new HashMap<>();
            body.put("files", List.of(file));
            if (stdin != null && !stdin.isEmpty()) {
                body.put("stdin", stdin);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            String stdout = (String) responseBody.get("stdout");
            String stderr = (String) responseBody.get("stderr");
            String error = (String) responseBody.get("error");

            ExecutionResult result = new ExecutionResult();
            result.setOutput(stdout);
            result.setRuntimeMs("N/A");
            result.setMemoryUsed("N/A");

            if (error != null && !error.isEmpty()) {
                result.setStatus("RUNTIME_ERROR");
                result.setErrorMessage(error);
            } else if (stderr != null && !stderr.isEmpty()) {
                result.setStatus("RUNTIME_ERROR");
                result.setErrorMessage(stderr);
            } else {
                result.setStatus("ACCEPTED");
            }

            return result;

        } catch (Exception e) {
            ExecutionResult result = new ExecutionResult();
            result.setStatus("RUNTIME_ERROR");
            result.setErrorMessage("Execution failed: " + e.getMessage());
            return result;
        }
    }
}
