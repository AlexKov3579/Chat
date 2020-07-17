import java.io.Serializable;

public class Message implements Serializable {
    private String text;
    private User sender, destination;

    Message(User sender, User destination, String text) {
        this.text = text;
        this.sender = sender;
        this.destination = destination;
    }

    Message(User sender, String text) {
        this.text = text;
        this.sender = sender;
        destination = null;
    }

    Message(String text) {
        this.text = text;
        sender = destination = null;
    }

    public String getText() {
        return text;
    }

    public User getDestination() {
        return destination;
    }

    public User getSender() {
        return sender;
    }
}
