package org.example.model;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;


//Author: Yuling Zang
public class Issue {
    @NotBlank(message = "Number is required")
    @NotBlank(message = "Title is required")
    @NotBlank(message = "State is required")
    @NotBlank(message = "Created_at is required")
    @NotBlank(message = "Updated_at is required")
    private Integer number;
    private String title;
    private String state;
    private String created_at;
    private String updated_at;
    private String body;
    private List<Label> labels = new ArrayList<>();
    private Integer totalComments;
    private String html_url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public Integer getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(Integer totalComments) {
        this.totalComments = totalComments;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public Issue() {
    }


    private static class Label {
        private String name; // 只需要标签名称
        private String color;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Label() {
        }
    }
}
