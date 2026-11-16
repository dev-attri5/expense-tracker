# Simple Website Design

## 1. Look and feel
Make a clean and simple website. Do not spend too much time on effects.

Use:
- Light grey page background.
- White cards.
- Dark text.
- Green for income.
- Red for expenses.
- Simple buttons.
- Clear labels.

## 2. Dashboard page: `index.html`

The dashboard should have:

```text
-----------------------------------------------
 Personal Expense Tracker       Add New Record
-----------------------------------------------
| Total Income | Total Expenses | Balance     |
|   ₹10,000    |     ₹650       |  ₹9,350     |
-----------------------------------------------
 Add Income or Expense
 Type:       [Income / Expense]
 Amount:     [______________]
 Category:   [______________]
 Date:       [______________]
 Note:       [______________]
             [Save Record]
-----------------------------------------------
 Recent Records
 Date       Type       Category      Amount
 01 Nov     Income     Pocket Money  ₹10,000
 02 Nov     Expense    Food          ₹150
-----------------------------------------------
```

## 3. Records page: `records.html`

Show:
- Page heading: All Records.
- Type filter: All, Income, Expense.
- Start date.
- End date.
- Search button.
- Clear button.
- Records table.
- Edit and Delete buttons.

## 4. Form fields
The add-record form needs:
1. Type dropdown.
2. Amount input.
3. Category dropdown.
4. Date input.
5. Note input.
6. Save button.

## 5. Mobile design
- Put cards one below another.
- Make the form fields full width.
- Keep buttons easy to tap.
- Allow the table to scroll sideways if needed.

## 6. Messages
Show simple messages:
- `Record saved successfully.`
- `Record updated successfully.`
- `Record deleted successfully.`
- `Please enter an amount greater than zero.`
- `Could not connect to the server. Please try again.`

## 7. Design rules
- Make the main features work before adding animations.
- Use the same button style on both pages.
- Keep text easy to read.
- Show labels, not just placeholder text.
- Do not use color as the only way to show income or expense.
