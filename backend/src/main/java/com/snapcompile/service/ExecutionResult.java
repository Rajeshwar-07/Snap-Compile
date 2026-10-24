package com.snapcompile.service;

import lombok.Data;

@Data
public class ExecutionResult {
    private String output;
    private String status;
    private String errorMessage;
    private String runtimeMs;
    private String memoryUsed;
}
