package com.samson.restbackend.coverage.whitebox;

import com.samson.restbackend.api.dto.CreateUserRequest;
import com.samson.restbackend.api.dto.UserResponse;
import com.samson.restbackend.models.Users;
import com.samson.restbackend.repositories.UserRepository;
import com.samson.restbackend.services.UserService;
import com.samson.restbackend.util.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceWhiteBoxTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService service;

    @Test
    void createRejectsDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("sam", "sam@example.com", "password123", UserRole.USER);
        when(repository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createRejectsBlankPassword() {
        CreateUserRequest request = new CreateUserRequest("sam", "sam@example.com", "   ", UserRole.USER);
        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(repository.existsByUsername(request.username())).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createPersistsEncodedPasswordAndReturnsResponse() {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest("sam", "sam@example.com", "password123", UserRole.ADMIN);

        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(repository.existsByUsername(request.username())).thenReturn(false);
        when(encoder.encode(request.password())).thenReturn("encoded");

        when(repository.save(any(Users.class))).thenAnswer(invocation -> {
            Users u = invocation.getArgument(0);
            u.setUser_id(userId);
            return u;
        });

        UserResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(userId.toString());
        assertThat(response.username()).isEqualTo("sam");
        assertThat(response.email()).isEqualTo("sam@example.com");
        assertThat(response.userRole()).isEqualTo("ADMIN");
    }
}
