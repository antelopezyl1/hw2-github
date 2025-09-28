package org.example.model;

import jakarta.validation.constraints.NotBlank;

public class ErrorResponse {
    @NotBlank(message = "Code is required")
    @NotBlank(message = "Message is required")
    private Integer code;
    private String message;

    public Integer getCode() {return code;}
    public void setCode(Integer code) {this.code = code;}
    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}

    public ErrorResponse(){}
}
