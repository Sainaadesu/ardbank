package mn.astvision.ard.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import mn.astvision.ard.enums.TransactionType;

/**
 * Request to move money from one account to another.
 *
 * @param fromAccountNumber source account number (debited)
 * @param toAccountNumber   destination account number (credited)
 * @param amount            positive amount to transfer
 * @param type              {@code internal} or {@code interBank}; defaults to internal when null
 * @param description       free-text note shown on the statement
 * @param reference         optional idempotency key; a repeat with the same value is a no-op
 */
@Builder
public record TransferRequest(
        @NotBlank String fromAccountNumber,
        @NotBlank String toAccountNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        TransactionType type,
        String description,
        String reference) {
}
