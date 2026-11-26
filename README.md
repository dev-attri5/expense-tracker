# Personal Expense Tracker

A clean, beginner-friendly full-stack web application to record and track your personal finances (income, expenses, and balance).

---

## 🌟 Features

- **Add Transactions:** Record income and expenses with amount, category, date, and notes.
- **Smart Category Selection:** Automatically toggles between income and expense categories.
- **Financial Dashboard:** Real-time summary cards displaying:
  - Total Income
  - Total Expenses
  - Balance ($\text{Total Income} - \text{Total Expenses}$)
- **Transaction History:** View all records in a responsive table with colored badges.
- **Edit & Delete:** Update existing transactions or delete them with confirmation.
- **Search & Filter:** Filter transactions by type (All, Income, Expense) or date range (From Date $\rightarrow$ To Date).
- **Persistent Storage:** All data safely stored in a local MySQL database.
- **Responsive Design:** Works seamlessly on mobile phones, tablets, and desktop browsers.

---

## 🛠️ Technology Stack

- **Backend:**
  - Java 17
  - Spring Boot 3.3.5 (Spring Web, Spring JDBC)
  - JdbcTemplate (clean, direct SQL queries)
- **Database:**
  - MySQL Server 8.4
  - MySQL Workbench
- **Frontend:**
  - HTML5 & CSS3 (Mobile-responsive, card-based UI)
  - Vanilla JavaScript (`fetch()` API)

---

## 📁 Project Structure

```text
expense tracker/
├── pom.xml
├── .gitignore
├── README.md
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── expense/
        │           ├── ExpenseTrackerApplication.java  # Main application runner
        │           ├── ExpenseController.java          # REST API endpoints
        │           ├── ExpenseDAO.java                 # SQL queries with JdbcTemplate
        │           └── Expense.java                    # Transaction model
        │
        └── resources/
            ├── application.properties                  # Database connection settings
            └── static/
                ├── index.html                          # Dashboard & Add form
                ├── records.html                        # All records & Filters
                ├── css/
                │   └── style.css                       # Clean, responsive styles
                └── js/
                    └── app.js                          # Fetch API integration
```

---

## 🚀 How to Run the Application

### 1. Prerequisites
Ensure you have installed:
- Java JDK 17 or newer
- MySQL Server 8.4 & MySQL Workbench
- IntelliJ IDEA Community Edition

### 2. Database Setup
1. Open **MySQL Workbench** and connect to your local MySQL instance.
2. Run the following SQL script to create the database and table:

```sql
CREATE DATABASE expense_tracker;

USE expense_tracker;

CREATE TABLE transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    note VARCHAR(200),
    transaction_date DATE NOT NULL
);

-- Optional sample data
INSERT INTO transactions (type, amount, category, note, transaction_date) VALUES
('INCOME', 10000.00, 'Pocket Money', 'Monthly allowance', '2026-11-01'),
('EXPENSE', 150.00, 'Food', 'Lunch', '2026-11-02'),
('EXPENSE', 500.00, 'Travel', 'Bus pass', '2026-11-03'),
('INCOME', 2000.00, 'Gift', 'Birthday gift', '2026-11-05');
```

### 3. Configure Database Credentials
Open `src/main/resources/application.properties` and verify your MySQL password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
server.port=8080
```

### 4. Run the Application
1. Open the project folder in **IntelliJ IDEA**.
2. Navigate to `src/main/java/com/expense/ExpenseTrackerApplication.java`.
3. Click the green **Run** button ($\blacktriangleright$).

### 5. Open in Browser
- **Dashboard:** [http://localhost:8080](http://localhost:8080)
- **All Records & Filters:** [http://localhost:8080/records.html](http://localhost:8080/records.html)

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/status` | Backend and database health check |
| `GET` | `/api/summary` | Returns total income, total expenses, and balance |
| `GET` | `/api/transactions` | Returns all records (supports `?type=`, `?startDate=`, `?endDate=`) |
| `GET` | `/api/transactions/{id}` | Returns a single record by ID |
| `POST` | `/api/transactions` | Creates a new transaction |
| `PUT` | `/api/transactions/{id}` | Updates an existing transaction |
| `DELETE` | `/api/transactions/{id}` | Deletes a transaction by ID |
