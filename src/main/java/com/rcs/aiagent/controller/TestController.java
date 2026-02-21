package com.rcs.aiagent.controller;

import com.rcs.aiagent.integration.JiraService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    private final JiraService jiraService;

    public TestController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @GetMapping("/issue/{key}")
    public String getIssue(@PathVariable String key) {
        return jiraService.getIssue(key);
    }
}