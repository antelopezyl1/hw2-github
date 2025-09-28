package org.example.controller;

import org.example.config.AppProperties;
import org.example.model.Comment;
import org.example.model.CommentInput;
import org.example.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/issues/{issueNumber}/comments")
@Validated
public class CommentsController {
    private final GitHubService gitHubService;

    public CommentsController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // post /issues/{number}/comments
    @PostMapping
    public ResponseEntity<Comment> postComment(
            @PathVariable int issueNumber,
            @Validated @RequestBody CommentInput commentInput)
    {
        Comment comment = this.gitHubService.postComment(issueNumber,commentInput);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    //get comments list
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(
            @PathVariable int issueNumber,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int perPage)
    {
        List<Comment> Comments = this.gitHubService.getComments(issueNumber,page,perPage);
        return ResponseEntity.ok(Comments);
    }

    //update a specific comment
    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable int issueNumber,
            @RequestBody CommentInput updateInput)
    {
        Comment updatedComment = this.gitHubService.patchComment(issueNumber, updateInput);
        return ResponseEntity.ok(updatedComment);
    }

    //delete a specific comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable int issueNumber,
            @PathVariable int commentId)
    {
        this.gitHubService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
