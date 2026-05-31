package com.example.backend_service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    System.out.println(">>> Incoming request: " + request.getMethod() + " " + request.getRequestURI());
    String auth = request.getHeader("Authorization");
    System.out.println(">>> Authorization header: " + (auth != null ? auth.substring(0, Math.min(30, auth.length())) + "..." : "MISSING"));
    chain.doFilter(request, response);
  }
}