package mn.astvision.ard.repo;

import mn.astvision.ard.data.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void printAllUsernames() {
        List<User> users = userRepository.findAll();

        System.out.println("=== Found " + users.size() + " user(s) ===");
        users.forEach(user -> System.out.println("username: " + user.getUsername()));
    }
}
