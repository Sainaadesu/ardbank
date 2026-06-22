package mn.astvision.ard.enums;

/**
 * Lifecycle of a money transfer.
 *
 * <pre>
 *   PENDING ──► COMPLETED
 *      │
 *      └──────► FAILED
 *   COMPLETED ─► REVERSED   (compensating entry after a completed transfer)
 * </pre>
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED
}
