package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


// Spring Boot can check configs with prefix = "app" in application.yml
//Author: Yuling Zang
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private GitHub github = new GitHub();
    private WebHook webhook = new WebHook();

    public static class GitHub {
        private String token;
        private String owner;
        private String repo;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public String getRepo() { return repo; }
        public void setRepo(String repo) { this.repo = repo; }
    }

    public static class WebHook {
        private String secret;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
    }

    public GitHub getGithub() { return github; }
    public void setGithub(GitHub github) { this.github = github; }

    public WebHook getWebhook() { return webhook; }
    public void setWebhook(WebHook webhook) { this.webhook = webhook; }

}
