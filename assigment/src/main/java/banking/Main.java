package banking;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("""

                === BANKING SYSTEM ===
                1. Open account        6. Search by owner prefix
                2. Deposit             7. Search by owner contains
                3. Withdraw            8. Search by balance range
                4. Transfer            9. Freeze / Unfreeze / Close
                5. Account history     0. Exit""");
            System.out.print("Choose: ");
            if (!in.hasNextLine()) {
                return;
            }
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        Account a = bank.openAccount(ask(in, "Owner"), money(in, "Initial deposit"));
                        System.out.println("Created: " + a);
                    }
                    case "2" -> System.out.println(bank.deposit(ask(in, "Account id"), money(in, "Amount")));
                    case "3" -> System.out.println(bank.withdraw(ask(in, "Account id"), money(in, "Amount")));
                    case "4" -> System.out.println(
                            bank.transfer(ask(in, "From id"), ask(in, "To id"), money(in, "Amount")));
                    case "5" -> bank.getHistory(ask(in, "Account id")).forEach(System.out::println);
                    case "6" -> print(bank.searchByOwnerPrefix(ask(in, "Prefix")));
                    case "7" -> print(bank.searchByOwnerContains(ask(in, "Text")));
                    case "8" -> print(bank.searchByBalanceRange(money(in, "Min"), money(in, "Max")));
                    case "9" -> manage(bank, in);
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Invalid option");
                }
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void manage(Bank bank, Scanner in) {
        String id = ask(in, "Account id");
        switch (ask(in, "Action (freeze/unfreeze/close)").toLowerCase()) {
            case "freeze" -> bank.freezeAccount(id);
            case "unfreeze" -> bank.unfreezeAccount(id);
            case "close" -> bank.closeAccount(id);
            default -> throw new BankException("Unknown action");
        }
        System.out.println("Done: " + bank.getAccount(id));
    }

    private static void print(List<Account> accounts) {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found");
        }
        accounts.forEach(System.out::println);
    }

    private static String ask(Scanner in, String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private static BigDecimal money(Scanner in, String label) {
        try {
            return new BigDecimal(ask(in, label));
        } catch (NumberFormatException e) {
            throw new BankException("Invalid number");
        }
    }
}
