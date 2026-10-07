package com.ecommerce.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.exception.ConflictException;
import com.ecommerce.security.JwtService;
import com.ecommerce.user.Role;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import com.ecommerce.user.UserResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserWithEncodedPasswordAndUserRole() {
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponse response = authService.register(new RegisterRequest(" João ", " JOAO@email.com ", "senha1234"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hash", captor.getValue().getPassword());
        assertEquals("joao@email.com", response.email());
        assertEquals("João", response.name());
        assertEquals(Role.USER, response.role());
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> authService.register(new RegisterRequest("João", "joao@email.com", "senha1234")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnTokenOnLogin() {
        User user = User.builder().id(1L).email("joao@email.com").role(Role.USER).build();
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("joao@email.com", "USER")).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("joao@email.com", "senha1234"));

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
    }

    @Test
    void shouldPropagateBadCredentialsOnLogin() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("joao@email.com", "errada")));

        verify(jwtService, never()).generateToken(any(), any());
    }
}
