package doboard.core.controllers;

import doboard.core.dao.BillDAO;
import doboard.core.dao.ChoreDAO;
import doboard.core.dao.DormDAO;
import doboard.core.models.ChoreDisplay;
import doboard.core.models.Dorm;
import doboard.core.models.User;
import doboard.core.util.AppEventBus;
import doboard.core.util.Observer;
import doboard.core.util.SceneLoader;
import doboard.core.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class MainMenuController implements Observer {

    @FXML private Label welcomeLabel;
    @FXML private ListView<ChoreDisplay> dashboardChoresList;
    @FXML private ListView<doboard.core.models.BillDisplay> dashboardBillsList;

    private ChoreDAO choreDAO;
    private BillDAO billDAO;
    private DormDAO dormDAO;

    @FXML
    private void initialize() {
        choreDAO = new ChoreDAO();
        billDAO = new BillDAO();
        dormDAO = new DormDAO();

        AppEventBus.getInstance().attach(this);

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getUsername();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome back, " + name + "!");
            }

            if (dashboardChoresList != null) {
                dashboardChoresList.setCellFactory(param -> new javafx.scene.control.ListCell<ChoreDisplay>() {
                    @Override
                    protected void updateItem(ChoreDisplay chore, boolean empty) {
                        super.updateItem(chore, empty);
                        if (empty || chore == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("-fx-background-color: transparent;");
                        } else {
                            javafx.scene.layout.HBox root = new javafx.scene.layout.HBox(10);
                            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            root.setStyle("-fx-padding: 10px; -fx-background-color: #0E7CB5; -fx-background-radius: 5px;");

                            String displayText = chore.getTitle() + " - Due: " + chore.getDueDate();
                            Label choreLabel = new Label(displayText);
                            choreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                            javafx.scene.layout.HBox.setHgrow(choreLabel, javafx.scene.layout.Priority.ALWAYS);
                            choreLabel.setMaxWidth(Double.MAX_VALUE);

                            javafx.scene.control.Button doneBtn = new javafx.scene.control.Button("Task Done");
                            doneBtn.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                            
                            if ("COMPLETED".equals(chore.getStatus())) {
                                doneBtn.setText("Completed");
                                doneBtn.setDisable(true);
                                root.setStyle("-fx-padding: 10px; -fx-background-color: #888888; -fx-background-radius: 5px;");
                            } else {
                                doneBtn.setOnAction(e -> {
                                    if (choreDAO.markChoreAsDone(chore.getChoreId())) {
                                        AppEventBus.getInstance().notifyObservers();
                                    }
                                });
                            }

                            root.getChildren().addAll(choreLabel, doneBtn);
                            setGraphic(root);
                            setStyle("-fx-background-color: transparent; -fx-padding: 5px 0px;");
                        }
                    }
                });
            }

            if (dashboardBillsList != null) {
                dashboardBillsList.setCellFactory(param -> new javafx.scene.control.ListCell<doboard.core.models.BillDisplay>() {
                    @Override
                    protected void updateItem(doboard.core.models.BillDisplay bill, boolean empty) {
                        super.updateItem(bill, empty);
                        if (empty || bill == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("-fx-background-color: transparent;");
                        } else {
                            javafx.scene.layout.HBox root = new javafx.scene.layout.HBox(10);
                            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            root.setStyle("-fx-padding: 10px; -fx-background-color: #E25D5D; -fx-background-radius: 5px;");

                            String displayText = String.format("%s - You Owe: $%.2f (Due: %s)", bill.getTitle(), bill.getAmountOwed(), bill.getDueDate());
                            Label billLabel = new Label(displayText);
                            billLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                            javafx.scene.layout.HBox.setHgrow(billLabel, javafx.scene.layout.Priority.ALWAYS);
                            billLabel.setMaxWidth(Double.MAX_VALUE);

                            javafx.scene.control.Button payBtn = new javafx.scene.control.Button("Pay My Share");
                            payBtn.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                            
                            if (bill.isPaid()) {
                                payBtn.setText("Paid");
                                payBtn.setDisable(true);
                                root.setStyle("-fx-padding: 10px; -fx-background-color: #888888; -fx-background-radius: 5px;");
                            } else {
                                payBtn.setOnAction(e -> {
                                    if (billDAO.markBillAsPaid(bill.getSplitId())) {
                                        AppEventBus.getInstance().notifyObservers();
                                    }
                                });
                            }

                            root.getChildren().addAll(billLabel, payBtn);
                            setGraphic(root);
                            setStyle("-fx-background-color: transparent; -fx-padding: 5px 0px;");
                        }
                    }
                });
            }

            loadDashboardData();
        }
    }

    @Override
    public void update() {
        // Must run on JavaFX Application Thread since Observer is triggered from other controllers
        javafx.application.Platform.runLater(this::loadDashboardData);
    }

    private void loadDashboardData() {
        if (dashboardChoresList == null || dashboardBillsList == null) return;
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        
        Dorm dorm = dormDAO.getUserDorm(user.getUser_id());
        if (dorm != null) {
            // Load pending chores for the user
            List<ChoreDisplay> myChores = choreDAO.getChoresForUser(dorm.getDorm_id(), user.getUser_id(), false);
            myChores.removeIf(c -> "COMPLETED".equals(c.getStatus()));
            dashboardChoresList.getItems().clear();
            dashboardChoresList.getItems().addAll(myChores);

            // Load unpaid bills
            List<doboard.core.models.BillDisplay> myBills = billDAO.getUserBills(dorm.getDorm_id(), user.getUser_id());
            myBills.removeIf(doboard.core.models.BillDisplay::isPaid);
            dashboardBillsList.getItems().clear();
            dashboardBillsList.getItems().addAll(myBills);
        } else {
            dashboardChoresList.getItems().clear();
            dashboardBillsList.getItems().clear();
            dashboardChoresList.getItems().add(new ChoreDisplay(-1, "No dorm yet. Join a dorm!", "", java.time.LocalDate.now(), "PENDING"));
        }
    }

    @FXML
    private void goToDormitory(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/dormitory-view.fxml", "DoBoard - My Dorm");
    }

    @FXML
    private void goToChores(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/chores-view.fxml", "DoBoard - Chores");
    }

    @FXML
    private void goToBills(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/bills-view.fxml", "DoBoard - Bills");
    }

    @FXML
    private void logout(ActionEvent event) {
        UserSession.getInstance().clearSession();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneLoader.loadScene(stage, "/com/doboard/view/login-view.fxml", "DoBoard - Login");
    }
}
