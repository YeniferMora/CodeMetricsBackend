package com.codemetrics.codemetrics_generator.controller;

import com.codemetrics.codemetrics_generator.CodeMetricsConfigProperties;
import com.codemetrics.codemetrics_generator.service.CodeMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/codemetrics")
public class CodeMetricsController {

    private final CodeMetricsConfigProperties codeMetricsConfigProperties;
    @Autowired
    private CodeMetricsService codeMetricsService;

    public CodeMetricsController(CodeMetricsConfigProperties codeMetricsConfigProperties) {
        this.codeMetricsConfigProperties = codeMetricsConfigProperties;
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeCode(@RequestBody String code) {
        String result = codeMetricsService.analyzeCode(code);
        System.out.println(result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/smellCodeAnalysis")
    public ResponseEntity<String> getSmellCodeAnalysis(@RequestBody String metricsResult) {
        String resultCodeAnalysis = codeMetricsService.smellCodeAnalysis(metricsResult,codeMetricsConfigProperties.geminiApiKey());
        System.out.println(resultCodeAnalysis);
        return ResponseEntity.ok(resultCodeAnalysis);
    }

    @PostMapping("/codeDescription")
    public ResponseEntity<String> getCodeDescription(@RequestBody String metricsResult) {
        String resultCodeAnalysis = codeMetricsService.codeDescription(metricsResult,codeMetricsConfigProperties.geminiApiKey());
        System.out.println(resultCodeAnalysis);
        return ResponseEntity.ok(resultCodeAnalysis);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Welcome to CodeMetrics API!");
    }

}
