package bac.com.mosweu.ooad_project.model;

import java.math.BigDecimal;

/** Savings account model with interest and a rule prohibiting withdrawals. */
public class SavingsAccount extends Account {
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.0005");

    /** Creates a savings account with its opening balance. */
    public SavingsAccount(String accountNumber, String branch, double initialBalance) {
        super(accountNumber, branch, initialBalance);
    }

    /** Savings accounts do not permit withdrawals. */
    @Override
    public void withdraw(double amount) {
        // Assignment rule: Savings accounts DO NOT allow withdrawals
        throw new UnsupportedOperationException("Withdrawals are not permitted from a Savings Account.");
    }

    /** Calculates the interest amount using the configured savings rate. */
    @Override
    public double calculateInterest() {
        return calculateInterest(BigDecimal.valueOf(getBalance())).doubleValue();
    }

    /** Calculates the monthly savings interest without binary floating-point arithmetic. */
    @Override
    public BigDecimal calculateInterest(BigDecimal principal) {
        return principal.multiply(INTEREST_RATE);
    }
}