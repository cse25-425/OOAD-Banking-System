package bac.com.mosweu.ooad_project.model;

import java.math.BigDecimal;

/** Investment account model with a minimum opening balance and interest calculation. */
public class InvestmentAccount extends Account {
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.05");
    private static final double MINIMUM_DEPOSIT = 500.00;

    /** Creates an investment account and rejects opening balances below the minimum. */
    public InvestmentAccount(String accountNumber, String branch, double initialBalance) {
        this(accountNumber, branch, initialBalance, true);
    }

    private InvestmentAccount(String accountNumber, String branch, double initialBalance, boolean opening) {
        super(accountNumber, branch, initialBalance);
        if (opening && initialBalance < MINIMUM_DEPOSIT) {
            throw new IllegalArgumentException("Investment Account requires a minimum initial deposit of BWP 500.00");
        }
    }

    /** Rehydrates an existing investment account without reapplying its opening-deposit rule. */
    public static InvestmentAccount restore(String accountNumber, String branch, double currentBalance) {
        return new InvestmentAccount(accountNumber, branch, currentBalance, false);
    }

    /** Reports whether the requested amount is within the current balance. */
    @Override
    public void withdraw(double amount) {
        deduct(amount);
    }

    /** Calculates the interest amount using the configured investment rate. */
    @Override
    public double calculateInterest() {
        return calculateInterest(BigDecimal.valueOf(getBalance())).doubleValue();
    }

    /** Calculates the monthly investment interest without binary floating-point arithmetic. */
    @Override
    public BigDecimal calculateInterest(BigDecimal principal) {
        return principal.multiply(INTEREST_RATE);
    }
}