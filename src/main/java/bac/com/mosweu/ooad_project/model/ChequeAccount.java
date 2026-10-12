package bac.com.mosweu.ooad_project.model;

public class ChequeAccount extends Account {
    private String employerName;
    private String employerAddress;

    public ChequeAccount(String accountNumber, String branch, double initialBalance, String employerName, String employerAddress) {
        super(accountNumber, branch, initialBalance);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= getBalance()) {
            System.out.println("Withdrawn BWP " + amount + " from Cheque Account.");
        } else {
            System.out.println("Insufficient funds.");
        }
    }

    @Override
    public double calculateInterest() {
        return 0; // Cheque accounts do not earn interest
    }
}