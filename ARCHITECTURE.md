# Simple Project Structure

## 1. Keep the code easy
Use only a few Java files. Do not create many folders or classes.

We will use:
- One Java class to start the website.
- One Java class to handle website requests.
- One Java class to run SQL queries.
- One Java class to hold record information.

## 2. Full folder structure

```text
PersonalExpenseTracker/
│
├── pom.xml
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── expense/
        │           ├── ExpenseTrackerApplication.java
        │           ├── ExpenseController.java
        │           ├── ExpenseDAO.java
        │           └── Expense.java
        │
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── records.html
                ├── css/
                │   └── style.css
                └── js/
                    └── app.js
```

## 3. What does each file do?

### `ExpenseTrackerApplication.java`
Starts the Java application. You run this file to start the website.

### `ExpenseController.java`
Receives requests from JavaScript. For example, it receives a request when the user clicks Save.

### `ExpenseDAO.java`
DAO means Data Access Object. This class runs SQL commands to save, read, edit, and delete records.

### `Expense.java`
Holds the details of one record:
- id
- type
- amount
- category
- note
- date

### `application.properties`
Contains the MySQL connection settings.

### `index.html`
Shows the dashboard and the add-record form.

### `records.html`
Shows all records and the search options.

### `style.css`
Controls the look of the website.

### `app.js`
Sends requests to Java and shows the returned data on the page.

## 4. How one action works

Example: Saving an expense.

1. User fills the form in `index.html`.
2. `app.js` reads the form values.
3. JavaScript sends the values to `ExpenseController.java`.
4. The controller calls `ExpenseDAO.java`.
5. `ExpenseDAO.java` saves the record in MySQL.
6. Java sends a success response.
7. JavaScript refreshes the records on the page.

## 5. Keep these rules
- Put HTML in the `static` folder.
- Put CSS in the `css` folder.
- Put JavaScript in the `js` folder.
- Keep SQL commands in `ExpenseDAO.java`.
- Keep request handling in `ExpenseController.java`.
- Do not add a service, repository, DTO, security, or extra layers in version 1.
