package bac.com.mosweu.ooad_project.model;

public class SavingsAccount extends Account {
    private final double INTEREST_RATE = 0.0005; // 0.05%

    public SavingsAccount(String accountNumber, String branch, double initialBalance) {
        super(accountNumber, branch, initialBalance);
    }

    @Override
    public void withdraw(double amount) {
        // Assignment rule: Savings accounts DO NOT allow withdrawals
        throw new UnsupportedOperationException("Withdrawals are not permitted from a Savings Account.");
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}