package com.rcs.aiagent.service;

import com.rcs.aiagent.model.DeactivationRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIExtractionService {

    public DeactivationRequest extract(String text) {

        DeactivationRequest request = new DeactivationRequest();
        request.setAction("deactivate");

        // Extract email
        Pattern emailPattern =
                Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = emailPattern.matcher(text);

        if (matcher.find()) {
            request.setEmail(matcher.group());
        }

        // Detect systems
        List<String> systems = new ArrayList<>();

        String lower = text.toLowerCase();

        if (lower.contains("jira")) {
            systems.add("jira");
        }

        if (lower.contains("confluence")) {
            systems.add("confluence");
        }

        if (lower.contains("azure")) {
            systems.add("azure");
        }

        request.setSystems(systems);

        return request;
    }
}