package doboard.common.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.IOException;

public class ComponentFactory {
    public static Node createNotification(String message, String time){
        try{
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/common/notif-item.fxml"));
            Node node = loader.load();

            Label msg = (Label) node.lookup("#messageLabel");
            Label t = (Label) node.lookup("#timeLabel");
            if(msg != null) msg.setText(message);
            if(t != null) t.setText(time);

            return node;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Node createChoreItem(String title, Runnable onDoneAction) {
        try{
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/dashboard/chore-item.fxml"));
            Node node = loader.load();

            Label titleLabel = (Label) node.lookup("#choreTitleLabel");
            Button doneBtn = (Button) node.lookup("#doneBtn");
            if(titleLabel != null) titleLabel.setText(title);
            if(doneBtn != null){
                doneBtn.setOnAction(e -> onDoneAction.run());
            }
            return node;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Node createExpenseAlert(String message){
        try{
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/dashboard/expense-item.fxml"));
            Node node = loader.load();

            Label label = (Label) node.lookup("#alertLabel");
            if(label != null) label.setText("• " + message);

            return node;
        }catch(IOException e){
            e.printStackTrace();;
            return null;
        }
    }

    public static Node createLeaderboardRow(String username, int count, String imagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/common/leaderboard-item.fxml"));
            Node node = loader.load();

            ImageView profileImage = (ImageView) node.lookup("#profileImage");
            Label usernameLabel = (Label) node.lookup("#usernameVal");
            Label countLabel = (Label) node.lookup("#choresCount");
            if (usernameLabel != null) usernameLabel.setText(username);
            if (countLabel != null) countLabel.setText(String.valueOf(count));
            if (profileImage != null) {
                // Default to a placeholder if imagePath is null or empty
                String path = (imagePath == null || imagePath.isEmpty()) ? "/images/background.png" : imagePath;
                profileImage.setImage(new Image(ComponentFactory.class.getResourceAsStream(path)));

                // 10 is half of the 20.0 fitHeight/fitWidth
                Circle clip = new Circle(10, 10, 10);
                profileImage.setClip(clip);
            }

            return node;
        } catch (IOException | NullPointerException e) {
            System.err.println("Error loading leaderboard row component.");
            e.printStackTrace();
            return null;
        }
    }

    public static Node createDueBill(String title, double amount){
        try{
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/expenses/duebill-item.fxml"));
            Node node = loader.load();
            ((Label) node.lookup("#billLabel")).setText(title);
            ((Label) node.lookup("#amountLabel")).setText(String.format("₱%.2f", amount));

            return node;
        }catch(IOException e){
            return null;
        }
    }

    public static Node createProcessedBill(String date){
        try{
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/expenses/processedbill-item.fxml"));
            Node node = loader.load();

            Label dateLbl = (Label) node.lookup("#dateLabel");
            if(dateLbl != null) dateLbl.setText(date);

            return node;
        }catch(IOException e){
            return null;
        }
    }

    public static Node createTransactionItem(String title, String date, double amount){
        try {
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/expenses/transaction-item.fxml"));
            Node node = loader.load();

            Label titleLbl = (Label) node.lookup("#transactionTitle");
            Label dateLbl = (Label) node.lookup("#transactionDate");
            Label amountLbl = (Label) node.lookup("#transactionAmount");
            if (titleLbl != null) titleLbl.setText("Paid: " + title);
            if (dateLbl != null) dateLbl.setText("Completed on " + date);
            if (amountLbl != null) {
                amountLbl.setText(String.format("-₱%,.2f", amount));
            }

            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node createSpaceItem(String name){
        try{
            FXMLLoader loader = new FXMLLoader(ComponentFactory.class.getResource("/doboard/dashboard/space-item.fxml"));
            Node node = loader.load();

            Label spaceLbl = (Label) node.lookup("#spaceLabel");
            if(spaceLbl != null) spaceLbl.setText(name);

            return node;
        }catch(IOException e){
            return null;
        }
    }
}
