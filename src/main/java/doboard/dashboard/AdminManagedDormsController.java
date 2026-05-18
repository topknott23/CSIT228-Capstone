package doboard.dashboard;

import doboard.dorm.Dorm;
import doboard.dorm.DormDAO;
import doboard.common.util.Popup;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminManagedDormsController {

    @FXML private VBox dormsListContainer;
    private final DormDAO dormDAO = new DormDAO();

    @FXML
    public void initialize() {
        loadDorms();
    }

    private void loadDorms() {
        dormsListContainer.getChildren().clear();
        List<Dorm> allDorms = dormDAO.findAllDorms();

        for (Dorm dorm : allDorms) {
            HBox row = new HBox(10);
            row.setStyle("-fx-alignment: center-left; -fx-padding: 15; -fx-background-color: #ffffff; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

            VBox infoBox = new VBox(4);


            String dName = dorm.getDorm_name();
            if (dName == null || dName.trim().isEmpty()) {
                dName = "Unnamed Dorm";
            }

            Label nameLbl = new Label(dName);

            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");

            Label codeLbl = new Label("Join Code: " + dorm.getJoin_code());
            codeLbl.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

            infoBox.getChildren().addAll(nameLbl, codeLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button nudgeBtn = new Button("Nudge Tenants");
            nudgeBtn.setStyle("-fx-background-color: #3F69AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
            nudgeBtn.setOnAction(e -> {
                Popup.show("Nudge Sent", "Sent a reminder to the tenants of " + dorm.getDorm_name());
            });

            row.getChildren().addAll(infoBox, spacer, nudgeBtn);
            dormsListContainer.getChildren().add(row);
        }
    }
}