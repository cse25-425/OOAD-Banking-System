package bac.com.mosweu.ooad_project.model;

import java.util.ArrayList;
import java.util.List;

/** In-memory customer model associated with one or more accounts. */
public class Customer {
    private String firstName;
    private String surname;
    private String address;
    private String customerId;
    private List<Account> accounts; // One customer can be associated with multiple accounts.

    /** Creates a customer model and initializes its account collection. */
    public Customer(String customerId, String firstName, String surname, String address) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    /** Adds an account to this customer's in-memory collection. */
    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    /** Returns this customer's account collection. */
    public List<Account> getAccounts() {
        return accounts;
    }

    /** Returns the customer's first and last names joined by a space. */
    public String getFullName() {
        return firstName + " " + surname;
    }
}