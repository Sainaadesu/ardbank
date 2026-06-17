package mn.astvision.ard.service;

import mn.astvision.ard.data.User;
import mn.astvision.ard.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<User> getAllByPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
