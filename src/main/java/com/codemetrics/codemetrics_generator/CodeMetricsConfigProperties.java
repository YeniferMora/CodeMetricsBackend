package com.codemetrics.codemetrics_generator;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gemini")
public record CodeMetricsConfigProperties(String geminiApiKey) {
}
