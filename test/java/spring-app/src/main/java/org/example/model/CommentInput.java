package org.example.model;

import jakarta.validation.constraints.NotBlank;

//Author: Yuling Zang
public class CommentInput {
    @NotBlank(message = "Body is required")
    private String body;
    public String getBody() {return body;}
    public void setBody(String body) {this.body = body;}

    public CommentInput(){}
}
