package doboard.dashboard;

import doboard.common.util.ComponentFactory;
import doboard.common.util.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class ContentController {
    @FXML private VBox contentArea;
    @FXML private VBox systemNotifContainer;
    @FXML private VBox nudgeNotifContainer;
    @FXML private VBox dueChoreContainer;
    @FXML private VBox upcomingChoreContainer;
    @FXML private Label balanceValue;
    @FXML private VBox expensesAlertContainer;
    @FXML private Button signalBtnShrtct1;
    @FXML private Button signalBtnShrtct2;
    @FXML private Button signalBtnShrtct3;
    @FXML private Button signalBtnShrtct4;

    @FXML
    public void initialize(){
        refreshChoreList();

        // TODO: setup a timeline to refresh list every hour to check for data transitions
        // perhaps it's possible to use multithreading ani
    }

    @FXML private void goChores(){NavigationManager.loadView(getClass(), "/doboard/chores/chore-view.fxml");}
    @FXML private void goExpenses(){NavigationManager.loadView(getClass(), "/doboard/expenses/expenses-view.fxml");}
    @FXML private void goSignals(){NavigationManager.loadView(getClass(), "/doboard/signals/signals-view.fxml");}

    @FXML
    private void markAsDone(ActionEvent event){
        // TODO: update database
    }
    @FXML
    private void signalAct1(ActionEvent event){
        // TODO: send signal to user -> database (default receiver = all, default nudge = 1)
    }
    @FXML
    private void signalAct2(ActionEvent event){
        // TODO: send signal to user -> database (default receiver = all, default nudge = 1)
    }
    @FXML
    private void signalAct3(ActionEvent event){
        // TODO: send signal to user -> database (default receiver = all, default nudge = 1)
    }
    @FXML
    private void signalAct4(ActionEvent event){
        // TODO: send signal to user -> database (default receiver = all, default nudge = 1)
    }

    //Helpers
    // Diko sure sa if maghimo ba database sa notifications. Pwede rag di nlng tana e implement nga feature 100%
    public void addNotification(String msg, String time, boolean isNudge){
        Node notif = ComponentFactory.createNotification(msg, time);
        if(notif != null){
            if(isNudge){
                nudgeNotifContainer.getChildren().add(notif);
            }else{
                systemNotifContainer.getChildren().add(notif);
            }
        }
    }

    public void refreshChoreList(){
        dueChoreContainer.getChildren().clear();
        upcomingChoreContainer.getChildren().clear();

        // TODO: Fetch the real dorm_id from the active session to fetch all the task from database
    }
    public void addChore(VBox container, String title, boolean isDue){
        // TODO: Fix Logic
        // Intended Flow: if there is an upcomingChore in whatever day, it goes to dueChore when
        // the certain day is reached. For example, There is 1 upcomingChore on Tuesday
        // (Today is Monday), when it turns Tuesday. All the upcomingChores for Tusday should
        // go to dueChores (1 Chore in this instance).

        // Example code, change to proper types
        Node choreNode = ComponentFactory.createChoreItem(
                title, () -> {
                    markAsDone(null);
                });
        if(choreNode != null){
            // Hide done button for upcoming Chores, og ganahan mog feature nga pwede advance2
            // ky e disable lng.
            if(!isDue){
                Node doneBtn = choreNode.lookup("#doneBtn");
                if(doneBtn != null) doneBtn.setVisible(false);
            }
            container.getChildren().add(choreNode);
        }
    }

    public void addExpenseAlert(String message){
        Node alertNode = ComponentFactory.createExpenseAlert(message);
        if(alertNode != null) expensesAlertContainer.getChildren().add(alertNode);
    }
}
