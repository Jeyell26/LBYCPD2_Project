package MainMenuFX.LoginFX;

import Tools.Navigate;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class LoginController implements Initializable {

    Navigate x = new Navigate();

    Stage stage;

    // Username and Password textFields
    @FXML
    TextField user, showPass;

    @FXML
    CheckBox showToggle;

    @FXML
    PasswordField pass;

    @FXML
    Label error;

    @FXML
    Button login, back;

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("User Details");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    // Run for loading
    {
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        error.setWrapText(true);
        showPass.setVisible(false);
        showToggle.setOnAction(this::togglePass);
        login.setOnAction(e -> {
            boolean temp = false;
            stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
            try {
                temp = loginUser(user.getText(),getPass());
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
            if (temp){
                try {
                    error.setText(getType(user.getText()) + " login! Welcome " + user.getText());
                    error.setStyle("-fx-text-fill: green");
                } catch (ExecutionException | InterruptedException executionException) {
                    executionException.printStackTrace();
                }

            }
            this.user.clear();
            this.pass.clear();
            this.showPass.clear();
        });

        back.setOnAction(this::setBack);


    }

    private void togglePass(ActionEvent event){
        if(showToggle.isSelected()){
            // show the text
            showPass.setText(pass.getText());
            showPass.setVisible(true);
            pass.setVisible(false);
            return;
        }
        pass.setText(showPass.getText());
        showPass.setVisible(false);
        pass.setVisible(true);
    }

    private String getPass(){
        if(showToggle.isSelected()){
            return showPass.getText();
        }
        return pass.getText();
    }

    private void setBack(ActionEvent e){
        x.switchScene(e,"main","Main Menu");
    }

    private void setLogin(ActionEvent e, String input) {
        x.switchScene(e,"menu","Menu", input);
    }

    // whether inputted user details matches
    private Boolean loginUser(String user, String pass) throws ExecutionException, InterruptedException {

        if(user.equals("")||pass.equals("")){
            error.setText("Please input username/password");
            error.setStyle("-fx-text-fill: red");
            return false;
        }


        docRef = cr.document(user);
        future = docRef.get();
        document = future.get();


        if (document.exists()) {
            if(pass.equals(document.getString("Pass"))) {
                return true;
            }
            else {
                System.out.println("Wrong Password");
                error.setText("Wrong Password");
                error.setStyle("-fx-text-fill: red");
                return false;
            }
        } else {
            System.out.println("User does not exist");
            error.setText("Username does not exist in our database");
            error.setStyle("-fx-text-fill: red");
            return false;
        }

    }

    private String getType(String user) throws ExecutionException, InterruptedException{
        docRef = cr.document(user);
        future = docRef.get();
        document = future.get();

        return document.getString("Type");

    }

}
