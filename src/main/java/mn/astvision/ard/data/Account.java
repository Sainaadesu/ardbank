package mn.astvision.ard.data;

import java.math.BigDecimal;
//import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import mn.astvision.ard.enums.AccountCategory;

//import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "accounts")

public class Account {
    @Id
    private String accountId;

    @Indexed(unique = true)
    private String accountNumber;
    private String id;

    private BigDecimal currentBalance;
    private AccountCategory accountCategory;
    

    
}
