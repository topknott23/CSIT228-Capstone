package doboard.chatbox;

public class MemberStatus {
    private final String fullName;
    private final boolean online;

    public MemberStatus(String fullName, boolean online) {
        this.fullName = fullName;
        this.online = online;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isOnline() {
        return online;
    }
}