package com.example.backend_service.authinterceptor;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class SupabaseJwtDecoderConfig {

  @Value("${supabase.jwks.url}")
  private String jwksUrl;

  @Value("${supabase.api.key}")
  private String supabaseApiKey;

  @Bean
  public JwtDecoder jwtDecoder() {
    // Diagnostic: fetch and log JWKS
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.getInterceptors().add((request, body, execution) -> {
        request.getHeaders().set("apikey", supabaseApiKey);
        return execution.execute(request, body);
      });
      String jwksJson = restTemplate.getForObject(jwksUrl, String.class);
      System.out.println("=== JWKS Response ===");
      System.out.println(jwksJson);
      JWKSet jwkSet = JWKSet.parse(jwksJson);
      System.out.println("Number of keys: " + jwkSet.getKeys().size());
      if (!jwkSet.getKeys().isEmpty()) {
        var key = jwkSet.getKeys().get(0);
        System.out.println("Key ID (kid): " + key.getKeyID());
        System.out.println("Algorithm: " + key.getAlgorithm());
        System.out.println("Key type: " + key.getKeyType());
      }
    } catch (Exception e) {
      System.err.println("Failed to fetch or parse JWKS: " + e.getMessage());
      e.printStackTrace();
    }

    // Build decoder with ES256 support using Spring's SignatureAlgorithm
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add((request, body, execution) -> {
      request.getHeaders().set("apikey", supabaseApiKey);
      return execution.execute(request, body);
    });

    NimbusJwtDecoder nimbusDecoder = NimbusJwtDecoder
        .withJwkSetUri(jwksUrl)
        .restOperations(restTemplate)
        .jwsAlgorithm(SignatureAlgorithm.ES256)   // ✅ correct type
        .build();

    return new JwtDecoder() {
      @Override
      public Jwt decode(String token) throws JwtException {
        System.out.println("=== JWT Decoding Start ===");
        System.out.println("Token (first 50 chars): " + token.substring(0, Math.min(50, token.length())));

        try {
          String[] parts = token.split("\\.");
          if (parts.length < 2) throw new JwtException("Invalid JWT format");
          String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
          System.out.println("JWT Header: " + headerJson);
          String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
          System.out.println("JWT Payload (issuer): " + payloadJson);
        } catch (Exception e) {
          System.err.println("Failed to decode JWT parts: " + e.getMessage());
        }

        try {
          Jwt jwt = nimbusDecoder.decode(token);
          System.out.println("✅ JWT valid. Subject: " + jwt.getSubject());
          System.out.println("=== JWT Decoding End ===");
          return jwt;
        } catch (JwtException e) {
          System.err.println("❌ JWT decoding error: " + e.getMessage());
          e.printStackTrace();
          throw e;
        }
      }
    };
  }
}