package com.codeninja.mfa.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthEntryPoint authExceptionHandler;

    public SecurityConfig(JwtFilter jwtFilter, AuthEntryPoint authExceptionHandler) {
        this.jwtFilter = jwtFilter;
        this.authExceptionHandler = authExceptionHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 对来自 http://localhost:4200 域的跨域请求的控制。
    // 只允许该域的 GET 和 POST 请求，并允许的请求头包括 "authorization" 和 "content-type"。
    // 这有助于确保应用程序在处理跨域请求时遵循安全策略
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // 创建了一个 CorsConfiguration 对象
        // 这些设置指定了在跨域请求中允许的来源、请求方法和请求头
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
        configuration.setAllowedHeaders(Arrays.asList("authorization","content-type"));

        // 创建一个 UrlBasedCorsConfigurationSource 对象
        // 将刚创建的 CorsConfiguration 对象注册到该对象中
        // 使用 "/**" 表示将此配置应用于所有的路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // csrf
    // CSRF 是一种攻击方式，攻击者利用用户已经认证的身份，在用户不知情的情况下发送恶意请求。为了防止这种攻击，应用程序通常会实施 CSRF 保护机制。
    // 禁用 CSRF 保护意味着在应用程序中不会执行针对 CSRF 攻击的防御措施。这通常在以下情况下使用：
    // 1. 应用程序使用无状态的身份验证机制，如基于令牌的身份验证（如 JWT）。
    // 2. 应用程序不涉及敏感操作，或已经通过其他方式保护了敏感操作。
    @Bean
    public SecurityFilterChain defaultFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(handle -> handle.authenticationEntryPoint(authExceptionHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error**","confirm-email","/register**","/login**","/verifyTotp**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}