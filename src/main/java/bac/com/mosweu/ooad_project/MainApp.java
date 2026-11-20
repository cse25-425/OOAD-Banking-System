package bac.com.mosweu.ooad_project;

import bac.com.mosweu.ooad_project.dao.AccountDAO;
import bac.com.mosweu.ooad_project.dao.CustomerDAO;
import java.sql.SQLException;
import java.time.YearMonth;
import javafx.scene.control.Alert;
import bac.com.mosweu.ooad_project.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Initializes the database and displays the application's login screen. */
public class MainApp extends Application {

    /** Prepares persistent storage before creating the first scene. */
    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseInitializer.initialize();
            new CustomerDAO().migrateLegacyPasswords();
            new AccountDAO().applyMonthlyInterestToAllAccounts(YearMonth.now());
        } catch (IOException | SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("The database could not be initialized.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            throw new RuntimeException("Database initialization failed.", e);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("Banking System");
        stage.setScene(scene);
        stage.show();
    }
}
