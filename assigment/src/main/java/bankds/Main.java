package bankds;

import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);

    private static String prompt(String msg) {
        System.out.print(msg);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private static long readAmount() {
        String s = prompt("So tien: ");
        if (s == null) throw new IllegalArgumentException("Het du lieu nhap");
        try {
            long v = Long.parseLong(s.replace(",", ""));
            if (v <= 0) throw new IllegalArgumentException("So tien phai > 0");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("So tien khong hop le (phai la so nguyen duong)");
        }
    }

    private static int readId() {
        String s = prompt("Ma tai khoan: ");
        if (s == null) throw new IllegalArgumentException("Het du lieu nhap");
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ma tai khoan khong hop le");
        }
    }

    public static void main(String[] args) {
        BankingManager bank = new BankingManager();
        while (true) {
            System.out.println("\n=== BANKING SYSTEM ===");
            System.out.println("1. Tao tai khoan\n2. Nap tien\n3. Rut tien\n4. Xem lich su\n5. Hoan tac giao dich cuoi\n6. Thoat");
            String choice = prompt("Chon: ");
            if (choice == null) return;
            try {
                switch (choice) {
                    case "1" -> {
                        String name = prompt("Ten chu tai khoan: ");
                        Account a = bank.createAccount(name, 0);
                        System.out.println("Da tao tai khoan, ma = " + a.getAccountId());
                    }
                    case "2" -> {
                        int id = readId();
                        bank.deposit(id, readAmount());
                        System.out.println("Nap tien thanh cong. So du: " + String.format("%,d", bank.find(id).getBalance()));
                    }
                    case "3" -> {
                        int id = readId();
                        bank.withdraw(id, readAmount());
                        System.out.println("Rut tien thanh cong. So du: " + String.format("%,d", bank.find(id).getBalance()));
                    }
                    case "4" -> bank.viewHistory(readId());
                    case "5" -> System.out.println("Da hoan tac: " + bank.undoLast(readId()));
                    case "6" -> { System.out.println("Tam biet!"); return; }
                    default -> System.out.println("Lua chon khong hop le.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }
}
