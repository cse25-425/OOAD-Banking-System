package bac.com.mosweu.ooad_project.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import bac.com.mosweu.ooad_project.Launcher;
import java.io.IOException;

public class AccountController {

    @FXML
    private Label welcomeLabel;

    public void initialize() {
        // I can later change this to grab the actual customer's name
        welcomeLabel.setText("Welcome to your Dashboard, Ogone!");
    }

    @FXML
    protected void handleLogout() {
        try {
            // Load the login screen again
            FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}