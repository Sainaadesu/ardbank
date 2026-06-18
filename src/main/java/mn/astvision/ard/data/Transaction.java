package mn.astvision.ard.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mn.astvision.ard.enums.TransactionType;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "transactions")

public class Transaction {    
   @Id
    private String TransactionId;

    private BigDecimal Amount;
    private TransactionType TransactionType;
    private LocalDateTime dateTime;
    private String accountId;

}
