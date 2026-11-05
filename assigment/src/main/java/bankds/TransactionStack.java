package bankds;

/** Stack tự cài bằng danh sách liên kết đơn (LIFO). */
public class TransactionStack {
    private Transaction top;
    private int size;

    public void push(Transaction t) {
        t.next = top;
        top = t;
        size++;
    }

    /** Lấy và xóa giao dịch trên đỉnh; trả về null nếu rỗng. */
    public Transaction pop() {
        if (top == null) return null;
        Transaction t = top;
        top = t.next;
        t.next = null;
        size--;
        return t;
    }

    public Transaction peek() { return top; }

    public boolean isEmpty() { return top == null; }

    public int size() { return size; }

    /** In từ mới nhất đến cũ nhất. */
    public void printHistory() {
        if (top == null) {
            System.out.println("  (chua co giao dich)");
            return;
        }
        for (Transaction t = top; t != null; t = t.next) System.out.println("  " + t);
    }
}
