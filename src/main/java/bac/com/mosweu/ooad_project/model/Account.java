package bac.com.mosweu.ooad_project.model;

import java.math.BigDecimal;

/** Base in-memory representation shared by the bank's account types. */
public abstract class Account implements ITransaction {
    // Encapsulation: Private attributes
    private String accountNumber;
    private double balance;
    private String branch;
    private AccountStatus status = AccountStatus.NEW;

    /** Creates an account model with its identifying details and opening balance. */
    public Account(String accountNumber, String branch, double initialBalance) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required.");
        }
        if (!Double.isFinite(initialBalance) || initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.balance = initialBalance;
    }

    @Override
    public void deposit(double amount) {
        requireActive();
        requirePositiveAmount(amount);
        this.balance += amount;
    }

    /** Validates and deducts a withdrawal amount for account types that allow withdrawals. */
    protected void deduct(double amount) {
        requireActive();
        requirePositiveAmount(amount);
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds.");
        }
        balance -= amount;
    }

    /** Moves a newly created account into service. */
    public void activate() {
        requireStatus(AccountStatus.NEW, "Only a new account can be activated.");
        status = AccountStatus.ACTIVE;
    }

    /** Suspends an active account so it cannot accept transactions. */
    public void suspend() {
        requireStatus(AccountStatus.ACTIVE, "Only an active account can be suspended.");
        status = AccountStatus.SUSPENDED;
    }

    /** Reactivates a suspended account. */
    public void reactivate() {
        requireStatus(AccountStatus.SUSPENDED, "Only a suspended account can be reactivated.");
        status = AccountStatus.ACTIVE;
    }

    /** Closes an active or suspended account only after its balance reaches zero. */
    public void close() {
        if (status != AccountStatus.ACTIVE && status != AccountStatus.SUSPENDED) {
            throw new IllegalStateException("Only an active or suspended account can be closed.");
        }
        if (balance != 0) {
            throw new IllegalStateException("An account must have a zero balance before it can be closed.");
        }
        status = AccountStatus.CLOSED;
    }

    private void requireActive() {
        requireStatus(AccountStatus.ACTIVE, "Transactions require an active account.");
    }

    private void requireStatus(AccountStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    private static void requirePositiveAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    /** Returns the account's identifying number. */
    public String getAccountNumber() {
        return accountNumber;
    }

    /** Returns this model's current in-memory balance. */
    public double getBalance() {
        return balance;
    }

    /** Returns the branch associated with the account. */
    public String getBranch() {
        return branch;
    }

    /** Returns the account's current lifecycle state. */
    public AccountStatus getStatus() {
        return status;
    }

    /** Implements the withdrawal policy for a concrete account type. */
    @Override
    public abstract void withdraw(double amount);

    /** Calculates interest for this account type without applying it to the balance. */
    public abstract double calculateInterest();

    /** Calculates currency-safe interest for a supplied principal. */
    public abstract BigDecimal calculateInterest(BigDecimal principal);
}
