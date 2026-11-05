package banking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final String id;
    private final TransactionType type;
    private final String sourceId;
    private final String targetId; // only used by TRANSFER
    private final BigDecimal amount;
    private final LocalDateTime timestamp;

    Transaction(String id, TransactionType type, String sourceId, String targetId, BigDecimal amount) {
        this.id = id;
        this.type = type;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public TransactionType getType() { return type; }
    public String getSourceId() { return sourceId; }
    public String getTargetId() { return targetId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        String route = type == TransactionType.TRANSFER ? sourceId + " -> " + targetId : sourceId;
        return String.format("%s | %-8s | %s | %s | %s", id, type, route, amount.toPlainString(), timestamp);
    }
}
