package com.medicore.user.service;
import com.medicore.user.dto.LoginRequest;
import com.medicore.user.dto.RegisterRequest;
import com.medicore.user.entity.User;
import com.medicore.user.repository.UserRepository;
import com.medicore.user.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Test
    void registrationHashesPasswordAndReturnsToken() {
        var repository = mock(UserRepository.class);
        var jwt = mock(JwtService.class);
        var encoder = new BCryptPasswordEncoder();
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwt.generate("new@example.com", "USER")).thenReturn("token");

        var response = new UserService(repository, encoder, jwt)
            .register(new RegisterRequest("new@example.com", "secret123", "New User"));

        assertEquals("token", response.token());
        var saved = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertTrue(encoder.matches("secret123", saved.getValue().getPasswordHash()));
    }

    @Test
    void loginRejectsWrongPassword() {
        var repository = mock(UserRepository.class);
        var stored = new User("user@example.com", new BCryptPasswordEncoder().encode("correct123"), "User");
        var jwt = mock(JwtService.class);
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(stored));

        assertThrows(IllegalArgumentException.class, () -> new UserService(repository,
            new BCryptPasswordEncoder(), jwt)
            .login(new LoginRequest("user@example.com", "wrong123")));
        verifyNoInteractions(jwt);
    }
}
