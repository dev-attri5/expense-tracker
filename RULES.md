# Simple Coding Rules

Follow these rules so the project stays easy to read.

## 1. General rules
1. Build one feature at a time.
2. Use simple variable and method names.
3. Do not write the whole project in one day.
4. Do not add features that are not in `PRD.md`.
5. Ask for an explanation when you do not understand a line of code.

## 2. Java rules
1. Use Java 17.
2. Use simple `if`, `else`, loops, and methods.
3. Keep each class small.
4. Use `BigDecimal` for money amounts.
5. Use `LocalDate` for dates.
6. Write SQL in `ExpenseDAO.java`.
7. Do not put SQL code inside the controller.
8. Use clear names such as `addExpense`, `getAllRecords`, and `deleteRecord`.
9. Add comments for code that may be hard to understand.
10. Do not use advanced Java features unless they are really needed.

## 3. HTML rules
1. Use normal HTML tags.
2. Give every input a label.
3. Use buttons for actions.
4. Keep page content simple.

## 4. CSS rules
1. Keep all CSS in `style.css`.
2. Use simple class names.
3. Make the website work on phone and computer.
4. Do not add animations until the main project works.

## 5. JavaScript rules
1. Keep JavaScript in `app.js`.
2. Use simple functions.
3. Use `fetch()` to send requests to Java.
4. Show a message if something goes wrong.
5. Do not put all JavaScript inside the HTML file.

## 6. Database rules
1. Use MySQL.
2. Use one table named `transactions`.
3. Amount must be more than zero.
4. Type must be `INCOME` or `EXPENSE`.
5. Do not save the amount as text.
6. Do not delete the whole database to fix a code problem.

## 7. Testing rules
After adding a feature:
1. Run the Java application.
2. Try the feature in the browser.
3. Check the result in MySQL.
4. Try an incorrect value.
5. Fix errors before starting the next feature.

## 8. Git rules
1. Save your work with Git.
2. Make a commit after finishing a feature.
3. Never upload your database password.
4. Write a short README for GitHub.
