package AdminView;
import Tools.Navigate;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminViewController implements Initializable {

    @FXML
    private Label welcome;

    @FXML
    private Button inventory;

    @FXML
    private Button product;

    @FXML
    private Button adminTools;

    @FXML
    private Button graphView;

    @FXML
    private Button Back;

    Stage stage;
    // can be showed to show the name of current user
    // currently used as indicator of who is the current user logged in.

    Navigate x = new Navigate();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(this::setWelcome);
        //adminTools.setDisable(true); // To be implemented later on
        adminTools.setOnAction(e -> x.switchScene(e,"adminTools","Admin Tools Page"));
        Back.setOnAction(e -> x.switchScene(e,"login","Login Page"));
        inventory.setOnAction(e -> x.switchScene(e, "inventory", "Inventory Page"));
        product.setOnAction(e -> x.switchScene(e, "product", "Product Page"));
        graphView.setOnAction(e -> x.switchScene(e, "graph", "Graph Page"));
    }

    public void setWelcome(){
        stage = (Stage) welcome.getScene().getWindow();
        welcome.setWrapText(true);
        welcome.setText("Hi Administrator " + stage.getUserData()+ "!\n" +
                "What is our Agenda today?");
    }

}
