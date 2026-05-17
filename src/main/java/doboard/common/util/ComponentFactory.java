package doboard.common.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
}
