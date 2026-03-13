package com.samson.restbackend.coverage.greybox;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerGreyBoxTest {

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
    void refreshRotatesTokenAndReturnsNewPair() throws Exception {
        UUID userId = UUID.randomUUID();
        String oldRefresh = "old.refresh.token";
        String oldJwtId = "old-jti";
        String newAccess = "new.access.token";
        String newRefresh = "new.refresh.token";

        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.get("typ", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn(oldJwtId);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(jwtService.parseAndValidate(oldRefresh)).thenReturn(jws);
        when(jwtService.isRefreshTokenExpired(oldRefresh)).thenReturn(false);
        when(jwtService.isRefreshTokenExpired(oldJwtId)).thenReturn(false);
        when(jwtService.generateAccessToken(userId, "sam", UserRole.USER.name())).thenReturn(newAccess);
        when(jwtService.generateRefreshToken(userId)).thenReturn(newRefresh);
        when(userRepository.findById(userId)).thenReturn(Optional.of(
                Users.builder().user_id(userId).username("sam").email("sam@example.com").userRole(UserRole.USER).password("hashed").build()
        ));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refresh_token", oldRefresh))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(newAccess))
                .andExpect(jsonPath("$.refresh_token").value(newRefresh))
                .andExpect(jsonPath("$.token_type").value("Bearer"));

        verify(jwtService).revokeRefreshToken(oldJwtId);
    }
}
