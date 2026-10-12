package bac.com.mosweu.ooad_project.model;

public class InvestmentAccount extends Account {
    private final double INTEREST_RATE = 0.05; // 5%
    private final double MINIMUM_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, String branch, double initialBalance) {
        super(accountNumber, branch, initialBalance);
        if (initialBalance < MINIMUM_DEPOSIT) {
            throw new IllegalArgumentException("Investment Account requires a minimum initial deposit of BWP 500.00");
        }
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= getBalance()) {
            // Logic to deduct amount
            System.out.println("Withdrawn BWP " + amount + " from Investment Account.");
        } else {
            System.out.println("Insufficient funds.");
        }
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}