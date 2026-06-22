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
import mn.astvision.ard.enums.TransactionStatus;
import mn.astvision.ard.enums.TransactionType;

/**
 * A single money-transfer record (one ledger document per transfer).
 * <p>
 * The document captures BOTH legs of the movement (sender + receiver) plus the
 * snapshot balances after the transfer, so a transaction is fully auditable on
 * its own without having to replay the whole ledger.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String transactionId;

    /**
     * Business / idempotency key. Two transfer requests carrying the same
     * reference resolve to the same transaction instead of moving money twice.
     */
    @Indexed(unique = true)
    private String reference;

    private TransactionType type;
    private TransactionStatus status;

    private BigDecimal amount;
    /** Fee charged to the sender on top of {@link #amount} (0 when free). */
    private BigDecimal fee;
    /** ISO-4217 currency code, e.g. {@code MNT}. */
    private String currency;

    // --- sender (debit) leg ---------------------------------------------
    private String fromAccountId;
    private String fromAccountNumber;
    private String fromUserId;
    /** Sender balance immediately AFTER this transfer (audit snapshot). */
    private BigDecimal senderBalanceAfter;

    // --- receiver (credit) leg ------------------------------------------
    private String toAccountId;
    private String toAccountNumber;
    private String toUserId;
    /** Receiver balance immediately AFTER this transfer (audit snapshot). */
    private BigDecimal receiverBalanceAfter;

    private String description;
    /** Populated only when {@link #status} is {@code FAILED}. */
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
