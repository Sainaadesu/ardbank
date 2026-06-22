package mn.astvision.ard.repo;

import mn.astvision.ard.data.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
}
