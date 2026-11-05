package bankds;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Giao dịch; đồng thời là node của Stack (next). */
public class Transaction {
    public enum Type { DEPOSIT, WITHDRAW }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final long transactionId;
    final Type type;
    final long amount;
    final LocalDateTime timestamp;
    Transaction next;

    Transaction(long transactionId, Type type, long amount) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("#%d | %s | %-8s | %,d", transactionId, timestamp.format(FMT),
                type == Type.DEPOSIT ? "Nap" : "Rut", amount);
    }
}
