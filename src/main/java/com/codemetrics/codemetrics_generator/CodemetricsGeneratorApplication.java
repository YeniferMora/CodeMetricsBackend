package com.codemetrics.codemetrics_generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CodeMetricsConfigProperties.class)
public class CodemetricsGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodemetricsGeneratorApplication.class, args);
	}

}
