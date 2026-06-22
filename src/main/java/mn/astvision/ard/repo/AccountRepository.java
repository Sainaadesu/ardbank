package mn.astvision.ard.repo;

import mn.astvision.ard.data.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import mn.astvision.ard.data.Account;

import java.util.Optional;

public interface AccountRepository extends MongoRepository <Account, String>{
        boolean existsByAccountNumber(String account);
        Optional<Account> findByAccountNumber(String s);
        boolean existsByUserId(String userId);
        Optional<Account> findByUserId(String userId);

}
