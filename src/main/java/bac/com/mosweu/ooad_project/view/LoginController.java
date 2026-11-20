package bac.com.mosweu.ooad_project.view;

import bac.com.mosweu.ooad_project.Launcher;
import bac.com.mosweu.ooad_project.dao.CustomerDAO;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Handles credential validation and navigation from the login screen. */
public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /** Authenticates the form values and opens the dashboard for the matched customer. */
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

        try {
            String customerId = new CustomerDAO().authenticate(username, password);
            if (customerId == null) {
                showLoginFailed();
                return;
            }

            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("account-view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 550);
            AccountController controller = loader.getController();
            controller.loadCustomer(customerId);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (SQLException e) {
            showDatabaseError(e);
        } catch (IOException e) {
            showError("Navigation Error", "The dashboard could not be opened: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("registration-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 480));
        } catch (IOException e) {
            showError("Navigation Error", "The registration screen could not be opened: " + e.getMessage());
        }
    }

    /** Displays the same generic response for credentials that do not match a customer. */
    private void showLoginFailed() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Failed");
        alert.setHeaderText(null);
        alert.setContentText("Invalid username or password.");
        alert.showAndWait();
    }

    /** Reports database failures separately from invalid credentials. */
    private void showDatabaseError(SQLException e) {
        showError("Database Error", "The login request could not be completed: " + e.getMessage());
    }

    /** Displays an application error message in a JavaFX alert. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
