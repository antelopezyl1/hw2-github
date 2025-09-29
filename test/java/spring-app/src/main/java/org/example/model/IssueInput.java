package org.example.model;

import jakarta.validation.constraints.NotBlank;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//Author: Yuling Zang
public class IssueInput {
    @NotBlank(message = "Title is required")
    private String title;
    private String body;
    private List<String> labels = new ArrayList<>();;

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getBody() {return body;}
    public void setBody(String body) {this.body = body;}
    public List<String> getLabels() {return labels;}
    public void setLabels(List<String> labels) {this.labels = labels;}

    public IssueInput(){
    }
}
