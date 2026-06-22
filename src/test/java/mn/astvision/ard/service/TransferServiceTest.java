package mn.astvision.ard.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.data.Account;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.enums.AccountCategory;
import mn.astvision.ard.enums.TransactionStatus;
import mn.astvision.ard.enums.TransactionType;
import mn.astvision.ard.repo.AccountRepository;
import mn.astvision.ard.repo.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

/**
 * Pure unit tests for {@link TransferService}.
 * <p>
 * No Spring context and no real MongoDB: {@link AccountRepository} and
 * {@link TransactionRepository} are Mockito mocks so we exercise only the
 * money-movement logic in isolation.
 * <p>
 * Each {@code @Nested} block is one scenario family (happy path + the ways a
 * transfer can be rejected) following the {@code given / when / then} rhythm.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private Account account(String id, String number, String balance) {
        return Account.builder()
                .accountId(id)
                .accountNumber(number)
                .userId("user-" + id)
                .currentBalance(new BigDecimal(balance))
                .accountCategory(AccountCategory.Checking)
                .build();
    }

    private TransferRequest request(String from, String to, String amount, String reference) {
        return new TransferRequest(from, to, new BigDecimal(amount),
                TransactionType.internal, "rent", reference);
    }

    // ---------------------------------------------------------------------
    // happy path
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("transfer (happy path)")
    class HappyPath {

        @Test
        @DisplayName("debits the sender, credits the receiver and records a COMPLETED transaction")
        void transfer_validRequest_movesMoneyAndRecordsLedger() {
            // given: sender has 1000, receiver has 200
            Account from = account("a1", "3620001", "1000.00");
            Account to = account("a2", "3620002", "200.00");
            when(accountRepository.findByAccountNumber("3620001")).thenReturn(Optional.of(from));
            when(accountRepository.findByAccountNumber("3620002")).thenReturn(Optional.of(to));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            // when: transfer 300
            Transaction tx = transferService.transfer(request("3620001", "3620002", "300.00", null));

            // then: balances moved by exactly 300
            assertThat(from.getCurrentBalance()).isEqualByComparingTo("700.00");
            assertThat(to.getCurrentBalance()).isEqualByComparingTo("500.00");
            verify(accountRepository).save(from);
            verify(accountRepository).save(to);

            // and: the ledger record is fully populated and COMPLETED
            assertThat(tx.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(tx.getAmount()).isEqualByComparingTo("300.00");
            assertThat(tx.getFromAccountId()).isEqualTo("a1");
            assertThat(tx.getToAccountId()).isEqualTo("a2");
            assertThat(tx.getSenderBalanceAfter()).isEqualByComparingTo("700.00");
            assertThat(tx.getReceiverBalanceAfter()).isEqualByComparingTo("500.00");
            assertThat(tx.getReference()).isNotBlank();
            assertThat(tx.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("uses the caller-supplied reference as the transaction reference")
        void transfer_withReference_keepsReference() {
            // given
            when(accountRepository.findByAccountNumber("3620001"))
                    .thenReturn(Optional.of(account("a1", "3620001", "1000")));
            when(accountRepository.findByAccountNumber("3620002"))
                    .thenReturn(Optional.of(account("a2", "3620002", "0")));
            when(transactionRepository.findByReference("ref-123")).thenReturn(Optional.empty());
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            Transaction tx = transferService.transfer(request("3620001", "3620002", "50", "ref-123"));

            // then
            assertThat(tx.getReference()).isEqualTo("ref-123");
        }
    }

    // ---------------------------------------------------------------------
    // idempotency
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("transfer (idempotency)")
    class Idempotency {

        @Test
        @DisplayName("returns the existing transaction and moves no money when the reference was already processed")
        void transfer_duplicateReference_isNoOp() {
            // given: a transfer with this reference already exists
            Transaction original = Transaction.builder()
                    .transactionId("tx-1").reference("ref-dup").status(TransactionStatus.COMPLETED).build();
            when(transactionRepository.findByReference("ref-dup")).thenReturn(Optional.of(original));

            // when
            Transaction tx = transferService.transfer(request("3620001", "3620002", "100", "ref-dup"));

            // then: same record back, and no balances were touched or re-saved
            assertThat(tx).isSameAs(original);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(accountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------
    // rejections
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("transfer (rejections)")
    class Rejections {

        @Test
        @DisplayName("rejects a non-positive amount with 422 and moves no money")
        void transfer_nonPositiveAmount_throwsUnprocessable() {
            assertThatThrownBy(() -> transferService.transfer(request("3620001", "3620002", "0", null)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(UNPROCESSABLE_ENTITY));

            verify(accountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects a transfer to the same account with 400")
        void transfer_sameAccount_throwsBadRequest() {
            assertThatThrownBy(() -> transferService.transfer(request("3620001", "3620001", "10", null)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(BAD_REQUEST));

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects an unknown source account with 404")
        void transfer_unknownSource_throwsNotFound() {
            when(accountRepository.findByAccountNumber("3620001")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.transfer(request("3620001", "3620002", "10", null)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(NOT_FOUND));

            verify(accountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects a transfer larger than the balance with 422 and leaves balances untouched")
        void transfer_insufficientFunds_throwsUnprocessable() {
            // given: sender only has 50
            Account from = account("a1", "3620001", "50.00");
            Account to = account("a2", "3620002", "0.00");
            when(accountRepository.findByAccountNumber("3620001")).thenReturn(Optional.of(from));
            when(accountRepository.findByAccountNumber("3620002")).thenReturn(Optional.of(to));

            // when / then: transferring 100 fails
            assertThatThrownBy(() -> transferService.transfer(request("3620001", "3620002", "100.00", null)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(UNPROCESSABLE_ENTITY));

            // and: nothing was debited, credited or recorded
            assertThat(from.getCurrentBalance()).isEqualByComparingTo("50.00");
            assertThat(to.getCurrentBalance()).isEqualByComparingTo("0.00");
            verify(accountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------
    // ledger snapshot
    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("transfer (ledger record)")
    class LedgerRecord {

        @Test
        @DisplayName("persists exactly one transaction carrying both legs of the movement")
        void transfer_persistsSingleDoubleSidedRecord() {
            // given
            when(accountRepository.findByAccountNumber("3620001"))
                    .thenReturn(Optional.of(account("a1", "3620001", "500")));
            when(accountRepository.findByAccountNumber("3620002"))
                    .thenReturn(Optional.of(account("a2", "3620002", "0")));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            transferService.transfer(request("3620001", "3620002", "120", null));

            // then: one save, capturing both sender and receiver sides
            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            Transaction persisted = captor.getValue();

            assertThat(persisted.getFromAccountNumber()).isEqualTo("3620001");
            assertThat(persisted.getToAccountNumber()).isEqualTo("3620002");
            assertThat(persisted.getType()).isEqualTo(TransactionType.internal);
            assertThat(persisted.getFee()).isEqualByComparingTo("0");
            assertThat(persisted.getCurrency()).isEqualTo("MNT");
            assertThat(persisted.getCreatedAt()).isNotNull();
        }
    }
}
