package mn.astvision.ard.service;

import lombok.RequiredArgsConstructor;
import mn.astvision.ard.api.dto.LoginRequest;
import mn.astvision.ard.api.dto.RegisterRequest;
import mn.astvision.ard.data.User;
import mn.astvision.ard.repo.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.util.List;
import java.util.Random;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user with the default {@code USER} role.
     *
     * @throws ResponseStatusException 409 if the username is already taken
     */
    public User register(RegisterRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(existing -> {
            throw new ResponseStatusException(CONFLICT, "Username already taken: " + request.username());
        });


        User user = User.builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .roles(List.of("USER"))
                .build();

        return userRepository.save(user);
    }

    /**
     * Validates the given credentials against the configured AuthenticationManager.
     *
     * @return the authenticated user
     * @throws ResponseStatusException 401 if the credentials are invalid
     */
    public User login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        }
        catch (BadCredentialsException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password");
        }
        catch (AuthenticationException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, ex.getMessage());
        }
        return userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid username or password"));
    }

    /**
     * Loads the profile of an already-authenticated user (e.g. the HTTP Basic principal).
     */
    public User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unknown user: " + username));
    }
}
