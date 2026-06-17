package mn.astvision.ard.service;

import mn.astvision.ard.api.dto.LoginRequest;
import mn.astvision.ard.api.dto.RegisterRequest;
import mn.astvision.ard.data.User;
import mn.astvision.ard.repo.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Pure unit tests for {@link AuthService}.
 * <p>
 * No Spring context and no real database: every collaborator
 * ({@link UserRepository}, {@link PasswordEncoder}, {@link AuthenticationManager})
 * is replaced by a Mockito mock so we test only the service logic in isolation.
 * <p>
 * Educational focus: each {@code @Nested} block is one method, and each {@code @Test}
 * is one distinct use case (happy path + the ways it can fail).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ---------------------------------------------------------------------
    // register(...)
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("register")
    class Register {

        private final RegisterRequest request =
                new RegisterRequest("jdoe", "secret123", "John", "Doe");

        @Test
        @DisplayName("creates a new user with an encoded password and the default USER role")
        void register_newUsername_savesEncodedUser() {
            // given: username is free, the encoder turns the raw password into a hash,
            //        and the repository echoes back whatever it is asked to save.
            when(userRepository.findByUsername("jdoe")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("secret123")).thenReturn("ENCODED_HASH");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            User saved = authService.register(request);

            // then: the password is never stored in plain text and the default role is applied
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User persisted = captor.getValue();

            assertThat(persisted.getUsername()).isEqualTo("jdoe");
            assertThat(persisted.getFirstName()).isEqualTo("John");
            assertThat(persisted.getLastName()).isEqualTo("Doe");
            assertThat(persisted.getPassword()).isEqualTo("ENCODED_HASH");
            assertThat(persisted.getPassword()).isNotEqualTo("secret123");
            assertThat(persisted.getRoles()).containsExactly("USER");

            assertThat(saved).isSameAs(persisted);
        }

        @Test
        @DisplayName("rejects a username that is already taken with 409 CONFLICT")
        void register_duplicateUsername_throwsConflict() {
            // given: the username already exists
            when(userRepository.findByUsername("jdoe"))
                    .thenReturn(Optional.of(User.builder().username("jdoe").build()));

            // when / then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(CONFLICT));

            // and: we must never encode a password or hit the DB for a duplicate
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------
    // login(...)
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("login")
    class Login {

        private final LoginRequest request = new LoginRequest("admin", "password");

        @Test
        @DisplayName("returns the user profile when credentials are valid")
        void login_validCredentials_returnsUser() {
            // given: authentication passes and the user exists
            User admin = User.builder().username("admin").roles(List.of("ADMIN", "USER")).build();
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
            // authenticationManager.authenticate(...) returns normally (no stubbing needed -> returns null mock)

            // when
            User result = authService.login(request);

            // then
            assertThat(result).isSameAs(admin);
            verify(authenticationManager)
                    .authenticate(new UsernamePasswordAuthenticationToken("admin", "password"));
        }

        @Test
        @DisplayName("maps bad credentials to 401 UNAUTHORIZED and never loads the user")
        void login_badCredentials_throwsUnauthorized() {
            // given: the AuthenticationManager rejects the credentials
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("bad"));

            // when / then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(UNAUTHORIZED));

            verify(userRepository, never()).findByUsername(any());
        }

        @Test
        @DisplayName("returns 401 if authentication passes but the user vanished from the DB")
        void login_authPassesButUserMissing_throwsUnauthorized() {
            // given: authentication succeeds, yet the user can no longer be found (edge case)
            when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(UNAUTHORIZED));
        }
    }

    // ---------------------------------------------------------------------
    // currentUser(...)
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("currentUser")
    class CurrentUser {

        @Test
        @DisplayName("returns the profile of an existing user")
        void currentUser_existing_returnsUser() {
            User admin = User.builder().username("admin").build();
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

            assertThat(authService.currentUser("admin")).isSameAs(admin);
        }

        @Test
        @DisplayName("throws 401 for an unknown user")
        void currentUser_unknown_throwsUnauthorized() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.currentUser("ghost"))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(UNAUTHORIZED));
        }
    }
}
