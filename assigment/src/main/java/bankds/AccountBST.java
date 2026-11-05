package bankds;

/** Cây nhị phân tìm kiếm theo accountId, mỗi node tham chiếu tới Account trong Linked List. */
public class AccountBST {
    private static class Node {
        final int key;
        final Account account;
        Node left, right;

        Node(int key, Account account) { this.key = key; this.account = account; }
    }

    private Node root;
    private int size;

    /** Chèn lặp (không đệ quy) để tránh tràn stack khi cây lệch. Trả về false nếu ID đã tồn tại. */
    public boolean insert(int accountId, Account account) {
        if (root == null) { root = new Node(accountId, account); size++; return true; }
        Node cur = root;
        while (true) {
            if (accountId == cur.key) return false;
            if (accountId < cur.key) {
                if (cur.left == null) { cur.left = new Node(accountId, account); break; }
                cur = cur.left;
            } else {
                if (cur.right == null) { cur.right = new Node(accountId, account); break; }
                cur = cur.right;
            }
        }
        size++;
        return true;
    }

    /** Trả về Account hoặc null nếu không tồn tại. */
    public Account search(int accountId) {
        Node cur = root;
        while (cur != null) {
            if (accountId == cur.key) return cur.account;
            cur = accountId < cur.key ? cur.left : cur.right;
        }
        return null;
    }

    public int size() { return size; }
}
