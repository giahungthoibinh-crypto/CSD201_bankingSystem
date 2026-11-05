package bankds;

/** Danh sách liên kết đơn quản lý tài khoản (có con trỏ tail để chèn cuối O(1)). */
public class AccountList {
    private Account head, tail;
    private int size;

    public void insert(Account a) {
        a.next = null;
        if (head == null) head = tail = a;
        else { tail.next = a; tail = a; }
        size++;
    }

    public boolean delete(int accountId) {
        Account prev = null;
        for (Account cur = head; cur != null; prev = cur, cur = cur.next) {
            if (cur.accountId != accountId) continue;
            if (prev == null) head = cur.next; else prev.next = cur.next;
            if (cur == tail) tail = prev;
            cur.next = null;
            size--;
            return true;
        }
        return false;
    }

    /** Tìm tuyến tính O(n) - dùng để so sánh với BST. */
    public Account linearSearch(int accountId) {
        for (Account c = head; c != null; c = c.next) if (c.accountId == accountId) return c;
        return null;
    }

    public int size() { return size; }

    public Account head() { return head; }
}
