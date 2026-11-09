package bac.com.mosweu.ooad_project.dao;

import bac.com.mosweu.ooad_project.database.DatabaseInitializer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM Customer WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if a matching user was found

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
