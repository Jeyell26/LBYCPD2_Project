package AdminView.AdminTools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminTools extends Application {
    Firebase.FirebaseService firebase = new Firebase.FirebaseService();

    @Override
    public void start(Stage primaryStage) throws Exception{
        firebase.initialize();
        Parent root = FXMLLoader.load(getClass().getResource("/AdminTools.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
