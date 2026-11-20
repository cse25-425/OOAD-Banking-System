package bac.com.mosweu.ooad_project.dao;

import bac.com.mosweu.ooad_project.database.DatabaseInitializer;
import bac.com.mosweu.ooad_project.model.Account;
import bac.com.mosweu.ooad_project.model.InvestmentAccount;
import bac.com.mosweu.ooad_project.model.SavingsAccount;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Performs account and transaction reads and writes against the H2 database. */
public class AccountDAO {

    /** A persisted deposit, withdrawal, or interest credit shown in transaction history. */
    public record TransactionRecord(
            String accountNumber, String transactionType, BigDecimal amount, Timestamp createdAt) {}

    /** A summary of an account available to its owning customer. */
    public record AccountRecord(
            String accountNumber,
            String accountType,
            BigDecimal balance,
            String branch,
            String employerName,
            String employerAddress,
            String status) {}

    /** Returns the combined balance of a customer's accounts of the requested type. */
    public BigDecimal getBalance(String customerId, String accountType) throws SQLException {
        String query = """
            SELECT COALESCE(SUM(balance), 0) AS balance
            FROM Account
            WHERE customerId = ? AND accountType = ?
            """;

        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, customerId);
            pstmt.setString(2, accountType);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("balance");
            }
        }
    }

    /** Retrieves a customer's transactions, newest first. */
    public List<TransactionRecord> getTransactionHistory(String customerId) throws SQLException {
        String query = """
            SELECT t.accountNumber, t.transactionType, t.amount, t.createdAt
            FROM BankTransaction t
            JOIN Account a ON a.accountNumber = t.accountNumber
            WHERE a.customerId = ?
            ORDER BY t.createdAt DESC, t.transactionId DESC
            """;
        List<TransactionRecord> transactions = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionRecord(
                            rs.getString("accountNumber"),
                            rs.getString("transactionType"),
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("createdAt")));
                }
            }
        }

        return transactions;
    }

    /** Lists all accounts belonging to a customer. */
    public List<AccountRecord> getAccounts(String customerId) throws SQLException {
        String query = """
            SELECT accountNumber, accountType, balance, branch, employerName, employerAddress, status
            FROM Account
            WHERE customerId = ?
            ORDER BY accountNumber
            """;
        List<AccountRecord> accounts = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new AccountRecord(
                            rs.getString("accountNumber"),
                            rs.getString("accountType"),
                            rs.getBigDecimal("balance"),
                            rs.getString("branch"),
                            rs.getString("employerName"),
                            rs.getString("employerAddress"),
                            rs.getString("status")));
                }
            }
        }
        return accounts;
    }

    /** Finds one account only when it belongs to the supplied customer. */
    public AccountRecord getAccount(String accountNumber, String customerId) throws SQLException {
        String query = """
            SELECT accountNumber, accountType, balance, branch, employerName, employerAddress, status
            FROM Account
            WHERE accountNumber = ? AND customerId = ?
            """;
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AccountRecord(
                        rs.getString("accountNumber"),
                        rs.getString("accountType"),
                        rs.getBigDecimal("balance"),
                        rs.getString("branch"),
                        rs.getString("employerName"),
                        rs.getString("employerAddress"),
                        rs.getString("status"));
            }
        }
    }

    /** Inserts a new account without replacing an existing account record. */
    public void createAccount(String accountNumber, String customerId, String accountType, BigDecimal openingBalance)
            throws SQLException {
        createAccount(accountNumber, customerId, accountType, null, null, null, openingBalance);
    }

    /** Validates account-specific opening rules and saves the account and opening deposit atomically. */
    public void createAccount(
            String accountNumber,
            String customerId,
            String accountType,
            String branch,
            String employerName,
            String employerAddress,
            BigDecimal openingBalance)
            throws SQLException {
        requireText(accountNumber, "Account number");
        requireText(customerId, "Customer ID");
        requireText(accountType, "Account type");
        if (openingBalance == null || openingBalance.signum() < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative.");
        }
        if ("Investment".equals(accountType) && openingBalance.compareTo(new BigDecimal("500.00")) < 0) {
            throw new IllegalArgumentException(
                    "Investment accounts require an opening balance of at least BWP 500.00.");
        }
        if (!List.of("Savings", "Investment", "Cheque").contains(accountType)) {
            throw new IllegalArgumentException("Unsupported account type.");
        }
        if ("Cheque".equals(accountType)
                && (employerName == null
                        || employerName.isBlank()
                        || employerAddress == null
                        || employerAddress.isBlank())) {
            throw new IllegalArgumentException("Employer name and address are required for a cheque account.");
        }

        String query = """
            INSERT INTO Account
                (accountNumber, customerId, accountType, branch, employerName, employerAddress, balance, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
            """;
        try (Connection conn = DriverManager.getConnection(
                DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, accountNumber);
                pstmt.setString(2, customerId);
                pstmt.setString(3, accountType);
                pstmt.setString(4, branch);
                pstmt.setString(5, employerName);
                pstmt.setString(6, employerAddress);
                pstmt.setBigDecimal(7, openingBalance);
                pstmt.executeUpdate();

                if (openingBalance.signum() > 0) {
                    try (PreparedStatement transaction = conn.prepareStatement("""
                        INSERT INTO BankTransaction (accountNumber, transactionType, amount)
                        VALUES (?, 'DEPOSIT', ?)
                        """)) {
                        transaction.setString(1, accountNumber);
                        transaction.setBigDecimal(2, openingBalance);
                        transaction.executeUpdate();
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

    /** Deletes an account owned by a customer; history references may prevent deletion. */
    public void deleteAccount(String accountNumber, String customerId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt =
                        conn.prepareStatement("DELETE FROM Account WHERE accountNumber = ? AND customerId = ?")) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, customerId);
            if (pstmt.executeUpdate() != 1) {
                throw new SQLException("Account was not found or does not belong to this customer.");
            }
        }
    }

    /**
     * Credits each eligible savings and investment account once for a month.
     * The unique transaction index also prevents duplicate monthly credits across concurrent logins.
     */
    public void applyMonthlyInterest(String customerId, YearMonth month) throws SQLException {
        String accountsQuery = """
            SELECT accountNumber, accountType, balance
            FROM Account
            WHERE customerId = ? AND accountType IN ('Savings', 'Investment') AND status = 'ACTIVE'
            FOR UPDATE
            """;
        try (Connection conn = DriverManager.getConnection(
                DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement accounts = conn.prepareStatement(accountsQuery);
                    PreparedStatement alreadyPaid = conn.prepareStatement("""
                        SELECT 1 FROM BankTransaction
                        WHERE accountNumber = ? AND transactionType = 'INTEREST' AND interestMonth = ?
                        """);
                    PreparedStatement update = conn.prepareStatement("""
                        UPDATE Account SET balance = balance + ?
                        WHERE accountNumber = ? AND customerId = ? AND status = 'ACTIVE'
                        """);
                    PreparedStatement insert = conn.prepareStatement("""
                        INSERT INTO BankTransaction
                            (accountNumber, transactionType, amount, interestMonth)
                        VALUES (?, 'INTEREST', ?, ?)
                        """)) {
                accounts.setString(1, customerId);
                try (ResultSet rs = accounts.executeQuery()) {
                    while (rs.next()) {
                        String number = rs.getString("accountNumber");
                        alreadyPaid.setString(1, number);
                        alreadyPaid.setDate(2, java.sql.Date.valueOf(month.atDay(1)));
                        try (ResultSet existing = alreadyPaid.executeQuery()) {
                            if (existing.next()) {
                                continue;
                            }
                        }

                        BigDecimal balance = rs.getBigDecimal("balance");
                        Account interestAccount = "Savings".equals(rs.getString("accountType"))
                                ? new SavingsAccount(number, null, balance.doubleValue())
                                : InvestmentAccount.restore(number, null, balance.doubleValue());
                        BigDecimal interest =
                                interestAccount.calculateInterest(balance).setScale(2, RoundingMode.HALF_UP);
                        if (interest.signum() == 0) {
                            continue;
                        }

                        update.setBigDecimal(1, interest);
                        update.setString(2, number);
                        update.setString(3, customerId);
                        if (update.executeUpdate() != 1) {
                            throw new SQLException("Could not apply interest to account " + number + ".");
                        }
                        insert.setString(1, number);
                        insert.setBigDecimal(2, interest);
                        insert.setDate(3, java.sql.Date.valueOf(month.atDay(1)));
                        insert.executeUpdate();
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

    /** Applies the month's interest for every customer; repeat runs are idempotent. */
    public void applyMonthlyInterestToAllAccounts(YearMonth month) throws SQLException {
        List<String> customerIds = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement query = conn.prepareStatement(
                        "SELECT DISTINCT customerId FROM Account WHERE accountType IN ('Savings', 'Investment')");
                ResultSet rs = query.executeQuery()) {
            while (rs.next()) {
                customerIds.add(rs.getString("customerId"));
            }
        }

        for (String customerId : customerIds) {
            applyMonthlyInterest(customerId, month);
        }
    }

    /** Updates an account's branch and employer details without changing its owner or balance. */
    public void updateAccountDetails(
            String accountNumber, String customerId, String branch, String employerName, String employerAddress)
            throws SQLException {
        String sql = """
            UPDATE Account
            SET branch = ?, employerName = ?, employerAddress = ?
            WHERE accountNumber = ? AND customerId = ?
            """;
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, branch);
            pstmt.setString(2, employerName);
            pstmt.setString(3, employerAddress);
            pstmt.setString(4, accountNumber);
            pstmt.setString(5, customerId);
            if (pstmt.executeUpdate() != 1) {
                throw new SQLException("Account was not found or does not belong to this customer.");
            }
        }
    }

    /** Applies a valid lifecycle transition to an account owned by the customer. */
    public void updateAccountStatus(String accountNumber, String customerId, String action) throws SQLException {
        String sql = switch (action) {
            case "SUSPEND" -> """
                UPDATE Account SET status = 'SUSPENDED'
                WHERE accountNumber = ? AND customerId = ? AND status = 'ACTIVE'
                """;
            case "REACTIVATE" -> """
                UPDATE Account SET status = 'ACTIVE'
                WHERE accountNumber = ? AND customerId = ? AND status = 'SUSPENDED'
                """;
            case "CLOSE" -> """
                UPDATE Account SET status = 'CLOSED'
                WHERE accountNumber = ? AND customerId = ?
                  AND status IN ('ACTIVE', 'SUSPENDED') AND balance = 0
                """;
            default -> throw new IllegalArgumentException("Unsupported account status action.");
        };
        try (Connection conn = DriverManager.getConnection(
                        DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, customerId);
            if (pstmt.executeUpdate() != 1) {
                throw new SQLException("Account was not found or the requested status transition is not allowed.");
            }
        }
    }

    /** Deposits a positive amount and records it in the same database transaction. */
    public void deposit(String accountNumber, String customerId, BigDecimal amount) throws SQLException {
        changeBalance(accountNumber, customerId, amount, false);
    }

    /** Withdraws a permitted amount and records it atomically with the balance update. */
    public void withdraw(String accountNumber, String customerId, BigDecimal amount) throws SQLException {
        changeBalance(accountNumber, customerId, amount, true);
    }

    /** Applies a balance change only to the owner's account and rolls back if recording fails. */
    private void changeBalance(String accountNumber, String customerId, BigDecimal amount, boolean withdrawal)
            throws SQLException {

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        String updateQuery = withdrawal ? """
            UPDATE Account
            SET balance = balance - ?
            WHERE accountNumber = ? AND customerId = ?
              AND status = 'ACTIVE' AND accountType <> 'Savings' AND balance >= ?
            """ : """
                UPDATE Account
                SET balance = balance + ?
                WHERE accountNumber = ? AND customerId = ? AND status = 'ACTIVE'
                """;

        String transactionQuery = """
            INSERT INTO BankTransaction (accountNumber, transactionType, amount)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(
                DatabaseInitializer.URL, DatabaseInitializer.USER, DatabaseInitializer.PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement update = conn.prepareStatement(updateQuery);
                    PreparedStatement transaction = conn.prepareStatement(transactionQuery)) {

                update.setBigDecimal(1, amount);
                update.setString(2, accountNumber);
                update.setString(3, customerId);
                if (withdrawal) {
                    update.setBigDecimal(4, amount);
                }

                if (update.executeUpdate() != 1) {
                    throw new SQLException("Account was not updated. Check that it exists, belongs to this customer, "
                            + "and has enough funds. Savings withdrawals are not allowed.");
                }

                transaction.setString(1, accountNumber);
                transaction.setString(2, withdrawal ? "WITHDRAWAL" : "DEPOSIT");
                transaction.setBigDecimal(3, amount);
                transaction.executeUpdate();

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

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }
}
