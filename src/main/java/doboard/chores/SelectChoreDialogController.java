package doboard.chores;

import doboard.auth.User;
import doboard.common.session.SessionHandler;
import doboard.dorm.DormDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class SelectChoreDialogController {

    @FXML private ListView<String> choresListView;
    @FXML private Button cancelButton;
    @FXML private Button selectButton;

    private final ChoreDAO choreDAO = new ChoreDAO();
    private final DormDAO dormDAO = new DormDAO();

    private List<Chore> availableChores;
    private ObservableList<String> choreDisplayNames = FXCollections.observableArrayList();
    private Consumer<Chore> onChoreSelected;

    @FXML
    public void initialize() {
        loadChores();
        choresListView.setItems(choreDisplayNames);
    }

    private void loadChores() {
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int dormId = dormDAO.getDormIdByUserId(currentUser.getUser_id());
        if (dormId == -1) return;

        availableChores = choreDAO.findAllByDormId(dormId);
        choreDisplayNames.clear();

        for (Chore chore : availableChores) {
            String displayName = chore.getTitle() + " - " + chore.getStatus().name() +
                               " (Due: " + chore.getDue_date() + ")";
            choreDisplayNames.add(displayName);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    @FXML
    private void handleSelect(ActionEvent event) {
        int selectedIndex = choresListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < availableChores.size()) {
            Chore selectedChore = availableChores.get(selectedIndex);
            if (onChoreSelected != null) {
                onChoreSelected.accept(selectedChore);
            }
            closeDialog();
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setOnChoreSelected(Consumer<Chore> onChoreSelected) {
        this.onChoreSelected = onChoreSelected;
    }
}