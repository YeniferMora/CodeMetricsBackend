package com.codemetrics.codemetrics_generator.controller;

import com.codemetrics.codemetrics_generator.service.CodeMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/codemetrics")
public class CodeMetricsController {

    @Autowired
    private CodeMetricsService codeMetricsService;
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeCode(@RequestBody String code) {
        String result = codeMetricsService.analyzeCode(code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/smellCodeAnalysis")
    public ResponseEntity<String> getResultByGemini(@RequestBody String metricsResult) {
        String resultCodeAnalysis = codeMetricsService.smellCodeAnalysis(metricsResult);
        return ResponseEntity.ok(resultCodeAnalysis);
    }
}
