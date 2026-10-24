package com.snapcompile.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import lombok.Data;

@Service
public class Judge0Service {

    private static final String PISTON_URL = "https://emkc.org/api/v2/piston/execute";

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
        "java", "java",
        "python", "python",
        "javascript", "javascript",
        "cpp", "c++",
        "c", "c",
        "go", "go"
    );

    private static final Map<String, String> VERSION_MAP = Map.of(
        "java", "15.0.2",
        "python", "3.10.0",
        "javascript", "18.15.0",
        "cpp", "10.2.0",
        "c", "10.2.0",
        "go", "1.16.2"
    );

    public ExecutionResult executeCode(String code, String language, String stdin) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String lang = LANGUAGE_MAP.getOrDefault(language.toLowerCase(), "python");
            String version = VERSION_MAP.getOrDefault(language.toLowerCase(), "3.10.0");

            Map<String, Object> file = new HashMap<>();
            file.put("content", code);

            Map<String, Object> body = new HashMap<>();
            body.put("language", lang);
            body.put("version", version);
            body.put("files", List.of(file));
            if (stdin != null && !stdin.isEmpty()) {
                body.put("stdin", stdin);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(PISTON_URL, request, Map.class);

            Map run = (Map) response.getBody().get("run");
            String stdout = (String) run.get("stdout");
            String stderr = (String) run.get("stderr");
            int code2 = (int) run.get("code");

            ExecutionResult result = new ExecutionResult();
            result.setOutput(stdout);
            result.setStatus(code2 == 0 ? "ACCEPTED" : "RUNTIME_ERROR");
            result.setErrorMessage(stderr);
            result.setRuntimeMs("N/A");
            result.setMemoryUsed("N/A");
            return result;

        } catch (Exception e) {
            ExecutionResult result = new ExecutionResult();
            result.setStatus("RUNTIME_ERROR");
            result.setErrorMessage("Execution failed: " + e.getMessage());
            return result;
        }
    }
}

@Data
class ExecutionResult {
    private String output;
    private String status;
    private String errorMessage;
    private String runtimeMs;
    private String memoryUsed;
}
