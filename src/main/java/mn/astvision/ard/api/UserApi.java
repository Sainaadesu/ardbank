package mn.astvision.ard.api;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.data.User;
import mn.astvision.ard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserApi {

    @Autowired
    private UserService userService;

    @GetMapping("all")
    public Page<User> getAllUser(Pageable pageable) {
        return userService.getAllByPage(pageable);
    }
}
