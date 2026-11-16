# Project Plan: Personal Expense Tracker

## 1. Project idea
Make a simple website to save the money you receive and the money you spend.

## 2. Who will use it?
One person will use this project on their own computer.

## 3. What problem does it solve?
It helps a person remember their income and expenses and see how much money is left.

## 4. Features for the first version

### Feature 1: Add a record
The user can enter:
- Type: Income or Expense
- Amount
- Category
- Date
- Note (optional)

Example:
- Type: Expense
- Amount: 150
- Category: Food
- Date: 2026-11-15
- Note: Lunch

### Feature 2: See all records
Show all saved records in a table.

Each row should show:
- Date
- Type
- Category
- Note
- Amount
- Edit button
- Delete button

### Feature 3: Edit a record
The user can change a saved record.

### Feature 4: Delete a record
The user can delete a record. Ask for confirmation before deleting.

### Feature 5: Show totals
Show these three values:
- Total Income
- Total Expenses
- Balance

Balance formula:

```text
Balance = Total Income - Total Expenses
```

### Feature 6: Search and filter
Allow the user to:
- Show only income.
- Show only expenses.
- Search between two dates.

### Feature 7: Categories
Use simple fixed categories.

Income categories:
- Salary
- Pocket Money
- Gift
- Other Income

Expense categories:
- Food
- Travel
- Shopping
- Bills
- Education
- Health
- Other Expense

## 5. Pages
Only make two pages at first:

1. `index.html`
   - Dashboard totals
   - Add record form
   - Recent records

2. `records.html`
   - All records
   - Search and filter
   - Edit and delete

You may make more pages later, but these two are enough for version 1.

## 6. Things not needed now
Do not build login, signup, admin panel, PDF reports, online payments, bank connections, or multiple user accounts.

## 7. How to know it works
- A new record is saved in MySQL.
- Saved records appear on the website.
- Editing changes the correct record.
- Deleting removes the correct record.
- Income and expense totals are correct.
- Balance uses income minus expenses.
- Search and filters show the correct records.
- Data is still saved after restarting the application.
