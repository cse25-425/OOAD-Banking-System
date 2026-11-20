# OOAD Banking System

A JavaFX banking-system assignment backed by a file-based H2 database.

## Implemented features

- Customer registration and username/password authentication.
- Salted PBKDF2-HMAC-SHA-256 password hashes, with automatic migration of legacy plaintext passwords.
- Temporary login lock after three consecutive failed password attempts.
- Multiple Savings, Investment, and Cheque accounts per customer.
- Account lifecycle states (new, active, suspended, and closed) with transaction restrictions for inactive accounts.
- Account-specific opening rules, deposits, withdrawals, and persisted transaction history.
- Monthly interest credits of 0.05% for Savings and 5% for Investment accounts. Interest is applied once per calendar
  month when the application starts.
- DAO operations for customer and account persistence.

## Database location

The application uses a persistent H2 database. By default, its base path is
`~/.ooad-banking/bankdb`. Set the `bank.db.path` Java system property to choose a
different base path; do not include the `.mv.db` extension. For example:

```text
-Dbank.db.path=C:/path/to/project/bankdb
```

Database initialization creates missing tables but does not seed or reset
customer and account records. Startup applies compatible schema updates and
migrates legacy password values; back up the database file before first launch
after an application update.

## Running

The project targets Java 21 and uses Maven with the JavaFX Maven plugin. The
application entry point is `bac.com.mosweu.ooad_project.Launcher`.

## Integration smoke test

`src/test/java/bac/com/mosweu/ooad_project/BankingIntegrationTest.java` is a
standalone integration smoke test. Run its `main` method from IntelliJ; it
creates and removes an isolated temporary database and checks registration,
legacy schema/password migration, login lockout, account creation and lifecycle,
interest, transactions, transaction history, and account-type rules. It passed
with Java 21 and H2 2.2.224 in the development environment.

The application intentionally does not seed customer data at startup. Before
the presentation, ensure the selected database contains at least ten sample
customer/account records as required by the assignment.
