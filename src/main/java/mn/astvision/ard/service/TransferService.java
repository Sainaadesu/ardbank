package mn.astvision.ard.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.data.Account;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.enums.TransactionStatus;
import mn.astvision.ard.enums.TransactionType;
import mn.astvision.ard.repo.AccountRepository;
import mn.astvision.ard.repo.TransactionRepository;

/**
 * Moves money between accounts and records an auditable {@link Transaction} for
 * every movement.
 * <p>
 * A transfer is a debit on the sender plus a credit on the receiver. Both legs
 * are applied here together; either the whole transfer succeeds and is recorded
 * as {@code COMPLETED}, or it is rejected before any balance is touched.
 *
 * <p><b>Note on atomicity:</b> for true crash-safety the two balance writes
 * should run inside a MongoDB multi-document transaction (replica set). This
 * service centralises the logic so that guarantee can be layered on with a
 * single {@code @Transactional} without touching callers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    /** Default currency when an account does not pin one down. */
    private static final String DEFAULT_CURRENCY = "MNT";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Transfers {@link TransferRequest#amount()} from the source account to the
     * destination account.
     *
     * @return the recorded, {@code COMPLETED} transaction
     * @throws ResponseStatusException 422 on a non-positive amount or insufficient funds,
     *                                 404 when an account does not exist,
     *                                 400 when source and destination are the same account
     */
    public Transaction transfer(TransferRequest request) {
        BigDecimal amount = normalizeAmount(request.amount());

        // Idempotency: replay of the same reference returns the original record.
        if (request.reference() != null && !request.reference().isBlank()) {
            var existing = transactionRepository.findByReference(request.reference());
            if (existing.isPresent()) {
                log.info("Transfer with reference {} already processed; returning existing", request.reference());
                return existing.get();
            }
        }

        if (request.fromAccountNumber().equals(request.toAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and destination accounts must differ");
        }

        Account from = loadAccount(request.fromAccountNumber());
        Account to = loadAccount(request.toAccountNumber());

        if (balanceOf(from).compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Insufficient funds on account " + from.getAccountNumber());
        }

        // Apply both legs.

        from.setCurrentBalance(balanceOf(from).subtract(amount));
        to.setCurrentBalance(balanceOf(to).add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        LocalDateTime now = LocalDateTime.now();
        Transaction tx = Transaction.builder()
                .reference(resolveReference(request.reference()))
                .type(request.type() != null ? request.type() : TransactionType.internal)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .currency(DEFAULT_CURRENCY)
                .fromAccountId(from.getAccountId())
                .fromAccountNumber(from.getAccountNumber())
                .fromUserId(from.getUserId())
                .senderBalanceAfter(from.getCurrentBalance())
                .toAccountId(to.getAccountId())
                .toAccountNumber(to.getAccountNumber())
                .toUserId(to.getUserId())
                .receiverBalanceAfter(to.getCurrentBalance())
                .description(request.description())
                .createdAt(now)
                .completedAt(now)
                .build();

        Transaction saved = transactionRepository.save(tx);
        log.info("Transfer {} of {} {} from {} to {} completed", saved.getTransactionId(),
                amount, DEFAULT_CURRENCY, from.getAccountNumber(), to.getAccountNumber());
        return saved;
    }

    /** Full transfer history (incoming + outgoing) for an account, newest first. */
    public List<Transaction> history(String accountId) {
        log.info("account id нь :{}",accountId);
        var out = transactionRepository.findByFromAccountIdOrderByCreatedAtDesc(accountId);
        var in = transactionRepository.findByToAccountIdOrderByCreatedAtDesc(accountId);
        log.info("out болон in нь : {}{}",out,in);
        return java.util.stream.Stream.concat(out.stream(), in.stream())
                .sorted(java.util.Comparator.comparing(Transaction::getCreatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .toList();
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Transfer amount must be positive");
        }
        return amount;
    }

    private Account loadAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found: " + accountNumber));
    }

    private BigDecimal balanceOf(Account account) {
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }

    private String resolveReference(String reference) {
        return (reference != null && !reference.isBlank()) ? reference : UUID.randomUUID().toString();
    }
}
