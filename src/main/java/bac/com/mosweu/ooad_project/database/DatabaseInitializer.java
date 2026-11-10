package bac.com.mosweu.ooad_project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    // This creates a file called bankdb.mv.db in the project folder
    public static final String URL = "jdbc:h2:./bankdb;AUTO_SERVER=TRUE";
    public static final String USER = "sa";
    public static final String PASSWORD = "";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement()) {

            // Create Customer Table
            String createCustomerTable =
                    "CREATE TABLE IF NOT EXISTS Customer (" + "customerId VARCHAR(50) PRIMARY KEY, "
                            + "firstName VARCHAR(50), "
                            + "surname VARCHAR(50), "
                            + "username VARCHAR(50) UNIQUE, "
                            + "password VARCHAR(50))";
            stmt.execute(createCustomerTable);

            // Insert a test user (if they don't exist yet)
            String insertTestUser = "MERGE INTO Customer (customerId, firstName, surname, username, password) "
                    + "KEY(customerId) VALUES ('C001', 'Ogone', 'Mosweu', 'admin', '1234')";
            stmt.execute(insertTestUser);

            System.out.println("Database initialized successfully.");

            // Create Account Table
            String createAccountTable =
                    "CREATE TABLE IF NOT EXISTS Account (" + "accountNumber VARCHAR(50) PRIMARY KEY, "
                            + "customerId VARCHAR(50), "
                            + "accountType VARCHAR(50), "
                            + "balance DOUBLE, "
                            + "FOREIGN KEY (customerId) REFERENCES Customer(customerId))";
            stmt.execute(createAccountTable);

            // Insert a test Savings Account for admin (C001)
            String insertSavingsAccount = "MERGE INTO Account (accountNumber, customerId, accountType, balance) "
                    + "KEY(accountNumber) VALUES ('SA001', 'C001', 'Savings', 2000.00)";
            stmt.execute(insertSavingsAccount);

            // Insert a test Investment Account for admin (C001)
            String insertInvestmentAccount = "MERGE INTO Account (accountNumber, customerId, accountType, balance) "
                    + "KEY(accountNumber) VALUES ('IA001', 'C001', 'Investment', 3000.00)";
            stmt.execute(insertInvestmentAccount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
