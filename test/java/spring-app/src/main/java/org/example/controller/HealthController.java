package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//Author: Yuling Zang
@RestController
@CrossOrigin(origins = "*")
public class HealthController {
    @GetMapping("/healthz") //mapping to path in openapi.yml
    public ResponseEntity<Map<String,String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
