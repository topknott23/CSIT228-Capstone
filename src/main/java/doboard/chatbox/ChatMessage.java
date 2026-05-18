package doboard.chatbox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private final int messageId;
    private final String senderName;
    private final String messageText;
    private final LocalDateTime sentAt;
    private final boolean isCurrentUser;

    // Standardized formatter for chat bubbles (e.g., "05:50 PM")
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public ChatMessage(int messageId, String senderName, String messageText, LocalDateTime sentAt, boolean isCurrentUser) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.sentAt = sentAt;
        this.isCurrentUser = isCurrentUser;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public String getFormattedTime() {
        if (sentAt == null) {
            return "";
        }
        return sentAt.format(TIME_FORMATTER);
    }
}