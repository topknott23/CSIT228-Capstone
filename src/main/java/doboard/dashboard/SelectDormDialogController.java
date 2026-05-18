package doboard.dashboard;

import doboard.dorm.Dorm;
import doboard.dorm.DormService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class SelectDormDialogController {

    @FXML private TextField searchField;
    @FXML private ListView<String> dormsListView;
    @FXML private Button cancelButton;

    private List<Dorm> allDorms;
    private final ObservableList<String> displayedDorms = FXCollections.observableArrayList();
    private Consumer<Dorm> onDormSelected;
    private final DormService dormService = new DormService();

    @FXML
    public void initialize() {
        allDorms = dormService.getAllDorms();
        dormsListView.setItems(displayedDorms);
        updateList("");

        // Add a listener to the search field to filter the list dynamically
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateList(newValue));
    }

    private void updateList(String filter) {
        displayedDorms.clear();
        String lowerFilter = filter.toLowerCase();

        for (Dorm d : allDorms) {
            String displayName = d.getDorm_name() + " (" + d.getJoin_code() + ")";
            if (displayName.toLowerCase().contains(lowerFilter)) {
                displayedDorms.add(displayName);
            }
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSelect() {
        int selectedIdx = dormsListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx >= 0) {
            String selectedStr = displayedDorms.get(selectedIdx);

            // Find the matching Dorm object from the original list
            Dorm selectedDorm = allDorms.stream()
                    .filter(d -> (d.getDorm_name() + " (" + d.getJoin_code() + ")").equals(selectedStr))
                    .findFirst()
                    .orElse(null);

            if (selectedDorm != null && onDormSelected != null) {
                onDormSelected.accept(selectedDorm);
            }
            handleCancel();
        }
    }

    public void setOnDormSelected(Consumer<Dorm> onDormSelected) {
        this.onDormSelected = onDormSelected;
    }
}