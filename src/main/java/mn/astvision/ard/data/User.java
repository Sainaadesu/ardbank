package mn.astvision.ard.data;


import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String lastName;
    private String firstName;

    @Indexed(unique = true)
    private String username;

    @JsonIgnore
    private String password;

    private List<String> roles;
}
