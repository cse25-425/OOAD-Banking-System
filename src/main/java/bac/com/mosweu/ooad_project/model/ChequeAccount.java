package bac.com.mosweu.ooad_project.model;

import java.math.BigDecimal;

/** Cheque account model that retains employer details and earns no interest. */
public class ChequeAccount extends Account {
    private String employerName;
    private String employerAddress;

    /** Creates a cheque account and stores the account holder's employer details. */
    public ChequeAccount(
            String accountNumber, String branch, double initialBalance, String employerName, String employerAddress) {
        this(accountNumber, branch, initialBalance, employerName, employerAddress, true);
    }

    private ChequeAccount(
            String accountNumber,
            String branch,
            double initialBalance,
            String employerName,
            String employerAddress,
            boolean validateEmployment) {
        super(accountNumber, branch, initialBalance);
        if (validateEmployment
                && (employerName == null
                        || employerName.isBlank()
                        || employerAddress == null
                        || employerAddress.isBlank())) {
            throw new IllegalArgumentException("Employer name and address are required for a cheque account.");
        }
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    /** Rehydrates a legacy account even if older data lacks employment fields. */
    public static ChequeAccount restore(
            String accountNumber, String branch, double currentBalance, String employerName, String employerAddress) {
        return new ChequeAccount(accountNumber, branch, currentBalance, employerName, employerAddress, false);
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }

    /** Reports whether the requested amount is within the current balance. */
    @Override
    public void withdraw(double amount) {
        deduct(amount);
    }

    /** Cheque accounts have no interest return. */
    @Override
    public double calculateInterest() {
        return 0; // Cheque accounts do not earn interest
    }

    /** Cheque accounts do not earn interest. */
    @Override
    public BigDecimal calculateInterest(BigDecimal principal) {
        return BigDecimal.ZERO;
    }
}
