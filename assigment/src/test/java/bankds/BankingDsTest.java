package bankds;

import java.util.Random;

public class BankingDsTest {
    static int failed = 0;

    static void check(boolean cond, String name) {
        System.out.println((cond ? "PASS " : "FAIL ") + name);
        if (!cond) failed++;
    }

    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) {
        stackTest();
        listTest();
        bstTest();
        normalFlow();
        errorFlow();
        performance();
        System.out.println(failed == 0 ? "ALL TESTS PASSED" : failed + " TEST(S) FAILED");
        System.exit(failed == 0 ? 0 : 1);
    }

    static void stackTest() {
        TransactionStack s = new TransactionStack();
        check(s.pop() == null && s.isEmpty(), "stack: pop on empty -> null");
        s.push(new Transaction(1, Transaction.Type.DEPOSIT, 10));
        s.push(new Transaction(2, Transaction.Type.WITHDRAW, 5));
        check(s.size() == 2 && s.peek().transactionId == 2, "stack: LIFO top");
        check(s.pop().transactionId == 2 && s.pop().transactionId == 1 && s.isEmpty(), "stack: pop order");
    }

    static void listTest() {
        AccountList l = new AccountList();
        for (int i = 1; i <= 4; i++) l.insert(new Account(i, "n" + i, 0));
        check(l.delete(1) && l.head().accountId == 2, "list: delete head");
        check(l.delete(3) && l.linearSearch(3) == null && l.linearSearch(4) != null, "list: delete middle");
        check(l.delete(4), "list: delete tail");
        l.insert(new Account(9, "x", 0));
        check(l.linearSearch(9) != null && l.size() == 2, "list: insert after deleting tail keeps chain");
        check(!l.delete(100), "list: delete missing -> false");
    }

    static void bstTest() {
        AccountBST t = new AccountBST();
        Account a = new Account(50, "a", 0), b = new Account(30, "b", 0), c = new Account(70, "c", 0);
        t.insert(50, a); t.insert(30, b); t.insert(70, c);
        check(t.search(30) == b && t.search(70) == c && t.search(50) == a, "bst: search found");
        check(t.search(99) == null, "bst: search missing -> null");
        check(!t.insert(50, a) && t.size() == 3, "bst: duplicate rejected");
    }

    static void normalFlow() {
        BankingManager bank = new BankingManager();
        Account a = bank.createAccount("A", 0);
        bank.deposit(a.getAccountId(), 500);
        bank.withdraw(a.getAccountId(), 200);
        check(a.getBalance() == 300, "TC1: balance 300");
        check(a.getHistory().peek().type == Transaction.Type.WITHDRAW
                && a.getHistory().peek().next.type == Transaction.Type.DEPOSIT, "TC1: history newest first");
        bank.undoLast(a.getAccountId());
        check(a.getBalance() == 500 && a.getHistory().size() == 1, "undo restores balance");
        bank.viewHistory(a.getAccountId());
    }

    static void errorFlow() {
        BankingManager bank = new BankingManager();
        Account a = bank.createAccount("A", 0);
        bank.deposit(a.getAccountId(), 100);
        check(throwsIAE(() -> bank.withdraw(a.getAccountId(), 101)), "TC2: overdraw rejected");
        check(a.getBalance() == 100 && a.getHistory().size() == 1, "TC2: no stack entry, balance unchanged");
        check(throwsIAE(() -> bank.deposit(a.getAccountId(), -5)), "negative amount rejected");
        check(throwsIAE(() -> bank.deposit(a.getAccountId(), 0)), "zero amount rejected");
        check(throwsIAE(() -> bank.deposit(-1, 5)), "unknown account rejected");
        check(throwsIAE(() -> bank.createAccount("  ", 0)), "blank name rejected");
    }

    static void performance() {
        BankingManager bank = new BankingManager();
        int n = 10000;
        int[] ids = new int[n];
        for (int i = 0; i < n; i++) ids[i] = bank.createAccount("User" + i, 0).getAccountId();
        Random r = new Random(1);
        int lookups = 10000;
        int[] targets = new int[lookups];
        for (int i = 0; i < lookups; i++) targets[i] = ids[r.nextInt(n)];

        long t0 = System.nanoTime();
        int found = 0;
        for (int id : targets) if (bank.find(id) != null) found++;
        long bstNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        int found2 = 0;
        for (int id : targets) if (bank.list().linearSearch(id) != null) found2++;
        long listNs = System.nanoTime() - t0;

        System.out.printf("TC3: %d lookups over %d accounts | BST: %.2f ms | LinkedList: %.2f ms | x%.0f faster%n",
                lookups, n, bstNs / 1e6, listNs / 1e6, (double) listNs / Math.max(bstNs, 1));
        check(found == lookups && found2 == lookups, "TC3: all lookups found");
        check(bstNs < listNs, "TC3: BST faster than linked list");
    }
}
