package banking;

import java.math.BigDecimal;

public class Account {
    public enum Status { ACTIVE, FROZEN, CLOSED }

    private final String id;
    private final String owner;
    private BigDecimal balance;
    private Status status = Status.ACTIVE;

    Account(String id, String owner, BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    public String getId() { return id; }
    public String getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }
    public Status getStatus() { return status; }

    void setBalance(BigDecimal balance) { this.balance = balance; }
    void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%s | %-20s | %15s | %s", id, owner, balance.toPlainString(), status);
    }
}
