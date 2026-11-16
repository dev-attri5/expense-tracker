# Step-by-Step Development Tasks

Complete each phase in order. Tick a task after it works.

## Phase 0: Install the tools
- [x] Install Java JDK 17 or newer.
- [x] Install IntelliJ IDEA.
- [x] Install MySQL Server.
- [x] Install MySQL Workbench.
- [x] Install VS Code.
- [x] Install Postman (optional, for testing).
- [x] Check that Java and MySQL work.

**Finish when:** You can run a small Java program and connect to MySQL.

## Phase 1: Understand the project
- [x] Read `PRD.md`.
- [x] Look at the folder structure in `ARCHITECTURE.md`.
- [x] Understand the six fields: id, type, amount, category, note, date.
- [x] Draw a simple picture of the dashboard.

**Finish when:** You can explain what the project will do.

## Phase 2: Make the database
- [x] Open MySQL Workbench.
- [x] Run the SQL from `DATABASE.md`.
- [x] Add the sample records.
- [x] Run `SELECT * FROM transactions;`.
- [x] Check that the sample records appear.

**Finish when:** Your database and table are ready.

## Phase 3: Create the Java project
- [x] Open Spring Initializr.
- [x] Choose Maven, Java, and Java 17.
- [x] Add Spring Web and JDBC API dependencies.
- [x] Add the MySQL Driver.
- [x] Download and open the project in IntelliJ.
- [x] Create `ExpenseTrackerApplication.java`.
- [x] Create the other three Java files from `ARCHITECTURE.md`.
- [x] Add your MySQL connection details to `application.properties`.
- [x] Run the application.

**Finish when:** The Java application starts without an error.

## Phase 4: Connect Java to MySQL
- [x] Create the `Expense` class.
- [x] Create `ExpenseDAO.java`.
- [x] Write a method to add a record.
- [x] Write a method to get all records.
- [x] Write a method to delete a record.
- [x] Write a method to update a record.
- [x] Test the SQL methods.

**Finish when:** Java can save and read records from MySQL.

## Phase 5: Make the website design
- [x] Create `index.html`.
- [x] Create `records.html`.
- [x] Create `css/style.css`.
- [x] Add the navigation bar.
- [x] Add the income and expense form.
- [x] Add the three total cards.
- [x] Add a table for records.
- [x] Check the page on a small screen.

**Finish when:** The website design appears in your browser.

## Phase 6: Connect the website to Java
- [x] Create `js/app.js`.
- [x] Create a simple test API in `ExpenseController.java`.
- [x] Use JavaScript `fetch()` to call the test API.
- [x] Display the response on the page.
- [x] Connect the form to the add-record API.
- [x] Show saved records on the page.
- [x] Show an error message if saving fails.

**Finish when:** You can add a record from the website and see it in MySQL.

## Phase 7: Finish record features
- [x] Add the view-all-records feature.
- [x] Add the edit-record feature.
- [x] Add the delete-record feature.
- [x] Ask before deleting.
- [x] Refresh the list after each change.

**Finish when:** Add, view, edit, and delete all work.

## Phase 8: Add totals
- [x] Write SQL to calculate total income.
- [x] Write SQL to calculate total expenses.
- [x] Calculate balance: income minus expenses.
- [x] Send the totals from Java to the website.
- [x] Show the totals on the dashboard.
- [x] Check the totals using a calculator.

**Finish when:** The dashboard shows the correct totals.

## Phase 9: Add search and filters
- [x] Add an Income/Expense filter.
- [x] Add a start date.
- [x] Add an end date.
- [x] Show matching records.
- [x] Add a Clear button.

**Finish when:** Filters show the correct records.

## Phase 10: Final testing
- [x] Add an income record.
- [x] Add an expense record.
- [x] Edit a record.
- [x] Delete a record.
- [x] Try an amount of zero.
- [x] Try leaving a required field empty.
- [x] Check the dashboard totals.
- [x] Check the date filters.
- [x] Restart the application and check that records remain.
- [x] Update `README.md`.
- [x] Upload the project to GitHub.

**Finish when:** All main features work.

## Do these only later
- [ ] Login and signup.
- [ ] Monthly charts.
- [ ] Export to Excel or PDF.
- [ ] Multiple users.
- [ ] Dark mode.
