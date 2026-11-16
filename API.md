# Simple API Guide

An API lets JavaScript talk to Java.

The website will use these addresses. The examples assume the application runs at `http://localhost:8080`.

## 1. Get all records

- Method: `GET`
- Address: `/api/transactions`
- Purpose: Get all saved records.

## 2. Add a record

- Method: `POST`
- Address: `/api/transactions`
- Purpose: Save a new income or expense.

Send this JSON:

```json
{
  "type": "EXPENSE",
  "amount": 150.00,
  "category": "Food",
  "note": "Lunch",
  "transactionDate": "2026-11-15"
}
```

## 3. Get one record

- Method: `GET`
- Address: `/api/transactions/1`
- Purpose: Get the record with ID 1.

## 4. Update a record

- Method: `PUT`
- Address: `/api/transactions/1`
- Purpose: Change the record with ID 1.

Send the same fields as the Add Record example.

## 5. Delete a record

- Method: `DELETE`
- Address: `/api/transactions/1`
- Purpose: Delete the record with ID 1.

## 6. Get dashboard totals

- Method: `GET`
- Address: `/api/summary`
- Purpose: Get income, expenses, and balance.

Example response:

```json
{
  "totalIncome": 12000.00,
  "totalExpenses": 650.00,
  "balance": 11350.00
}
```

## 7. JavaScript example

This is a small example of asking Java for all records:

```javascript
fetch("/api/transactions")
    .then(response => response.json())
    .then(records => {
        console.log(records);
    })
    .catch(error => {
        console.log("Something went wrong", error);
    });
```

## 8. Important
- `GET` means read data.
- `POST` means add data.
- `PUT` means update data.
- `DELETE` means delete data.
- Java should check the data before saving it.
- These API addresses are the plan. Create them in `ExpenseController.java` during development.
