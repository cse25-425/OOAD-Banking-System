package bac.com.mosweu.ooad_project.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AccountController {
    @FXML
    private Label welcomeLabel;

    public void initialize() {
        // I will update this with the actual customer's name later
        welcomeLabel.setText("Welcome to your Dashboard!");
    }
}