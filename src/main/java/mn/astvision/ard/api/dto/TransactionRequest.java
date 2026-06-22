package mn.astvision.ard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mn.astvision.ard.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
    @NotBlank @Size(min = 1000) BigDecimal Amount,
    @NotBlank TransactionType TransactionType,
    @NotBlank String fromAccount,
    @NotBlank String toAccount
){}