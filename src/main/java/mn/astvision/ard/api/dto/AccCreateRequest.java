package mn.astvision.ard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mn.astvision.ard.enums.AccountCategory;

import java.math.BigDecimal;

public record AccCreateRequest(
        @NotBlank String userId,
        @NotBlank @Size(min=5000) BigDecimal currentBalance,
        @NotBlank AccountCategory accountCategory
) {
}
