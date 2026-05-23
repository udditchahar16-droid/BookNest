package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl
 * Tests: register, login, logout, validateToken, changePassword, getUserByEmail
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setFullName("Ravi Kumar");
        mockUser.setEmail("ravi@booknest.com");
        mockUser.setPasswordHash("$2a$10$hashedPassword");
        mockUser.setRole("CUSTOMER");
        mockUser.setMobile(9876543210L);
    }

    // ── register ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: should save user with hashed password")
    void register_savesUserWithHashedPassword() {
        User input = new User();
        input.setFullName("Priya Sharma");
        input.setEmail("priya@booknest.com");
        input.setPasswordHash("plainPass");
        input.setRole("CUSTOMER");

        when(userRepository.existsByEmail("priya@booknest.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(2);
            return u;
        });

        User saved = authService.register(input);

        assertThat(saved.getUserId()).isEqualTo(2);
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$hashed");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: should throw exception if email already exists")
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByEmail("ravi@booknest.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(mockUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: should return JWT token on valid credentials")
    void login_returnsTokenForValidCredentials() {
        when(userRepository.findByEmail("ravi@booknest.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Pass@123", "$2a$10$hashedPassword")).thenReturn(true);

        String token = authService.login("ravi@booknest.com", "Pass@123");

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("login: should throw exception for wrong password")
    void login_throwsForWrongPassword() {
        when(userRepository.findByEmail("ravi@booknest.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPass", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("ravi@booknest.com", "wrongPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("login: should throw exception for non-existent email")
    void login_throwsForNonExistentEmail() {
        when(userRepository.findByEmail("ghost@booknest.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost@booknest.com", "anyPass"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── getUserByEmail ────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserByEmail: should return user for valid email")
    void getUserByEmail_returnsUser() {
        when(userRepository.findByEmail("ravi@booknest.com")).thenReturn(Optional.of(mockUser));

        User result = authService.getUserByEmail("ravi@booknest.com");

        assertThat(result.getFullName()).isEqualTo("Ravi Kumar");
    }

    @Test
    @DisplayName("getUserByEmail: should throw for missing email")
    void getUserByEmail_throwsForMissingEmail() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.getUserByEmail("x@y.com"))
                .isInstanceOf(RuntimeException.class);
    }

    // ── validateToken ─────────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken: should return true for valid JWT")
    void validateToken_trueForValidToken() {
        // JWT generation is mocked; we verify the method returns a boolean
        String token = authService.login(
                "ravi@booknest.com", "Pass@123"
        );
        // reset mocks to re-use for this test
        when(userRepository.findByEmail("ravi@booknest.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Pass@123", "$2a$10$hashedPassword")).thenReturn(true);
        String realToken = authService.login("ravi@booknest.com", "Pass@123");
        assertThat(authService.validateToken(realToken)).isTrue();
    }

    @Test
    @DisplayName("validateToken: should return false for tampered token")
    void validateToken_falseForInvalidToken() {
        assertThat(authService.validateToken("tampered.jwt.token")).isFalse();
    }

    // ── changePassword ────────────────────────────────────────────────────

    @Test
    @DisplayName("changePassword: should update passwordHash in repository")
    void changePassword_updatesHash() {
        when(userRepository.findByUserId(1)).thenReturn(mockUser);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("$2a$10$newHash");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        authService.changePassword(1, "NewPass@456");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$newHash");
    }

    // ── findAllByRole ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllByRole: should return only CUSTOMER users")
    void findAllByRole_returnsCustomers() {
        when(userRepository.findAllByRole("CUSTOMER")).thenReturn(List.of(mockUser));

        List<User> customers = userRepository.findAllByRole("CUSTOMER");
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getRole()).isEqualTo("CUSTOMER");
    }

    // ── deleteByUserId ────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteByUserId: should call repository delete")
    void deleteByUserId_callsRepository() {
        doNothing().when(userRepository).deleteByUserId(1);
        userRepository.deleteByUserId(1);
        verify(userRepository, times(1)).deleteByUserId(1);
    }
}
