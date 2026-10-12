package bac.com.mosweu.ooad_project.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String firstName;
    private String surname;
    private String address;
    private String customerId;
    private List<Account> accounts; // Association: One customer has many accounts

    public Customer(String customerId, String firstName, String surname, String address) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public String getFullName() {
        return firstName + " " + surname;
    }
}