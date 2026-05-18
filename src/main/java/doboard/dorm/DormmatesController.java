package doboard.dorm;

import doboard.auth.User;
import doboard.auth.UserDAO;
import doboard.common.session.SessionHandler;
import doboard.common.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DormmatesController {

    @FXML private TableView<User> dormmatesTable;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;

    private final DormDAO dormDAO = new DormDAO();

    @FXML
    public void initialize() {
        NavigationManager.setTitle("Dormmates");

        nameCol.setCellValueFactory(new PropertyValueFactory<>("full_name"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadDormmates();
    }

    private void loadDormmates() {
        User currentUser = SessionHandler.loadSession();
        if (currentUser == null) return;

        int dormId = dormDAO.getDormIdByUserId(currentUser.getUser_id());
        if (dormId == -1) return;

        List<DormMember> members = dormDAO.getMembersByDorm(dormId);
        List<Integer> userIds = members.stream()
                .map(DormMember::getUser_id)
                .collect(Collectors.toList());

        List<User> users = UserDAO.getUsersByIds(userIds);
        ObservableList<User> tableData = FXCollections.observableArrayList(users);
        dormmatesTable.setItems(tableData);
    }
}