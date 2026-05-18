package doboard.chatbox;

import doboard.common.connection.SQLConnector;
import doboard.common.session.SessionHandler;
import doboard.dorm.DormDAO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatController {

    @FXML private ListView<ChatMessage> chatListView;
    private final ObservableList<ChatMessage> messageList = FXCollections.observableArrayList();

    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Label dormNameLabel;
    @FXML private Label dormCodeLabel;

    // Updated to use the MemberStatus object model wrapper
    @FXML private ListView<MemberStatus> membersListView;
    private final ObservableList<MemberStatus> memberStatusList = FXCollections.observableArrayList();

    private final DormDAO dormDAO = new DormDAO();

    private int lastMessageId = 0;
    private int firstMessageId = -1;
    private int currentUserId;
    private int currentDormId = -1;



    private ScheduledExecutorService executorService;

    @FXML
    public void initialize() {
        chatListView.setItems(messageList);
        membersListView.setItems(memberStatusList);

        // --- 1. CHAT BUBBLE CELL FACTORY ---
        chatListView.setCellFactory(lv -> new javafx.scene.control.ListCell<ChatMessage>() {
            private final HBox rootLayout = new HBox();
            private final VBox bubbleLayout = new VBox(2);
            private final Label nameLabel = new Label();
            private final Label textLabel = new Label();
            private final Label timeLabel = new Label();

            {
                textLabel.setWrapText(true);
                textLabel.getStyleClass().add("text-md");
                nameLabel.getStyleClass().addAll("text-sm", "font-bold", "text-brand-blue");
                timeLabel.getStyleClass().addAll("text-sm", "text-gray");
                bubbleLayout.getStyleClass().addAll("p-2", "rounded-md");

                textLabel.maxWidthProperty().bind(chatListView.widthProperty().multiply(0.70));
                nameLabel.maxWidthProperty().bind(chatListView.widthProperty().multiply(0.70));
                rootLayout.getChildren().add(bubbleLayout);
            }

            @Override
            protected void updateItem(ChatMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    bubbleLayout.getStyleClass().removeAll("bg-brand-blue1", "bg-gray-light");
                    bubbleLayout.getChildren().clear();

                    textLabel.setText(item.getMessageText());
                    timeLabel.setText(item.getFormattedTime());

                    if (item.isCurrentUser()) {
                        rootLayout.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        bubbleLayout.getStyleClass().add("bg-brand-blue1");
                        timeLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        bubbleLayout.getChildren().addAll(textLabel, timeLabel);
                    } else {
                        rootLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        bubbleLayout.getStyleClass().add("bg-gray-light");
                        nameLabel.setText(item.getSenderName());
                        timeLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        bubbleLayout.getChildren().addAll(nameLabel, textLabel, timeLabel);
                    }
                    setGraphic(rootLayout);
                    setStyle("-fx-background-color: transparent; -fx-padding: 4px 8px;");
                }
            }
        });

        // --- 2. PRESENCE INDICATOR CELL FACTORY (SIDEBAR) ---
        membersListView.setCellFactory(lv -> new javafx.scene.control.ListCell<MemberStatus>() {
            private final HBox rowContainer = new HBox(8); // 8px side padding spacing
            private final Circle statusDot = new Circle(5); // 5px dot radius
            private final Label nameLabel = new Label();

            {
                rowContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                nameLabel.getStyleClass().add("text-md");
                rowContainer.getChildren().addAll(statusDot, nameLabel);
            }

            @Override
            protected void updateItem(MemberStatus member, boolean empty) {
                super.updateItem(member, empty);
                if (empty || member == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(member.getFullName());

                    // Toggle colors dynamically based on active presence states
                    if (member.isOnline()) {
                        statusDot.setFill(Color.web("#2ecc71")); // Vivid Green
                    } else {
                        statusDot.setFill(Color.web("#95a5a6")); // Muted Flat Gray
                    }

                    setGraphic(rowContainer);
                }
            }
        });

        // --- 3. SESSION INITIALIZATION ---
        if (SessionHandler.loadSession() != null) {
            currentUserId = SessionHandler.loadSession().getUser_id();
            currentDormId = dormDAO.getDormIdByUserId(currentUserId);
        }

        if (currentDormId == -1) {
            System.err.println("Chat Warning: User is not assigned to any dorm room chat workspace.");
            messageInput.setDisable(true);
            sendButton.setDisable(true);
            return;
        }

        dormNameLabel.setText(dormDAO.findById(currentDormId).getDorm_name());
        dormCodeLabel.setText("Dorm Workspace ID: #" + currentDormId);

        startMessagePoller();
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || currentDormId == -1) {
            return;
        }

        messageInput.setDisable(true);
        sendButton.setDisable(true);

        new Thread(() -> {
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
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageInput.setDisable(false);
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Handles periodic network polling routines using a background scheduler thread pool.
     */
    /**
     * Handles periodic network polling routines using a background scheduler thread pool.
     */
    /**
     * Handles periodic network polling routines using a background scheduler thread pool.
     */
    /**
     * Handles periodic network polling routines using a background scheduler thread pool.
     */
    private void startMessagePoller() {
        executorService = Executors.newScheduledThreadPool(3, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        // 1. Queue the initial history load to run immediately on a background thread lane
        executorService.submit(() -> {
            try {
                loadInitialMessages();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        // 2. Task A: Synchronize incoming text streams (Delayed by 2 seconds to allow history to land first)
        executorService.scheduleAtFixedRate(() -> {
            try {
                fetchNewMessages();
            } catch (Throwable t) {
                System.err.println("[Chat Error] Exception in message stream loop:");
                t.printStackTrace();
            }
        }, 2000, 1500, TimeUnit.MILLISECONDS); // Initial delay set to 2000ms

        // 3. Task B: Tell the XAMPP database that we are active (Every 30 seconds)
        executorService.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Throwable t) {
                System.err.println("[Chat Error] Exception in heartbeat loop:");
                t.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);

        // 4. Task C: Update the sidebar presence array list (Every 10 seconds)
        executorService.scheduleAtFixedRate(() -> {
            try {
                fetchMemberStatus();
            } catch (Throwable t) {
                System.err.println("[Chat Error] Exception in sidebar status loop:");
                t.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Fetches only the latest 50 messages from the database on startup,
     * reverses them to be chronological, and sets the boundary tracking IDs.
     */
    private void loadInitialMessages() {
        // Fetches top 50 rows going backward from the newest record
        String query = "SELECT c.message_id, c.sender_id, c.message_text, c.sent_at, u.full_name " +
                "FROM chat_messages c " +
                "JOIN users u ON c.sender_id = u.user_id " +
                "WHERE c.dorm_id = ? " +
                "ORDER BY c.message_id DESC LIMIT 50";

        List<ChatMessage> initialList = new java.util.ArrayList<>();

        try (Connection conn = SQLConnector.getConnection()) {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, currentDormId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("message_id");
                            int senderId = rs.getInt("sender_id");
                            String messageText = rs.getString("message_text");
                            String senderName = rs.getString("full_name");
                            java.sql.Timestamp timestamp = rs.getTimestamp("sent_at");
                            LocalDateTime sentAt = timestamp.toLocalDateTime();

                            boolean isCurrentUser = (senderId == currentUserId);
                            initialList.add(new ChatMessage(id, senderName, messageText, sentAt, isCurrentUser));
                        }
                    }
                }
            }

            if (!initialList.isEmpty()) {
                // Reverse the collection so oldest entries sit at index 0 and newest sit at the end
                java.util.Collections.reverse(initialList);

                // Establish our historical viewport boundaries
                firstMessageId = initialList.get(0).getMessageId();
                lastMessageId = initialList.get(initialList.size() - 1).getMessageId();

                // Populate layout cleanly on the JavaFX UI thread
                Platform.runLater(() -> {
                    messageList.setAll(initialList);
                    chatListView.scrollTo(messageList.size() - 1);
                });
            }
        } catch (SQLException e) {
            System.err.println("Critical Error running initial history pagination fetch.");
            e.printStackTrace();
        }
    }

    private void fetchNewMessages() {
        String query = "SELECT c.message_id, c.sender_id, c.message_text, c.sent_at, u.full_name " +
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
                            java.sql.Timestamp timestamp = rs.getTimestamp("sent_at");
                            LocalDateTime sentAt = timestamp.toLocalDateTime();

                            boolean isCurrentUser = (senderId == currentUserId);
                            ChatMessage message = new ChatMessage(id, senderName, messageText, sentAt, isCurrentUser);

                            Platform.runLater(() -> {
                                // 1. Look up the internal vertical scrollbar of the ListView
                                javafx.scene.control.ScrollBar scrollBar = (javafx.scene.control.ScrollBar) chatListView.lookup(".scroll-bar:vertical");

                                boolean shouldScrollToBottom = true;

                                if (scrollBar != null) {
                                    double currentScroll = scrollBar.getValue();
                                    double maxScroll = scrollBar.getMax();

                                    // JavaFX scrollbars use a normalized scale from 0.0 (top) to 1.0 (bottom).
                                    // If the difference is greater than 0.05, the user has scrolled up.
                                    if ((maxScroll - currentScroll) > 0.05) {
                                        shouldScrollToBottom = false;
                                    }
                                }

                                // 2. Add the new message to the stream
                                messageList.add(message);

                                // 3. Only snap down if the user was already at the bottom boundary
                                if (shouldScrollToBottom) {
                                    chatListView.scrollTo(messageList.size() - 1);
                                }
                            });
                            lastMessageId = id;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Dorm channel connection lost... retrying.");
        }
    }

    /**
     * Updates the current user's last_seen timestamp field directly on the server host.
     */
    private void sendHeartbeat() {
        // UNIX_TIMESTAMP() returns a timezone-blind integer (e.g., 1773942855)
        String query = "UPDATE users SET last_seen = UNIX_TIMESTAMP() WHERE user_id = ?";
        try (Connection conn = SQLConnector.getConnection()) {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, currentUserId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Heartbeat ping failed to reach network database server.");
        }
    }

    /**
     * Fetches all room members and checks if they are online using the server host's clock time.
     */
    private void fetchMemberStatus() {
        String query = "SELECT u.full_name, " +
                "(u.last_seen IS NOT NULL AND (UNIX_TIMESTAMP() - u.last_seen) <= 10) AS is_online " +
                "FROM dorm_members dm " +
                "JOIN users u ON dm.user_id = u.user_id " +
                "WHERE dm.dorm_id = ? " +
                "ORDER BY is_online DESC, u.full_name ASC";

        List<MemberStatus> temporaryList = new ArrayList<>();

        try (Connection conn = SQLConnector.getConnection()) {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, currentDormId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String name = rs.getString("full_name");
                            boolean isOnline = rs.getBoolean("is_online");
                            temporaryList.add(new MemberStatus(name, isOnline));
                        }
                    }
                }
            }

            Platform.runLater(() -> {
                memberStatusList.setAll(temporaryList);
            });

        } catch (SQLException e) {
            System.err.println("Failed to synchronize sidebar user status logs.");
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}