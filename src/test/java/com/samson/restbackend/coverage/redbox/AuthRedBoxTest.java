package com.samson.restbackend.coverage.redbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samson.restbackend.api.dto.AuthDtos;
import com.samson.restbackend.controllers.AuthController;
import com.samson.restbackend.models.Users;
import com.samson.restbackend.repositories.UserRepository;
import com.samson.restbackend.services.JwtService;
import com.samson.restbackend.util.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRedBoxTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        Users user = Users.builder()
                .user_id(UUID.randomUUID())
                .username("sam")
                .email("sam@example.com")
                .password("hashed")
                .userRole(UserRole.USER)
                .build();
        when(userRepository.findByEmail("sam@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("sam@example.com", "wrongpassword");
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithInvalidTokenTypeReturnsUnauthorized() throws Exception {
        String jwt = "dummy.jwt.token";
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.get("typ", String.class)).thenReturn("access");
        when(jwtService.isRefreshTokenExpired(jwt)).thenReturn(false);
        when(jwtService.parseAndValidate(jwt)).thenReturn(jws);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refresh_token", jwt))))
                .andExpect(status().isUnauthorized());
    }
}
