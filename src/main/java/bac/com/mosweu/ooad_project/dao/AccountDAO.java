package bac.com.mosweu.ooad_project.dao;

import bac.com.mosweu.ooad_project.database.DatabaseInitializer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

    public double getBalance(String customerId, String accountType) {
        String query = "SELECT balance FROM Account WHERE customerId = ? AND accountType = ?";
        double balance = 0.0;

        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, customerId);
            pstmt.setString(2, accountType);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }
}
