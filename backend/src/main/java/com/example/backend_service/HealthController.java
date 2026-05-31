package com.example.backend_service;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
  @GetMapping("/health")
  public Map<String, String> health() {
    System.out.println("Health endpoint reached, user: " + "akjayy");
    return Map.of("status", "ok", "timestamp", Instant.now().toString());
  }
}