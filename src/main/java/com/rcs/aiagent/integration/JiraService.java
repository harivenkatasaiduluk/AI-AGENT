package com.rcs.aiagent.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;

import java.util.Base64;

@Service
public class JiraService {

    private final RestTemplate restTemplate;

    public JiraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${jira.base-url}")
    private String baseUrl;

    @Value("${jira.email}")
    private String email;

    @Value("${jira.api-token}")
    private String apiToken;

    // ===============================
    // Get Issue (with retry)
    // ===============================
    public String getIssue(String issueKey) {

        String url = baseUrl + "/rest/api/3/issue/" + issueKey +
                "?fields=summary,description,status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();

            System.out.println("Jira API Response:");
            System.out.println(body);

            return body;

        } catch (HttpClientErrorException e) {
            System.out.println("Jira API error: " + e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
    // ===============================
    // Add Comment to Jira
    // ===============================
    public void addComment(String issueKey, String comment) {

        String url = baseUrl + "/rest/api/3/issue/" + issueKey + "/comment";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Jira Cloud requires ADF format
        String body = "{\n" +
                "  \"body\": {\n" +
                "    \"type\": \"doc\",\n" +
                "    \"version\": 1,\n" +
                "    \"content\": [\n" +
                "      {\n" +
                "        \"type\": \"paragraph\",\n" +
                "        \"content\": [\n" +
                "          {\n" +
                "            \"type\": \"text\",\n" +
                "            \"text\": \"" + comment + "\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        System.out.println("Comment added to Jira successfully");
    }

    // ===============================
    // Move Issue to Done
    // ===============================
    public void transitionIssue(String issueKey, String transitionId) {

        String url = baseUrl + "/rest/api/3/issue/" + issueKey + "/transitions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{ \"transition\": { \"id\": \"" + transitionId + "\" } }";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        System.out.println("Issue moved to Done");
    }

    private String getAuthToken() {
        String auth = email + ":" + apiToken;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }
}