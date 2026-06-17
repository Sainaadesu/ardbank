package mn.astvision.ard.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthApi {

    @GetMapping("me")
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails user) {
        return Map.of(
                "username", user.getUsername(),
                "authorities", user.getAuthorities().stream()
                        .map(Object::toString)
                        .toList()
        );
    }
}
