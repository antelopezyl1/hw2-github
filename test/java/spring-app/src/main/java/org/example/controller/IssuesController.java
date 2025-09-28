package org.example.controller;

import org.example.model.Issue;
import org.example.model.IssueInput;
import org.example.service.GitHubService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/issues")
public class IssuesController {
    private final GitHubService gitHubService;

    public IssuesController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    //creat a new issue
    @PostMapping
    public ResponseEntity<Issue> postIssue(
            @Validated @RequestBody IssueInput issueInput) {
        Issue issue = this.gitHubService.postIssue(issueInput);
        return new ResponseEntity<>(issue, HttpStatus.CREATED);
    }

    //get issue list
    @GetMapping
    public ResponseEntity<List<Issue>> getIssues(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "state", defaultValue = "open") String state,
            @RequestParam(name = "per_page", defaultValue = "30") int perPage,
            @RequestParam(name = "labels", required = false) List<String> labels) {

        List<Issue> issues = this.gitHubService.getIssues(state,page,perPage,labels);
        return ResponseEntity.ok(issues);
    }

    //get a specific issue
    @GetMapping("/{number}")
    public ResponseEntity<Issue> getIssueByNumber(
            @PathVariable("number") int issueNumber)
    {
        Issue issue = this.gitHubService.getIssue(issueNumber);
        return ResponseEntity.ok(issue);
    }

    //update a specific issue
    @PatchMapping("/{number}")
    public ResponseEntity<Issue> patchIssue(
            @PathVariable("number") int issueNumber,
            @RequestBody IssueInput updateInput) {
        Issue updatedIssue = this.gitHubService.patchIssue(issueNumber,updateInput);
        return ResponseEntity.ok(updatedIssue);
    }

}
