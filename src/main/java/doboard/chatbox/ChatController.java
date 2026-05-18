package doboard.chatbox;

import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.dorm.DormDAO; // Imported to target room context

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatController {

    @FXML private ListView<String> chatListView;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Label dormNameLabel;
    @FXML private Label dormCodeLabel;
    @FXML private ListView<String> membersListView;

    private final ObservableList<String> memberNamesList = FXCollections.observableArrayList();
    private final ObservableList<String> messageList = FXCollections.observableArrayList();
    private final DormDAO dormDAO = new DormDAO();

    private int lastMessageId = 0;
    private int currentUserId;
    private int currentDormId = -1; // Scope database queries to this specific room instance

    private ScheduledExecutorService executorService;

    @FXML
    public void initialize() {
        chatListView.setItems(messageList);

        // 1. Resolve identity and dorm group scoping boundaries
        if (SessionHandler.loadSession() != null) {
            currentUserId = SessionHandler.loadSession().getUser_id();
            currentDormId = dormDAO.getDormIdByUserId(currentUserId);
        }

        // 2. Fallback check: Block input if user is not registered to a dorm room
        if (currentDormId == -1) {
            messageList.add("[System]: You are not assigned to any dorm room chat workspace.");
            messageInput.setDisable(true);
            sendButton.setDisable(true);
            return;
        }

        // Inside initialize() after validating currentDormId != -1
        dormNameLabel.setText(dormDAO.findById(currentDormId).getDorm_name()); // Or your equivalent getter
        dormCodeLabel.setText("Dorm Workspace ID: #" + currentDormId);

        // Populate the sidebar using your existing logic pattern
        membersListView.setItems(memberNamesList);
        java.util.List<doboard.dorm.DormMember> members = dormDAO.getMembersByDorm(currentDormId);
        java.util.List<Integer> userIds = members.stream().map(m -> m.getUser_id()).collect(java.util.stream.Collectors.toList());
        java.util.List<doboard.auth.User> users = doboard.auth.UserDAO.getUsersByIds(userIds);

        for (doboard.auth.User u : users) {
            memberNamesList.add(u.getFull_name());
        }

        // 3. Begin listening for group space events
        startMessagePoller();
    }

    /**
     * Handles both clicking the "Send" button and pressing ENTER in the TextField.
     */
    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || currentDormId == -1) {
            return;
        }

        messageInput.setDisable(true);
        sendButton.setDisable(true);

        new Thread(() -> {
            // Updated to save the message directly into the dorm's group space
            String query = "INSERT INTO chat_messages (dorm_id, sender_id, message_text) VALUES (?, ?, ?)";

            try (Connection conn = SQLConnector.getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, currentDormId);
                        stmt.setInt(2, currentUserId);
                        stmt.setString(3, text);
                        stmt.executeUpdate();

                        Platform.runLater(() -> {
                            messageInput.clear();
                            messageInput.setDisable(false);
                            sendButton.setDisable(false);
                            messageInput.requestFocus();
                        });
                    }
                } else {
                    throw new SQLException("Connection is null.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("Failed to send message.");
                    messageInput.setDisable(false);
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Spawns a background thread scheduler that checks the XAMPP server
     * for new database records without blocking the application window.
     */
    private void startMessagePoller() {
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(this::fetchNewMessages, 0, 1500, TimeUnit.MILLISECONDS);
    }

    /**
     * Queries the database for rows belonging to this dorm, created after our last checked ID.
     */
    private void fetchNewMessages() {
        String query = "SELECT c.message_id, c.sender_id, c.message_text, u.full_name " +
                "FROM chat_messages c " +
                "JOIN users u ON c.sender_id = u.user_id " +
                "WHERE c.dorm_id = ? AND c.message_id > ? " +
                "ORDER BY c.message_id ASC";

        try (Connection conn = SQLConnector.getConnection()) {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, currentDormId);
                    stmt.setInt(2, lastMessageId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("message_id");
                            int senderId = rs.getInt("sender_id");
                            String messageText = rs.getString("message_text");
                            String senderName = rs.getString("full_name");

                            String formattedMessage = (senderId == currentUserId)
                                    ? "[You]: " + messageText
                                    : "[" + senderName + "]: " + messageText;

                            Platform.runLater(() -> {
                                messageList.add(formattedMessage);
                                chatListView.scrollTo(messageList.size() - 1);
                            });

                            lastMessageId = id;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Dorm channel synchronizer encountered a network drop... retrying.");
        }
    }

    /**
     * Call this lifecycle method when closing the view to prevent thread leaks.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}