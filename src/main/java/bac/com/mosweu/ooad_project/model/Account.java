package bac.com.mosweu.ooad_project.model;

public abstract class Account {
    // Encapsulation: Private attributes
    private String accountNumber;
    private double balance;
    private String branch;

    // Constructor
    public Account(String accountNumber, String branch, double initialBalance) { 
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("Deposited BWP " + amount + " into account " + accountNumber);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    // Getters (Encapsulation)
    public String getAccountNumber() { return accountNumber; } 
    public double getBalance() { return balance; }
    public String getBranch() { return branch; }

    // Abstract methods
    public abstract void withdraw(double amount);
    public abstract double calculateInterest();
}