# MySQL Database Setup

## 1. Database name
`expense_tracker`

## 2. Create the database and table

Open MySQL Workbench and run:

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
```

## 3. Add sample records

```sql
INSERT INTO transactions
(type, amount, category, note, transaction_date)
VALUES
('INCOME', 10000.00, 'Pocket Money', 'Monthly money', '2026-11-01'),
('EXPENSE', 150.00, 'Food', 'Lunch', '2026-11-02'),
('EXPENSE', 500.00, 'Travel', 'Bus pass', '2026-11-03'),
('INCOME', 2000.00, 'Gift', 'Birthday gift', '2026-11-05');
```

## 4. Show all records

```sql
SELECT * FROM transactions;
```

## 5. Find total income

```sql
SELECT COALESCE(SUM(amount), 0)
FROM transactions
WHERE type = 'INCOME';
```

## 6. Find total expenses

```sql
SELECT COALESCE(SUM(amount), 0)
FROM transactions
WHERE type = 'EXPENSE';
```

## 7. Find records by type

```sql
SELECT * FROM transactions
WHERE type = 'EXPENSE';
```

## 8. Find records between dates

```sql
SELECT * FROM transactions
WHERE transaction_date BETWEEN '2026-11-01' AND '2026-11-30';
```

## 9. Java connection settings

Put these settings in `src/main/resources/application.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
server.port=8080
```

Replace `YOUR_MYSQL_PASSWORD` with your own local MySQL password. Do not upload your password to GitHub.

## 10. Important
- `id` is created automatically.
- `amount` stores money with two decimal places.
- `type` must be `INCOME` or `EXPENSE`.
- Use a positive amount for both income and expense.
- The first version needs only this one table.
