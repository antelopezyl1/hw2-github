package org.example.model;

import jakarta.validation.constraints.NotBlank;

//Author: Yuling Zang
public class Comment {
    @NotBlank(message = "Id is required")
    @NotBlank(message = "Body is required")
    @NotBlank(message = "User is required")
    @NotBlank(message = "Created_at is required")
    @NotBlank(message = "Html_url is required")
    private Long id;
    private String body;
    private String created_at;
    private Object user;
    private String html_url;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getBody() {return body;}
    public void setBody(String body) {this.body = body;}
    public String getCreated_at() {return created_at;}
    public void setCreated_at(String created_at) {this.created_at = created_at;}
    public Object getUser() {return user;}
    public void setUser(Object user) {this.user = user;}
    public String getHtml_url() {return html_url;}
    public void setHtml_url(String html_url) {this.html_url = html_url;}

    public Comment(){}

}
