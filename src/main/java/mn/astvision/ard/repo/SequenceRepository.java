package mn.astvision.ard.repo;

import mn.astvision.ard.data.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SequenceRepository extends MongoRepository<Sequence, String> {
}
