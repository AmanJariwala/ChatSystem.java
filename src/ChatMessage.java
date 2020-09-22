import java.io.Serializable;

/**
 * [Add your documentation here]
 *
 * @author Aman Jariwala and Sultan Al-Ali, lab sec 04
 * @version April 26, 2020
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private String message;
    private int type;
    private String recipient;
    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;

    }

    public String getRecipient() {
        return this.recipient;
    }

    public String getMessage() {
        return this.message;
    }

    public int getType() {
        return this.type;
    }
}
