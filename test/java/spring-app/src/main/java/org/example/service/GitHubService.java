package org.example.service;

import org.example.config.AppProperties;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Comment;
import org.example.model.CommentInput;
import org.example.model.Issue;
import org.example.model.IssueInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubService {
    //private final AppProperties properties;
    private final WebClient gitHubWebClient;

    @Autowired
    public GitHubService(AppProperties properties, WebClient gitHubWebClient) {
        //this.properties = properties;
        this.gitHubWebClient = gitHubWebClient;
    }

    // get issue list
    public List<Issue> getIssues(String state, int page, int perPage, List<String> labels) {
        return gitHubWebClient.get()
                // set api path and parameters
                .uri(uriBuilder -> uriBuilder.path("/issues")
                        .queryParam("state", state)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .queryParam("labels", labels)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        Mono.error(new RuntimeException("GitHub API Error: " + clientResponse.statusCode())))
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        Mono.error(new RuntimeException("GitHub Server Error")))
                .bodyToMono(new ParameterizedTypeReference<List<Issue>>() {})
                .block();
    }

    // post a issue to github
    public Issue postIssue(IssueInput issueInput) {
        return gitHubWebClient.post()
                .uri("/issues")
                // post a IssueInput object as requestBody
                .bodyValue(issueInput)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        Mono.error(new WebClientResponseException("Failed to create issue",
                                clientResponse.statusCode().value(),
                                clientResponse.statusCode().toString(),
                                null, null, null)))
                .bodyToMono(Issue.class)
                .block();
    }

    // get a specific issue
    public Issue getIssue(int issueNumber) {
        return gitHubWebClient.get()
                .uri("/issues/{number}", issueNumber)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse ->
                        Mono.error(new ResourceNotFoundException("Issue not found: " + issueNumber)))
                .bodyToMono(Issue.class)
                .block();
    }

    //update a specific issue
    public Issue patchIssue(int issueNumber, IssueInput updateInput) {
        return gitHubWebClient.patch()
                .uri("/issues/{number}", issueNumber)
                .bodyValue(updateInput)
                .retrieve()
                .bodyToMono(Issue.class)
                .block();
    }

    //create a comment
    public Comment postComment(int issueNumber, CommentInput commentInput) {
        return gitHubWebClient.post()
                .uri("/issues/{number}/comments", issueNumber)
                .bodyValue(commentInput)
                .retrieve()
                .bodyToMono(Comment.class)
                .block();
    }

    // get comment list
    public List<Comment> getComments(int issueNumber, int page, int perPage) {
        return gitHubWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/issues/{number}/comments")
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build(issueNumber))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Comment>>() {})
                .block();
    }

    //update a specific comment
    public Comment patchComment(Long commentId, CommentInput updateInput) {
        return gitHubWebClient.patch()
                .uri("/issues/comments/{commentId}", commentId)
                .bodyValue(updateInput)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse ->
                        Mono.error(new ResourceNotFoundException("Comment not found: " + commentId)))
                .bodyToMono(Comment.class)
                .block();
    }

    // delete a specific comment
    public void deleteComment(Long commentId) {
        this.gitHubWebClient.delete()
                .uri("/issues/comments/{commentId}", commentId)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    return Mono.error(new org.springframework.web.reactive.function.client.WebClientResponseException(
                            "Failed to delete comment on GitHub",
                            clientResponse.statusCode().value(),
                            clientResponse.statusCode().toString(),
                            null, null, null));
                })
                .toBodilessEntity()
                .block();
    }
}



