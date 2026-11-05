package bankds;

/** Tài khoản; đồng thời là node của Linked List (next). */
public class Account {
    final int accountId;
    final String name;
    long balance;
    final TransactionStack history = new TransactionStack();
    Account next;

    Account(int accountId, String name, long balance) {
        this.accountId = accountId;
        this.name = name;
        this.balance = balance;
    }

    public int getAccountId() { return accountId; }
    public String getName() { return name; }
    public long getBalance() { return balance; }
    public TransactionStack getHistory() { return history; }
}
