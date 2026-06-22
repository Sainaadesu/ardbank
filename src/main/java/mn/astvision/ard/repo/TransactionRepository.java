package mn.astvision.ard.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import mn.astvision.ard.data.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByReference(String reference);

    boolean existsByReference(String reference);



    /** Outgoing transfers of an account, newest first. */
    List<Transaction> findByFromAccountIdOrderByCreatedAtDesc(String fromAccountId);

    /** Incoming transfers of an account, newest first. */
    List<Transaction> findByToAccountIdOrderByCreatedAtDesc(String toAccountId);
}
