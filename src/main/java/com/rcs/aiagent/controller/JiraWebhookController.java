package com.rcs.aiagent.controller;

import com.rcs.aiagent.integration.JiraService;
import com.rcs.aiagent.service.AIExtractionService;
import com.rcs.aiagent.model.DeactivationRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook/jira")
public class JiraWebhookController {

    private final JiraService jiraService;
    private final AIExtractionService aiService;

    public JiraWebhookController(JiraService jiraService,
                                 AIExtractionService aiService) {
        this.jiraService = jiraService;
        this.aiService = aiService;
    }

    @PostMapping
    public String handleWebhook(@RequestBody Map<String, Object> payload) {

        try {

            // 1. Identify event type
            String eventType = (String) payload.get("webhookEvent");

            // Process only create/update
            if (!"jira:issue_created".equals(eventType) &&
                !"jira:issue_updated".equals(eventType)) {
                System.out.println("Ignoring event: " + eventType);
                return "Ignored";
            }

            System.out.println("Processing event: " + eventType);

            // 2. Extract issue key
            Map issue = (Map) payload.get("issue");
            String issueKey = (String) issue.get("key");

            System.out.println("Issue Key: " + issueKey);

            // 3. Wait for Jira indexing
            Thread.sleep(2000);

            // 4. Fetch full issue
            String issueDetails = jiraService.getIssue(issueKey);

            if (issueDetails == null || issueDetails.isEmpty()) {
                System.out.println("Issue details empty. Skipping.");
                return "No data";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(issueDetails);

            // 5. Check current status (Prevent infinite loop)
            String status = root.path("fields")
                                .path("status")
                                .path("name")
                                .asText();

            System.out.println("Current Status: " + status);

            // If already Done → skip
            if ("Done".equalsIgnoreCase(status)) {
                System.out.println("Issue already Done. Skipping processing.");
                return "Already Done";
            }

            // 6. Extract description text (ADF)
            JsonNode descriptionNode = root.path("fields").path("description");
            String text = extractText(descriptionNode);

            // 7. Enterprise fallback → use Summary (Primary source)
            if (text == null || text.isEmpty()) {
                text = root.path("fields").path("summary").asText("");
                System.out.println("Description empty, using Summary instead.");
            }

            System.out.println("Final Text Used: " + text);

            // 8. AI Extraction
            DeactivationRequest request = aiService.extract(text);

            System.out.println("Extracted Email: " + request.getEmail());
            System.out.println("Systems: " + request.getSystems());
            System.out.println("Action: " + request.getAction());

            // 9. Autonomous Execution
            if ("deactivate".equalsIgnoreCase(request.getAction())
                    && request.getEmail() != null) {

                System.out.println("Executing deactivation for: " + request.getEmail());

                String comment = "AI Agent: User " + request.getEmail()
                        + " deactivated from systems: " + request.getSystems();

                // Add comment
                jiraService.addComment(issueKey, comment);

                // Move issue to Done
                jiraService.transitionIssue(issueKey, "31");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Webhook processed";
    }

    // Recursive extractor for Jira ADF format
    private String extractText(JsonNode node) {

        if (node == null || node.isMissingNode()) {
            return "";
        }

        StringBuilder text = new StringBuilder();

        if (node.has("text")) {
            text.append(node.get("text").asText()).append(" ");
        }

        if (node.has("content")) {
            for (JsonNode child : node.get("content")) {
                text.append(extractText(child));
            }
        }

        return text.toString().trim();
    }
}