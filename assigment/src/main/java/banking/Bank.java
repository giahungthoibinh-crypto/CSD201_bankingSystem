package banking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Core service: account management, transactions and account search.
 *
 * Data structures:
 *  - HashMap<id, Account>            : O(1) lookup by account id
 *  - TreeMap<ownerKey, Set<id>>      : sorted owner index, O(log n + k) prefix search
 *  - HashMap<id, List<Transaction>>  : per-account history (append-only)
 *  - ArrayList<Transaction>          : global ledger in chronological order
 */
public class Bank {
    private final Map<String, Account> accounts = new HashMap<>();
    private final NavigableMap<String, Set<String>> ownerIndex = new TreeMap<>();
    private final Map<String, List<Transaction>> history = new HashMap<>();
    private final List<Transaction> ledger = new ArrayList<>();
    private int accountSeq = 0;
    private int txSeq = 0;

    // ---------- account management ----------

    public Account openAccount(String owner, BigDecimal initialDeposit) {
        if (owner == null || owner.isBlank()) {
            throw new BankException("Owner name must not be blank");
        }
        if (initialDeposit == null || initialDeposit.signum() < 0) {
            throw new BankException("Initial deposit must not be negative");
        }
        String id = String.format("ACC%04d", ++accountSeq);
        Account account = new Account(id, owner.trim(), initialDeposit);
        accounts.put(id, account);
        ownerIndex.computeIfAbsent(key(owner), k -> new LinkedHashSet<>()).add(id);
        history.put(id, new ArrayList<>());
        if (initialDeposit.signum() > 0) {
            record(TransactionType.DEPOSIT, id, null, initialDeposit);
        }
        return account;
    }

    public Account getAccount(String id) {
        Account account = id == null ? null : accounts.get(id);
        if (account == null) {
            throw new BankException("Account not found: " + id);
        }
        return account;
    }

    public void freezeAccount(String id) {
        Account a = getAccount(id);
        if (a.getStatus() == Account.Status.CLOSED) {
            throw new BankException("Account is closed: " + id);
        }
        a.setStatus(Account.Status.FROZEN);
    }

    public void unfreezeAccount(String id) {
        Account a = getAccount(id);
        if (a.getStatus() != Account.Status.FROZEN) {
            throw new BankException("Account is not frozen: " + id);
        }
        a.setStatus(Account.Status.ACTIVE);
    }

    public void closeAccount(String id) {
        Account a = getAccount(id);
        if (a.getStatus() == Account.Status.CLOSED) {
            throw new BankException("Account already closed: " + id);
        }
        if (a.getBalance().signum() != 0) {
            throw new BankException("Balance must be zero to close account: " + id);
        }
        a.setStatus(Account.Status.CLOSED);
    }

    public int accountCount() {
        return accounts.size();
    }

    // ---------- transactions ----------

    public Transaction deposit(String id, BigDecimal amount) {
        requirePositive(amount);
        Account a = requireActive(id);
        a.setBalance(a.getBalance().add(amount));
        return record(TransactionType.DEPOSIT, id, null, amount);
    }

    public Transaction withdraw(String id, BigDecimal amount) {
        requirePositive(amount);
        Account a = requireActive(id);
        if (a.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient funds in " + id);
        }
        a.setBalance(a.getBalance().subtract(amount));
        return record(TransactionType.WITHDRAW, id, null, amount);
    }

    public Transaction transfer(String fromId, String toId, BigDecimal amount) {
        requirePositive(amount);
        if (fromId != null && fromId.equals(toId)) {
            throw new BankException("Cannot transfer to the same account");
        }
        Account from = requireActive(fromId);
        Account to = requireActive(toId);
        if (from.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient funds in " + fromId);
        }
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        return record(TransactionType.TRANSFER, fromId, toId, amount);
    }

    public List<Transaction> getHistory(String id) {
        getAccount(id);
        return Collections.unmodifiableList(history.get(id));
    }

    public List<Transaction> getLedger() {
        return Collections.unmodifiableList(ledger);
    }

    // ---------- search ----------

    /** Exact lookup by id - O(1). Returns null when not found. */
    public Account findById(String id) {
        return id == null ? null : accounts.get(id);
    }

    /** Case-insensitive owner-name prefix search - O(log n + k). */
    public List<Account> searchByOwnerPrefix(String prefix) {
        List<Account> result = new ArrayList<>();
        if (prefix == null || prefix.isBlank()) {
            return result;
        }
        String p = key(prefix);
        for (Map.Entry<String, Set<String>> e : ownerIndex.tailMap(p, true).entrySet()) {
            if (!e.getKey().startsWith(p)) {
                break;
            }
            for (String id : e.getValue()) {
                result.add(accounts.get(id));
            }
        }
        return result;
    }

    /** Case-insensitive owner-name substring search - O(n). */
    public List<Account> searchByOwnerContains(String text) {
        List<Account> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        String t = key(text);
        for (Map.Entry<String, Set<String>> e : ownerIndex.entrySet()) {
            if (e.getKey().contains(t)) {
                for (String id : e.getValue()) {
                    result.add(accounts.get(id));
                }
            }
        }
        return result;
    }

    /** Inclusive balance range search - O(n). */
    public List<Account> searchByBalanceRange(BigDecimal min, BigDecimal max) {
        if (min == null || max == null || min.compareTo(max) > 0) {
            throw new BankException("Invalid balance range");
        }
        List<Account> result = new ArrayList<>();
        for (Account a : accounts.values()) {
            if (a.getBalance().compareTo(min) >= 0 && a.getBalance().compareTo(max) <= 0) {
                result.add(a);
            }
        }
        result.sort((x, y) -> x.getId().compareTo(y.getId()));
        return result;
    }

    // ---------- helpers ----------

    private static String key(String owner) {
        return owner.trim().toLowerCase();
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BankException("Amount must be greater than zero");
        }
    }

    private Account requireActive(String id) {
        Account a = getAccount(id);
        if (a.getStatus() != Account.Status.ACTIVE) {
            throw new BankException("Account is " + a.getStatus() + ": " + id);
        }
        return a;
    }

    private Transaction record(TransactionType type, String source, String target, BigDecimal amount) {
        Transaction tx = new Transaction(String.format("TX%05d", ++txSeq), type, source, target, amount);
        ledger.add(tx);
        history.get(source).add(tx);
        if (target != null) {
            history.get(target).add(tx);
        }
        return tx;
    }
}
