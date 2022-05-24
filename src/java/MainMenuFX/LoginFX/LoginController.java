package MainMenuFX.LoginFX;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    TextField user, showPass;

    @FXML
    PasswordField pass;

    @FXML
    Button login,back;

    @FXML
    Label error;

    @FXML
    CheckBox showToggle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        user.setText("Hi");
    }
}
