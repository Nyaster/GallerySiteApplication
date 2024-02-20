package com.gallery.galleryapplication.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;

public class CustomAuthenteficationSuccesHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String ip = getClientId(request);
        String browser = request.getHeader("User-Agent");
        String authority = userDetails.getAuthorities().stream().findFirst().get().getAuthority();
        LocalDateTime loginTime = LocalDateTime.now();
        System.out.println(username + ip + browser + loginTime+authority);
        response.sendRedirect("/");
    }

    private String getClientId(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header == null) {
            return request.getRemoteAddr();
        }

        return header.split(",")[0];
    }
}
