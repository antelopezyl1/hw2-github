package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/healthz") //mapping to path in openapi.yml
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
