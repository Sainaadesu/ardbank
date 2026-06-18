package mn.astvision.ard.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.data.User;
import mn.astvision.ard.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the {@code users} collection with mock accounts on startup when it is empty.
 * Default password for every seeded user is {@code password}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already present ({}), skipping mock data seeding", userRepository.count());
            return;
        }

        List<User> users = List.of(
                mockUser("admin", "System", "Admin",List.of("ADMIN", "USER")),
                mockUser("bbold", "Bold", "Batbayar",List.of("USER")),
                mockUser("tsetseg", "Tsetseg", "Munkh",List.of("USER")),
                mockUser("dorj", "Dorj", "Ganbat", List.of("USER")),
                mockUser("oyuna", "Oyun", "Erdene", List.of("USER"))
        );





        userRepository.saveAll(users);
        log.info("Seeded {} mock users (default password: 'password')", users.size());
    }

    private User mockUser(String username, String firstName, String lastName,List<String> roles) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode("password"))
                .roles(roles)
                .build();
    }
}
