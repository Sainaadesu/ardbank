package mn.astvision.ard.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mn.astvision.ard.api.dto.LoginRequest;
import mn.astvision.ard.api.dto.RegisterRequest;
import mn.astvision.ard.api.dto.UserResponse;
import mn.astvision.ard.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final AuthService authService;

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(authService.register(request));
    }

    @PostMapping("login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return UserResponse.from(authService.login(request));
    }

    @GetMapping("me")
    public UserResponse me(@AuthenticationPrincipal UserDetails principal) {
        return UserResponse.from(authService.currentUser(principal.getUsername()));
    }
}
