package bac.com.mosweu.ooad_project.dao;

import bac.com.mosweu.ooad_project.database.DatabaseInitializer;
import bac.com.mosweu.ooad_project.security.PasswordHasher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Performs customer authentication and CRUD operations against the H2 database. */
public class CustomerDAO {
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_MINUTES = 15;

    /**
     * Verifies credentials and returns the customer's ID when valid.
     * The third consecutive failure locks the account for fifteen minutes.
     */
    public String authenticate(String username, String password) throws SQLException {
        String query = """
            SELECT customerId, password, failedLoginAttempts, lockedUntil
            FROM Customer
            WHERE username = ?
            """;

        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String customerId = rs.getString("customerId");
                String storedPassword = rs.getString("password");
                int failedAttempts = rs.getInt("failedLoginAttempts");
                Timestamp lockedUntil = rs.getTimestamp("lockedUntil");
                if (lockedUntil != null && lockedUntil.toLocalDateTime().isAfter(LocalDateTime.now())) {
                    return null;
                }

                if (!PasswordHasher.matches(password, storedPassword)) {
                    recordFailedLogin(conn, customerId, failedAttempts, lockedUntil);
                    return null;
                }

                String storedHash =
                        PasswordHasher.isHashed(storedPassword) ? storedPassword : PasswordHasher.hash(password);
                try (PreparedStatement success = conn.prepareStatement("""
                    UPDATE Customer
                    SET password = ?, failedLoginAttempts = 0, lockedUntil = NULL
                    WHERE customerId = ?
                    """)) {
                    success.setString(1, storedHash);
                    success.setString(2, customerId);
                    success.executeUpdate();
                }
                return customerId;
            }
        }
    }

    /** Hashes any legacy plaintext passwords so inactive customer records are migrated too. */
    public void migrateLegacyPasswords() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement query = conn.prepareStatement("SELECT customerId, password FROM Customer");
                    ResultSet rs = query.executeQuery()) {
                while (rs.next()) {
                    String customerId = rs.getString("customerId");
                    String password = rs.getString("password");
                    if (PasswordHasher.isHashed(password)) {
                        continue;
                    }

                    try (PreparedStatement update = conn.prepareStatement(
                            "UPDATE Customer SET password = ? WHERE customerId = ? AND password = ?")) {
                        update.setString(1, PasswordHasher.hash(password));
                        update.setString(2, customerId);
                        update.setString(3, password);
                        update.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    private void recordFailedLogin(Connection conn, String customerId, int previousAttempts, Timestamp previousLock)
            throws SQLException {
        boolean lockExpired =
                previousLock != null && !previousLock.toLocalDateTime().isAfter(LocalDateTime.now());
        int nextAttempts = lockExpired ? 1 : previousAttempts + 1;
        Timestamp nextLock = nextAttempts >= MAX_FAILED_ATTEMPTS
                ? Timestamp.valueOf(LocalDateTime.now().plusMinutes(LOCK_MINUTES))
                : null;
        String sql = "UPDATE Customer SET failedLoginAttempts = ?, lockedUntil = ? WHERE customerId = ?";
        try (PreparedStatement update = conn.prepareStatement(sql)) {
            update.setInt(1, nextAttempts);
            update.setTimestamp(2, nextLock);
            update.setString(3, customerId);
            update.executeUpdate();
        }
    }

    /** Creates a customer with a salted password hash instead of storing the raw password. */
    public void createCustomer(
            String customerId, String firstName, String surname, String address, String username, String password)
            throws SQLException {
        requireText(customerId, "Customer ID");
        requireText(firstName, "First name");
        requireText(surname, "Surname");
        requireText(address, "Address");
        requireText(username, "Username");
        requireText(password, "Password");

        String query = """
            INSERT INTO Customer (customerId, firstName, surname, address, username, password)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customerId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, surname);
            pstmt.setString(4, address);
            pstmt.setString(5, username);
            pstmt.setString(6, PasswordHasher.hash(password));
            pstmt.executeUpdate();
        }
    }

    /** Returns the customer's full name, or {@code null} if the ID does not exist. */
    public String getFullName(String customerId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt =
                        conn.prepareStatement("SELECT firstName, surname FROM Customer WHERE customerId = ?")) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("firstName") + " " + rs.getString("surname") : null;
            }
        }
    }

    /** Updates a customer's contact details without changing their login credentials. */
    public void updateCustomer(String customerId, String firstName, String surname, String address)
            throws SQLException {
        String sql = """
            UPDATE Customer SET firstName = ?, surname = ?, address = ?
            WHERE customerId = ?
            """;
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, surname);
            pstmt.setString(3, address);
            pstmt.setString(4, customerId);
            requireOneUpdated(pstmt.executeUpdate(), "Customer was not found.");
        }
    }

    /** Deletes a customer; database referential integrity rejects deletion while accounts remain. */
    public void deleteCustomer(String customerId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Customer WHERE customerId = ?")) {
            pstmt.setString(1, customerId);
            requireOneUpdated(pstmt.executeUpdate(), "Customer was not found.");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }

    private static void requireOneUpdated(int rowCount, String message) throws SQLException {
        if (rowCount != 1) {
            throw new SQLException(message);
        }
    }
}
