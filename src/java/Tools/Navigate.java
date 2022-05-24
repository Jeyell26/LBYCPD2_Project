package Tools;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Navigate {
    // This class is purely for convenience purposes

    // Switch scene with title
    public void switchScene(ActionEvent event, String filename, String title){
        Stage stage, currStage;
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/"+filename+".fxml")));
            currStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setUserData(currStage.getUserData());
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Switch scene with userData
    public void switchScene(ActionEvent event, String filename, String title, String data){
        Stage stage;
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/"+filename+".fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setUserData(data);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setUserData(data);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
