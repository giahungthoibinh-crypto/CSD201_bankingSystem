package bankds;

import java.util.Random;

/** Ghép Linked List + BST + Stack thành hệ thống ngân hàng. */
public class BankingManager {
    private final AccountList accounts = new AccountList();
    private final AccountBST index = new AccountBST();
    private final Random random = new Random();
    private long nextTransactionId = 1;

    /** ID ngẫu nhiên duy nhất (6 chữ số) để BST không bị lệch như khi dùng ID tăng dần. */
    private int generateId() {
        int id;
        do { id = 100000 + random.nextInt(900000); } while (index.search(id) != null);
        return id;
    }

    public Account createAccount(String name, long initialBalance) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Ten khong duoc rong");
        if (initialBalance < 0) throw new IllegalArgumentException("So du ban dau khong duoc am");
        if (accounts.size() >= 900000) throw new IllegalStateException("Het ma tai khoan");
        Account a = new Account(generateId(), name.trim(), initialBalance);
        accounts.insert(a);
        index.insert(a.accountId, a);
        return a;
    }

    public Account find(int accountId) { return index.search(accountId); }

    private Account require(int accountId) {
        Account a = index.search(accountId);
        if (a == null) throw new IllegalArgumentException("Tai khoan khong ton tai: " + accountId);
        return a;
    }

    private static void checkAmount(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("So tien phai > 0");
    }

    public void deposit(int accountId, long amount) {
        checkAmount(amount);
        Account a = require(accountId);
        if (a.balance > Long.MAX_VALUE - amount) throw new IllegalArgumentException("So tien qua lon");
        a.balance += amount;
        a.history.push(new Transaction(nextTransactionId++, Transaction.Type.DEPOSIT, amount));
    }

    public void withdraw(int accountId, long amount) {
        checkAmount(amount);
        Account a = require(accountId);
        if (a.balance < amount) throw new IllegalArgumentException("So du khong du (hien co " + a.balance + ")");
        a.balance -= amount;
        a.history.push(new Transaction(nextTransactionId++, Transaction.Type.WITHDRAW, amount));
    }

    /** Hoàn tác giao dịch gần nhất (Pop). Trả về giao dịch đã hoàn tác. */
    public Transaction undoLast(int accountId) {
        Account a = require(accountId);
        Transaction t = a.history.peek();
        if (t == null) throw new IllegalArgumentException("Khong co giao dich de hoan tac");
        if (t.type == Transaction.Type.DEPOSIT && a.balance < t.amount)
            throw new IllegalArgumentException("Khong the hoan tac: so du khong du");
        a.history.pop();
        a.balance += t.type == Transaction.Type.DEPOSIT ? -t.amount : t.amount;
        return t;
    }

    public void viewHistory(int accountId) {
        Account a = require(accountId);
        System.out.printf("Tai khoan %d - %s | So du: %,d%n", a.accountId, a.name, a.balance);
        a.history.printHistory();
    }

    public int count() { return accounts.size(); }

    public AccountList list() { return accounts; }
}
