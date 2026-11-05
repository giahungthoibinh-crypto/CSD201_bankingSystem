# Banking System (Java)

Manage accounts, transactions, and search accounts. Pure Java 17+ (no external dependencies).

## Run

```powershell
.\build.ps1 run    # console menu
.\build.ps1 test   # 11 tests, dependency-free runner
```

## Structure

| File | Role |
|------|------|
| `Bank` | Service: open/freeze/close accounts, deposit/withdraw/transfer, search |
| `Account` | id, owner, balance (`BigDecimal`), status (ACTIVE/FROZEN/CLOSED) |
| `Transaction` / `TransactionType` | Ledger entry (DEPOSIT, WITHDRAW, TRANSFER) |
| `Main` | Console UI |

## Data structures

| Structure | Purpose | Cost |
|-----------|---------|------|
| `HashMap<id, Account>` | Lookup by account id | O(1) |
| `TreeMap<owner, Set<id>>` | Sorted owner index, prefix search (`tailMap`) | O(log n + k) |
| `HashMap<id, List<Transaction>>` | Per-account history | O(1) append |
| `ArrayList<Transaction>` | Global chronological ledger | O(1) append |

## Complexity analysis (n = accounts, k = matches, t = transactions of an account)

| Operation | Time | Space |
|-----------|------|-------|
| openAccount | O(log n) | O(1) |
| deposit / withdraw / transfer | O(1) | O(1) per transaction |
| getHistory | O(1) (unmodifiable view) | O(1) |
| findById | O(1) | O(1) |
| searchByOwnerPrefix | O(log n + k) | O(k) |
| searchByOwnerContains | O(n) | O(k) |
| searchByBalanceRange | O(n + k log k) | O(k) |

## Business rules

- Amounts must be > 0; initial deposit >= 0; owner must not be blank.
- Withdraw/transfer cannot exceed balance (exactly the balance is allowed).
- Only ACTIVE accounts can transact; transfer to the same account is rejected.
- An account can be closed only with a zero balance; CLOSED is final.
- Failed operations never change balances or the ledger.

## Combinatorial test analysis

Transfer is tested over all combinations of: source status (3) x target status (3) x amount
(below / equal / above balance = 3) = 27 cases. Only ACTIVE/ACTIVE with amount <= balance succeeds;
all other 26 must fail and leave both balances unchanged. See `BankTest.transferCombinatorial`.
Other tests cover boundary values (0, 0.01, balance, balance + 0.01) and search equivalence classes.
