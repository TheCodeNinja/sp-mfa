package com.codeninja.mfa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // Retrieve token from the request
            String jwt = getJWT(request);
            if (Objects.nonNull(jwt)) {
                // Validate the JWT from the Request
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) jwtService.validateJwt(jwt);
                // Set the details of the authenticated user
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set the authenticated user in the security context
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Log the exception if any error occurs during processing the JWT
            log.error("Exception wile processing the JWT"+e.getMessage());
        }
        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    private String getJWT(HttpServletRequest request) {
        // Retrieve the "authorization" header from the request
        String jwt = request.getHeader("authorization");
        if (Objects.nonNull(jwt) && jwt.startsWith("Bearer") && jwt.length() > 7) {
            // Extract the JWT token by removing the "Bearer " prefix
            return jwt.substring(7);
        }
        // Return null if the "authorization" header is missing or doesn't contain a valid JWT
        return null;
    }
}