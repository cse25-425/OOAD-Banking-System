package bac.com.mosweu.ooad_project.view;

import bac.com.mosweu.ooad_project.Launcher;
import bac.com.mosweu.ooad_project.dao.AccountDAO;
import bac.com.mosweu.ooad_project.dao.AccountDAO.AccountRecord;
import bac.com.mosweu.ooad_project.dao.AccountDAO.TransactionRecord;
import bac.com.mosweu.ooad_project.dao.CustomerDAO;
import bac.com.mosweu.ooad_project.service.AccountService;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

/** Loads customer balances and handles dashboard actions. */
public class AccountController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalBalanceLabel;

    @FXML
    private Label savingsBalanceLabel;

    @FXML
    private Label investmentBalanceLabel;

    @FXML
    private Label chequeBalanceLabel;

    private final AccountDAO accountDAO = new AccountDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountService accountService = new AccountService();
    private String currentCustomerId;

    /** Binds this controller to the authenticated customer and loads their balances. */
    public void loadCustomer(String customerId) throws SQLException {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }

        currentCustomerId = customerId;
        String customerName = customerDAO.getFullName(currentCustomerId);
        welcomeLabel.setText("Welcome to your Dashboard, " + (customerName == null ? "Customer" : customerName) + "!");
        refreshBalances();
    }

    /** Refreshes the dashboard totals using database balances for the current customer. */
    private void refreshBalances() throws SQLException {
        BigDecimal savings = accountDAO.getBalance(currentCustomerId, "Savings");
        BigDecimal investment = accountDAO.getBalance(currentCustomerId, "Investment");
        BigDecimal cheque = accountDAO.getBalance(currentCustomerId, "Cheque");

        savingsBalanceLabel.setText(formatCurrency(savings));
        investmentBalanceLabel.setText(formatCurrency(investment));
        chequeBalanceLabel.setText(formatCurrency(cheque));
        totalBalanceLabel.setText(formatCurrency(savings.add(investment).add(cheque)));
    }

    /** Formats a monetary amount with the dashboard's BWP currency label. */
    private String formatCurrency(BigDecimal amount) {
        return String.format(Locale.ROOT, "BWP %,.2f", amount);
    }

    /** Shows transaction records belonging to the currently authenticated customer. */
    @FXML
    private void handleTransactionHistory() {
        try {
            var transactions = accountDAO.getTransactionHistory(currentCustomerId);
            if (transactions.isEmpty()) {
                showMessage("Transaction History", "No transactions have been recorded.");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
            StringBuilder history = new StringBuilder();
            for (TransactionRecord transaction : transactions) {
                history.append(transaction.createdAt().toLocalDateTime().format(formatter))
                        .append(" | ")
                        .append(transaction.transactionType())
                        .append(" | ")
                        .append(transaction.accountNumber())
                        .append(" | ")
                        .append(formatCurrency(transaction.amount()))
                        .append(System.lineSeparator());
            }
            showMessage("Transaction History", history.toString());
        } catch (SQLException e) {
            showError("Database Error", "Could not load transaction history: " + e.getMessage());
        }
    }

    /** Returns to the login screen. */
    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Navigation Error", e.getMessage());
        }
    }

    /** Starts a persisted deposit flow for an account owned by the current customer. */
    @FXML
    private void handleDeposit() {
        performTransaction(false);
    }

    /** Starts a persisted withdrawal flow for an account owned by the current customer. */
    @FXML
    private void handleWithdraw() {
        performTransaction(true);
    }

    /** Opens an account for the signed-in customer after collecting type-specific details. */
    @FXML
    private void handleOpenAccount() {
        List<String> types = List.of("Savings", "Investment", "Cheque");
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>(types.get(0), types);
        typeDialog.setTitle("Open Account");
        typeDialog.setHeaderText(null);
        typeDialog.setContentText("Choose an account type:");
        Optional<String> selectedType = typeDialog.showAndWait();
        if (selectedType.isEmpty()) {
            return;
        }

        String accountNumber = prompt("Open Account", "Enter a unique account number:");
        if (accountNumber == null) {
            return;
        }
        String branch = prompt("Open Account", "Enter the branch:");
        if (branch == null) {
            return;
        }
        String openingBalanceText = prompt("Open Account", "Enter the opening deposit in BWP:");
        if (openingBalanceText == null) {
            return;
        }

        String employerName = null;
        String employerAddress = null;
        if ("Cheque".equals(selectedType.get())) {
            employerName = prompt("Employment Details", "Enter employer/company name:");
            if (employerName == null) {
                return;
            }
            employerAddress = prompt("Employment Details", "Enter employer/company address:");
            if (employerAddress == null) {
                return;
            }
        }

        try {
            BigDecimal openingBalance = new BigDecimal(openingBalanceText);
            accountService.openAccount(
                    accountNumber,
                    currentCustomerId,
                    selectedType.get(),
                    branch,
                    employerName,
                    employerAddress,
                    openingBalance);
            refreshBalances();
            showMessage("Account Opened", "The account was created and its opening deposit was recorded.");
        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Enter a valid opening balance.");
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException | SQLException e) {
            showError("Account Creation Failed", e.getMessage());
        }
    }

    /** Prompts for an account and amount, performs the transaction, then refreshes balances. */
    private void performTransaction(boolean withdrawal) {
        String accountNumber = prompt("Account number", "Enter the account number:");
        if (accountNumber == null) {
            return;
        }

        String amountText = prompt("Amount", "Enter the amount in BWP:");
        if (amountText == null) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);

            accountService.transact(accountNumber, currentCustomerId, amount, withdrawal);

            showMessage("Success", withdrawal ? "Withdrawal saved." : "Deposit saved.");

            try {
                refreshBalances();
            } catch (SQLException e) {
                showError(
                        "Refresh Error",
                        "The transaction was saved, but balances could not be refreshed: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Enter a valid monetary amount.");
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
            showError("Transaction Rejected", e.getMessage());
        } catch (SQLException e) {
            showError("Transaction Failed", e.getMessage());
        }
    }

    /** Displays a text prompt and returns its trimmed value, or {@code null} if cancelled. */
    private String prompt(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            return null;
        }
        return result.get().trim();
    }

    /** Displays an informational message in a JavaFX alert. */
    private void showMessage(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /** Displays an error message in a JavaFX alert. */
    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /** Creates and displays an alert with the requested severity and message. */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "An unexpected error occurred." : message);
        alert.showAndWait();
    }
}
