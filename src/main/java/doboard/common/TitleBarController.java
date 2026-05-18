package doboard.common;

import doboard.common.util.CustomTitleBar;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TitleBarController {
    @FXML private HBox topNavBar;
    @FXML private Label windowTitleLabel;

    private final CustomTitleBar titleBar = new CustomTitleBar();

    @FXML
    public void initialize() {
        titleBar.makeDraggable(topNavBar);
    }

    @FXML private void minimizeWindow(ActionEvent event) { titleBar.minimize(event); }
    @FXML private void maximizeWindow(ActionEvent event) { titleBar.maximize(event); }
    @FXML private void closeWindow(ActionEvent event) { titleBar.close(event); }

    public Label getWindowTitleLabel() {
        return windowTitleLabel;
    }
}