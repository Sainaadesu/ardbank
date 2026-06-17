package mn.astvision.ard.api.dto;

import mn.astvision.ard.data.User;

import java.util.List;

public record UserResponse(
        String id,
        String username,
        String firstName,
        String lastName,
        List<String> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles()
        );
    }
}
