package com.codeninja.mfa.security;

import com.codeninja.mfa.model.entity.User;
import com.codeninja.mfa.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthProvider implements AuthenticationProvider {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthProvider(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        // 从传入的 Authentication 对象中获取用户名和密码
        String username = auth.getName();
        String password = auth.getCredentials().toString();

        // 从数据库或其他数据源中获取与提供的用户名相对应的用户详细信息
        Optional<User> userOptional = userRepo.findByUsername(username);
        if (userOptional.isPresent()) {
            // 使用 passwordEncoder 对提供的密码与数据库中存储的密码进行比较
            if (userOptional != null && passwordEncoder.matches(password, userOptional.get().getPassword())) {
                return new UsernamePasswordAuthenticationToken(userOptional.get(), password);
            } else {
                throw new BadCredentialsException("Invalid password");
            }
        } else {
            throw new UsernameNotFoundException("Username Not Found");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}