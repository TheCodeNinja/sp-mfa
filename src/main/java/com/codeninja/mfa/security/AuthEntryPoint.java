package com.codeninja.mfa.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 通过 @Component 注解，Spring 将会自动扫描并将被注解的类实例化为一个可供应用程序使用的 Spring Bean。
// 具体来说，当发生身份验证异常时，该方法会记录错误日志并向客户端发送一个未经授权的响应。
@Component
@Slf4j
public class AuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        log.error("Unauthorized {}", authException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not Authenticated");
    }
}
