# Giải thích mã nguồn và luồng hoạt động

## 1. Tổng quan

Đây là ứng dụng ngân hàng chạy trên console, viết bằng Java 17 trở lên. Người dùng có thể mở và quản lý tài khoản, gửi/rút tiền, chuyển khoản, xem lịch sử giao dịch và tìm kiếm tài khoản.

Ứng dụng không dùng cơ sở dữ liệu. Tài khoản và giao dịch chỉ được lưu trong bộ nhớ khi chương trình đang chạy; dữ liệu sẽ mất khi thoát chương trình.

## 2. Cấu trúc mã nguồn

Các file chính nằm trong `src/main/java/banking/`:

| File | Vai trò |
|---|---|
| `Main.java` | Điểm bắt đầu chương trình, hiển thị menu, nhận dữ liệu từ console và gọi các chức năng của `Bank`. |
| `Bank.java` | Nghiệp vụ chính: quản lý tài khoản, trạng thái, giao dịch, lịch sử và tìm kiếm. |
| `Account.java` | Dữ liệu một tài khoản: mã, chủ tài khoản, số dư và trạng thái. |
| `Transaction.java` | Thông tin một giao dịch: mã, loại, tài khoản nguồn/đích, số tiền và thời gian. |
| `TransactionType.java` | Các loại giao dịch: `DEPOSIT`, `WITHDRAW`, `TRANSFER`. |
| `BankException.java` | Ngoại lệ nghiệp vụ dùng để báo lỗi như số dư không đủ hoặc tài khoản không hợp lệ. |

`src/test/java/banking/BankTest.java` chứa bộ kiểm thử tự viết, không cần thư viện test bên ngoài. `build.ps1` biên dịch chương trình và chạy ứng dụng hoặc kiểm thử.

## 3. Vai trò của các lớp

### `Main` — giao diện console

`main()` tạo một đối tượng `Bank` và một `Scanner`, sau đó lặp để hiển thị menu. Mỗi lựa chọn sẽ:

1. Đọc thông tin cần thiết từ người dùng.
2. Gọi phương thức tương ứng trong `Bank`.
3. In kết quả hoặc thông báo lỗi.

Các lựa chọn bao gồm mở tài khoản, gửi tiền, rút tiền, chuyển khoản, xem lịch sử, tìm kiếm theo tên hoặc số dư, quản lý trạng thái tài khoản và thoát. Hàm `money()` chuyển chuỗi nhập vào thành `BigDecimal`; nếu định dạng số không hợp lệ, chương trình báo `Invalid number`. Các lỗi nghiệp vụ `BankException` được bắt trong vòng lặp và hiển thị, vì vậy menu tiếp tục hoạt động sau lỗi.

### `Bank` — nghiệp vụ và dữ liệu trong bộ nhớ

`Bank` giữ dữ liệu bằng bốn cấu trúc:

| Cấu trúc | Nội dung |
|---|---|
| `accounts` (`HashMap`) | Tra cứu tài khoản theo mã. |
| `ownerIndex` (`TreeMap`) | Chỉ mục tên chủ tài khoản đã chuẩn hóa, hỗ trợ tìm tiền tố. |
| `history` (`HashMap`) | Danh sách giao dịch của từng tài khoản. |
| `ledger` (`ArrayList`) | Sổ giao dịch chung theo thứ tự ghi nhận. |

Mã tài khoản được tăng tuần tự theo dạng `ACC0001`; mã giao dịch theo dạng `TX00001`. Số tiền dùng `BigDecimal` để tránh sai số kiểu số thực.

### `Account` — trạng thái tài khoản

Tài khoản có ba trạng thái:

- `ACTIVE`: được phép giao dịch.
- `FROZEN`: tạm khóa giao dịch.
- `CLOSED`: đã đóng, không thể mở lại.

Các trường mã tài khoản và chủ tài khoản không thay đổi sau khi tạo. Số dư và trạng thái được cập nhật bởi `Bank`.

### `Transaction` và `TransactionType` — sổ giao dịch

Mỗi giao dịch có mã, loại, số tiền và thời điểm tạo. Giao dịch chuyển khoản có cả mã tài khoản nguồn và đích; giao dịch gửi/rút tiền chỉ dùng tài khoản nguồn. Khi được ghi nhận, giao dịch được thêm vào sổ chung và lịch sử của tài khoản liên quan.

## 4. Luồng khởi chạy và thao tác

### Khởi chạy

1. Chạy `Main.main()`.
2. Tạo `Bank` mới với các bộ lưu trữ trống và bộ đếm mã bắt đầu từ 0.
3. Hiển thị menu và chờ lựa chọn.
4. Lặp lại cho đến khi người dùng chọn `0` hoặc đầu vào kết thúc.

### Mở tài khoản

1. `Main` đọc tên chủ tài khoản và khoản tiền gửi ban đầu.
2. `Bank.openAccount()` kiểm tra tên không rỗng và số tiền ban đầu không âm.
3. Tạo tài khoản mới ở trạng thái `ACTIVE`, chuẩn hóa tên để lập chỉ mục, đồng thời tạo danh sách lịch sử rỗng.
4. Nếu khoản tiền ban đầu lớn hơn 0, tạo giao dịch `DEPOSIT` và ghi vào lịch sử cùng sổ chung. Nếu bằng 0, không tạo giao dịch.
5. Trả tài khoản mới về `Main` để hiển thị.

### Gửi hoặc rút tiền

1. Kiểm tra số tiền phải lớn hơn 0.
2. Tìm tài khoản và kiểm tra trạng thái phải là `ACTIVE`.
3. Với rút tiền, kiểm tra số dư đủ; rút đúng bằng số dư được phép.
4. Cập nhật số dư.
5. Tạo giao dịch tương ứng và ghi nhận vào lịch sử của tài khoản cùng sổ chung.

Nếu bước kiểm tra thất bại, nghiệp vụ ném `BankException`; số dư và sổ giao dịch không bị thay đổi.

### Chuyển khoản

1. Kiểm tra số tiền dương và hai mã tài khoản không giống nhau.
2. Tìm cả tài khoản nguồn lẫn tài khoản đích; cả hai phải ở trạng thái `ACTIVE`.
3. Kiểm tra tài khoản nguồn có đủ tiền.
4. Trừ tiền ở nguồn, cộng tiền vào đích.
5. Tạo một giao dịch `TRANSFER`, thêm cùng giao dịch đó vào lịch sử của cả hai tài khoản và sổ chung.

Các kiểm tra tài khoản và số dư diễn ra trước khi cập nhật tiền, nên chuyển khoản thất bại không làm thay đổi hai số dư.

### Đóng băng, mở đóng băng và đóng tài khoản

- Đóng băng: chuyển tài khoản chưa đóng sang `FROZEN`.
- Mở đóng băng: chỉ tài khoản `FROZEN` mới chuyển lại thành `ACTIVE`.
- Đóng tài khoản: chỉ cho phép khi số dư bằng 0; trạng thái `CLOSED` là trạng thái cuối.

Tài khoản không ở trạng thái `ACTIVE` không thể gửi, rút hoặc chuyển tiền.

### Xem lịch sử và tìm kiếm

- Xem lịch sử: lấy danh sách của tài khoản. Danh sách trả về không cho phép bên gọi sửa đổi.
- Tìm theo mã: tra cứu trực tiếp; `findById()` trả về `null` nếu không tìm thấy.
- Tìm theo tiền tố tên: không phân biệt chữ hoa/thường, dùng `TreeMap` để duyệt vùng tên phù hợp.
- Tìm theo chuỗi trong tên: không phân biệt chữ hoa/thường, duyệt chỉ mục tên.
- Tìm theo khoảng số dư: hai cận được tính bao gồm; kết quả được sắp xếp theo mã tài khoản.

## 5. Luồng biên dịch và kiểm thử

Mở PowerShell tại thư mục chứa `build.ps1`:

```powershell
.\build.ps1 run
```

Lệnh này xóa và tạo lại thư mục `out`, biên dịch mã trong `src/main/java`, rồi chạy `banking.Main`.

Chạy bộ kiểm thử:

```powershell
.\build.ps1 test
```

Script biên dịch mã chính và mã kiểm thử, sau đó chạy `banking.BankTest`. Bộ kiểm thử bao phủ việc mở tài khoản, gửi/rút tiền, biên số tiền, chuyển khoản, vòng đời tài khoản và các kiểu tìm kiếm. Riêng chuyển khoản kiểm tra mọi tổ hợp trạng thái nguồn, trạng thái đích và số tiền (dưới, bằng hoặc vượt số dư).

## 6. Tóm tắt luồng tổng thể

```text
Người dùng
   |
   v
Main (menu, nhập/xuất console)
   |
   v
Bank (kiểm tra quy tắc, cập nhật dữ liệu, tìm kiếm)
   |                  |
   v                  v
Account          Transaction
   |                  |
   +----> history / ledger <----+
```

`Main` chịu trách nhiệm giao tiếp với người dùng; `Bank` là nơi tập trung quy tắc nghiệp vụ; `Account` và `Transaction` biểu diễn dữ liệu. Toàn bộ dữ liệu hiện được quản lý trong bộ nhớ và không được lưu lâu dài.
