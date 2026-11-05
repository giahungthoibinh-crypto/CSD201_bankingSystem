package banking;

import java.math.BigDecimal;
import java.util.List;

/** Dependency-free test runner (no Maven/JUnit needed). Run: build.ps1 test */
public class BankTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("open account assigns sequential ids", BankTest::openAccount);
        run("open account rejects invalid input", BankTest::openAccountInvalid);
        run("deposit/withdraw happy path", BankTest::depositWithdraw);
        run("amount boundaries", BankTest::amountBoundaries);
        run("transfer moves money and is recorded for both accounts", BankTest::transferOk);
        run("combinatorial transfer (source status x target status x funds)", BankTest::transferCombinatorial);
        run("close/freeze lifecycle", BankTest::lifecycle);
        run("search by id", BankTest::searchById);
        run("search by owner prefix (case-insensitive, duplicates)", BankTest::searchPrefix);
        run("search by owner contains", BankTest::searchContains);
        run("search by balance range (inclusive bounds)", BankTest::searchRange);
        System.out.printf("%nPassed: %d, Failed: %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- tests ----------

    static void openAccount() {
        Bank b = new Bank();
        Account a1 = b.openAccount("Alice", bd("100"));
        Account a2 = b.openAccount("Bob", bd("0"));
        eq("ACC0001", a1.getId());
        eq("ACC0002", a2.getId());
        eq(1, b.getHistory("ACC0001").size());
        eq(0, b.getHistory("ACC0002").size()); // zero deposit is not a transaction
    }

    static void openAccountInvalid() {
        Bank b = new Bank();
        throwsBank(() -> b.openAccount(null, bd("1")));
        throwsBank(() -> b.openAccount("  ", bd("1")));
        throwsBank(() -> b.openAccount("Al", bd("-0.01")));
        throwsBank(() -> b.openAccount("Al", null));
        eq(0, b.accountCount());
    }

    static void depositWithdraw() {
        Bank b = new Bank();
        b.openAccount("Alice", bd("100"));
        b.deposit("ACC0001", bd("50.25"));
        b.withdraw("ACC0001", bd("20.25"));
        eq(bd("130.00"), b.getAccount("ACC0001").getBalance());
        eq(3, b.getHistory("ACC0001").size());
    }

    static void amountBoundaries() {
        Bank b = new Bank();
        b.openAccount("Alice", bd("100"));
        throwsBank(() -> b.deposit("ACC0001", bd("0")));
        throwsBank(() -> b.deposit("ACC0001", bd("-1")));
        throwsBank(() -> b.withdraw("ACC0001", null));
        throwsBank(() -> b.withdraw("ACC0001", bd("100.01")));
        b.withdraw("ACC0001", bd("100")); // exactly the balance is allowed
        eq(0, b.getAccount("ACC0001").getBalance().signum());
        b.deposit("ACC0001", bd("0.01")); // smallest unit
        throwsBank(() -> b.deposit("ACC9999", bd("1")));
    }

    static void transferOk() {
        Bank b = new Bank();
        b.openAccount("Alice", bd("100"));
        b.openAccount("Bob", bd("10"));
        b.transfer("ACC0001", "ACC0002", bd("40"));
        eq(bd("60"), b.getAccount("ACC0001").getBalance());
        eq(bd("50"), b.getAccount("ACC0002").getBalance());
        eq(TransactionType.TRANSFER, b.getHistory("ACC0002").get(1).getType());
        eq(b.getHistory("ACC0001").get(1).getId(), b.getHistory("ACC0002").get(1).getId());
        throwsBank(() -> b.transfer("ACC0001", "ACC0001", bd("1")));
    }

    /** Pairwise-complete (here: full) combination of the transfer parameters. */
    static void transferCombinatorial() {
        Account.Status[] states = Account.Status.values();
        BigDecimal[] amounts = { bd("50"), bd("100"), bd("100.01") }; // below / equal / above balance
        for (Account.Status src : states) {
            for (Account.Status dst : states) {
                for (BigDecimal amount : amounts) {
                    Bank b = new Bank();
                    b.openAccount("Src", bd("100"));
                    b.openAccount("Dst", bd("0"));
                    prepare(b, "ACC0001", src);
                    prepare(b, "ACC0002", dst);
                    boolean expectOk = src == Account.Status.ACTIVE && dst == Account.Status.ACTIVE
                            && amount.compareTo(bd("100")) <= 0;
                    String label = src + "/" + dst + "/" + amount;
                    BigDecimal srcBefore = b.getAccount("ACC0001").getBalance();
                    BigDecimal dstBefore = b.getAccount("ACC0002").getBalance();
                    try {
                        b.transfer("ACC0001", "ACC0002", amount);
                        check(expectOk, "unexpected success for " + label);
                    } catch (BankException e) {
                        check(!expectOk, "unexpected failure for " + label + ": " + e.getMessage());
                        // failed transfers must leave balances untouched
                        eq(srcBefore, b.getAccount("ACC0001").getBalance());
                        eq(dstBefore, b.getAccount("ACC0002").getBalance());
                    }
                }
            }
        }
    }

    static void lifecycle() {
        Bank b = new Bank();
        b.openAccount("Alice", bd("10"));
        throwsBank(() -> b.closeAccount("ACC0001")); // non-zero balance
        b.freezeAccount("ACC0001");
        throwsBank(() -> b.deposit("ACC0001", bd("1")));
        throwsBank(() -> b.unfreezeAccount("ACC0002"));
        b.unfreezeAccount("ACC0001");
        b.withdraw("ACC0001", bd("10"));
        b.closeAccount("ACC0001");
        eq(Account.Status.CLOSED, b.getAccount("ACC0001").getStatus());
        throwsBank(() -> b.closeAccount("ACC0001"));
        throwsBank(() -> b.freezeAccount("ACC0001"));
        throwsBank(() -> b.deposit("ACC0001", bd("1")));
    }

    static void searchById() {
        Bank b = new Bank();
        b.openAccount("Alice", bd("1"));
        check(b.findById("ACC0001") != null, "should find ACC0001");
        check(b.findById("ACC0002") == null, "should not find ACC0002");
        check(b.findById(null) == null, "null id returns null");
    }

    static void searchPrefix() {
        Bank b = seeded();
        eq(2, b.searchByOwnerPrefix("al").size()); // Alice, ALAN
        eq(1, b.searchByOwnerPrefix("  bo ").size());
        eq(0, b.searchByOwnerPrefix("zed").size());
        eq(0, b.searchByOwnerPrefix("").size());
        eq(0, b.searchByOwnerPrefix(null).size());
        b.openAccount("alice", bd("5")); // same owner, second account
        eq(2, b.searchByOwnerPrefix("alice").size());
    }

    static void searchContains() {
        Bank b = seeded();
        eq(2, b.searchByOwnerContains("LA").size()); // ALAN, Clara
        eq(0, b.searchByOwnerContains("xyz").size());
        eq(0, b.searchByOwnerContains(" ").size());
    }

    static void searchRange() {
        Bank b = seeded(); // balances: 100, 250, 250, 1000
        eq(2, b.searchByBalanceRange(bd("250"), bd("250")).size());
        eq(3, b.searchByBalanceRange(bd("100"), bd("250")).size());
        eq(0, b.searchByBalanceRange(bd("251"), bd("999")).size());
        throwsBank(() -> b.searchByBalanceRange(bd("2"), bd("1")));
        throwsBank(() -> b.searchByBalanceRange(null, bd("1")));
    }

    // ---------- helpers ----------

    private static Bank seeded() {
        Bank b = new Bank();
        b.openAccount("Alice", bd("100"));
        b.openAccount("ALAN", bd("250"));
        b.openAccount("Bob", bd("250"));
        b.openAccount("Clara", bd("1000"));
        return b;
    }

    private static void prepare(Bank b, String id, Account.Status status) {
        switch (status) {
            case FROZEN -> b.freezeAccount(id);
            case CLOSED -> {
                BigDecimal bal = b.getAccount(id).getBalance();
                if (bal.signum() > 0) {
                    b.withdraw(id, bal); // closing requires a zero balance
                }
                b.closeAccount(id);
            }
            default -> { }
        }
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("FAIL  " + name + " -> " + t);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void eq(Object expected, Object actual) {
        boolean same = expected instanceof BigDecimal e && actual instanceof BigDecimal a
                ? e.compareTo(a) == 0
                : expected.equals(actual);
        if (!same) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void throwsBank(Runnable r) {
        try {
            r.run();
        } catch (BankException e) {
            return;
        }
        throw new AssertionError("expected BankException");
    }
}
