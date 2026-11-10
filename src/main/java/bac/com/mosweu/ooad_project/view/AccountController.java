package bac.com.mosweu.ooad_project.view;

import bac.com.mosweu.ooad_project.dao.AccountDAO;
import bac.com.mosweu.ooad_project.dao.CustomerDAO;
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

    @FXML
    private Label totalBalanceLabel;

    @FXML
    private Label savingsBalanceLabel;

    @FXML
    private Label investmentBalanceLabel;

    private AccountDAO accountDAO = new AccountDAO();
    private CustomerDAO customerDAO = new CustomerDAO();

    public void initialize() {
        welcomeLabel.setText("Welcome to your Dashboard, Ogone!");

        // 1. Fetch the customer ID for 'admin' from the database
        String currentCustomerId = customerDAO.getCustomerIdByUsername("admin");

        if (currentCustomerId != null) {
            // 2. Fetch real balances from the database
            double savings = accountDAO.getBalance(currentCustomerId, "Savings");
            double investment = accountDAO.getBalance(currentCustomerId, "Investment");
            double total = savings + investment;

            // 3. Update the GUI labels dynamically
            savingsBalanceLabel.setText(String.format("BWP %.2f", savings));
            investmentBalanceLabel.setText(String.format("BWP %.2f", investment));
            totalBalanceLabel.setText(String.format("BWP %.2f", total));
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
