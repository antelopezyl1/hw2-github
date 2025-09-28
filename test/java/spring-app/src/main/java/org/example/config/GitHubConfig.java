package org.example.config;

import org.example.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GitHubConfig {

    //basic url of GitHub API
    private static final String GITHUB_API_BASE_URL = "https://api.github.com/repos/";

    @Bean
    public WebClient gitHubWebClient(AppProperties properties, WebClient.Builder webClientBuilder) {

        // get Auth info and repo path
        String owner = properties.getGithub().getOwner();
        String repo = properties.getGithub().getRepo();
        String token = properties.getGithub().getToken();

        // repo URL: https://api.github.com/repos/{owner}/{repo}
        String baseUrl = GITHUB_API_BASE_URL + owner + "/" + repo;

        // default settings of WebClient
        return webClientBuilder
                // set baseUrl
                .baseUrl(baseUrl)

                // Authorization: Bearer ${GITHUB_TOKEN} or token ${GITHUB_TOKEN}
                .defaultHeader(HttpHeaders.AUTHORIZATION, "token " + token)

                // set Accept header
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")

                // set Content-Type header
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

                .build();
    }
}