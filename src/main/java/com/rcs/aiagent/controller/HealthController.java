package com.rcs.aiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "AI Agent is running successfully on EC2";
    }

    @GetMapping("/health")
    public String health() {
        return "Status: UP";
    }
}