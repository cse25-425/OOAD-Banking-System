package bac.com.mosweu.ooad_project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    // This creates a file called bankdb.mv.db in the project folder
    public static final String URL = "jdbc:h2:./bankdb";
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
