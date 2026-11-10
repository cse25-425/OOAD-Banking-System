package bac.com.mosweu.ooad_project.view;

import bac.com.mosweu.ooad_project.dao.CustomerDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import bac.com.mosweu.ooad_project.Launcher;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please enter both username and password.");
            alert.showAndWait();
            return;
        }

        CustomerDAO dao = new CustomerDAO();
        boolean isVaild = dao.validateLogin(username, password);

        if (isVaild) {
            System.out.println("Login Successful!");

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("account-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 400, 300);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password.");
            alert.showAndWait();
        }
    }
}
