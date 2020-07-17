public class SystemMessage extends Message {
    private int flag;
    public static final int USER_INFO = 1;

    SystemMessage(User sender, User destination, String text, int flag) {
        super(sender, destination, text);
        this.flag = flag;

    }

    SystemMessage(User sender, String text, int flag) {
        super(sender, text);
        this.flag = flag;
    }

    SystemMessage(String text, int flag) {
        super(text);
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
