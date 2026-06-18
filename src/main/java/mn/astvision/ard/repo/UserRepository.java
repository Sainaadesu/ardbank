package mn.astvision.ard.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import mn.astvision.ard.data.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
