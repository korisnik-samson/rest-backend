package com.samson.restbackend.config;

import com.samson.restbackend.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String authentication = request.getHeader("Authorization");

            if (authentication != null && authentication.startsWith("Bearer ")) {
                String token = authentication.substring(7);
                Jws<Claims> claimsJws = jwtService.parseAndValidate(token);

                Claims claims = claimsJws.getPayload();
                String subject = claims.getSubject();
                String role = Optional.ofNullable(claims.get("role")).orElse("USER").toString();
                String username = claims.get("username", String.class);

                MDC.put("userId", subject);
                MDC.put("role", role);
                MDC.put("username", username);

                var authenticationToken = new AbstractAuthenticationToken(
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))) {
                    @Override public Object getCredentials() { return token; }
                    @Override public Object getPrincipal() { return subject; }
                };

                authenticationToken.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove("userId");
            MDC.remove("role");
            MDC.remove("username");
        }
    }
}
