/**
 * app.js - Personal Expense Tracker Frontend JavaScript
 *
 * This file connects the HTML forms and tables to our Java Spring Boot backend
 * using standard JavaScript fetch() calls.
 */

// Categories defined in PRD.md
const EXPENSE_CATEGORIES = [
    "Food",
    "Travel",
    "Shopping",
    "Bills",
    "Education",
    "Health",
    "Other Expense"
];

const INCOME_CATEGORIES = [
    "Salary",
    "Pocket Money",
    "Gift",
    "Other Income"
];

// Run when the HTML page is fully loaded in the browser
document.addEventListener("DOMContentLoaded", () => {
    // 1. Check if on login.html
    const loginForm = document.getElementById("login-form");
    if (loginForm) {
        initLoginPage();
        return;
    }

    // 2. Check if on register.html
    const registerForm = document.getElementById("register-form");
    if (registerForm) {
        initRegisterPage();
        return;
    }

    // 3. For protected pages (index.html, records.html), check session first
    checkAuthAndInit();
});

/* ======================================================
   AUTHENTICATION LOGIC (login.html & register.html)
   ====================================================== */

/**
 * Checks if user is authenticated. If not, redirects to login.html.
 */
function checkAuthAndInit() {
    fetch("/api/auth/me")
        .then(response => {
            if (!response.ok) {
                throw new Error("Unauthorized");
            }
            return response.json();
        })
        .then(userData => {
            // Setup navbar user profile and logout
            setupUserNavbar(userData.username);

            // Initialize whichever protected page is active
            if (document.getElementById("transaction-form")) {
                initDashboard();
            } else if (document.getElementById("all-records-body")) {
                initRecordsPage();
            }
        })
        .catch(() => {
            window.location.href = "login.html";
        });
}

function setupUserNavbar(username) {
    const greeting = document.getElementById("user-greeting");
    if (greeting && username) {
        greeting.textContent = "👤 " + username;
    }

    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", handleLogout);
    }
}

function handleLogout() {
    fetch("/api/auth/logout", { method: "POST" })
        .finally(() => {
            window.location.href = "login.html";
        });
}

function initLoginPage() {
    // If already logged in, redirect to dashboard
    fetch("/api/auth/me")
        .then(res => {
            if (res.ok) window.location.href = "index.html";
        })
        .catch(() => {});

    const form = document.getElementById("login-form");
    if (!form) return;

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value;

        fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        })
        .then(async response => {
            if (!response.ok) {
                const err = await response.text();
                throw new Error(err || "Invalid username or password.");
            }
            return response.json();
        })
        .then(() => {
            window.location.href = "index.html";
        })
        .catch(err => {
            showAlert(err.message, "error");
        });
    });
}

function initRegisterPage() {
    // If already logged in, redirect to dashboard
    fetch("/api/auth/me")
        .then(res => {
            if (res.ok) window.location.href = "index.html";
        })
        .catch(() => {});

    const form = document.getElementById("register-form");
    if (!form) return;

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value;
        const confirm = document.getElementById("confirm-password").value;

        if (password !== confirm) {
            showAlert("Passwords do not match.", "error");
            return;
        }

        fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        })
        .then(async response => {
            if (!response.ok) {
                const err = await response.text();
                throw new Error(err || "Failed to create account.");
            }
            return response.json();
        })
        .then(() => {
            showAlert("Account created! Redirecting to dashboard...", "success");
            setTimeout(() => {
                window.location.href = "index.html";
            }, 800);
        })
        .catch(err => {
            showAlert(err.message, "error");
        });
    });
}

/* ======================================================
   DASHBOARD LOGIC (index.html)
   ====================================================== */

function initDashboard() {
    const typeSelect = document.getElementById("type");
    const categorySelect = document.getElementById("category");
    const dateInput = document.getElementById("transaction-date");
    const form = document.getElementById("transaction-form");

    // Set today's date as default in the date input
    const today = new Date().toISOString().split("T")[0];
    if (dateInput) {
        dateInput.value = today;
    }

    // Update categories dropdown when Type (Income/Expense) changes
    if (typeSelect && categorySelect) {
        typeSelect.addEventListener("change", () => {
            updateCategoryDropdown(typeSelect.value, categorySelect);
        });
        updateCategoryDropdown(typeSelect.value, categorySelect);
    }

    // Handle form submit (Add new transaction)
    if (form) {
        form.addEventListener("submit", handleAddTransaction);
    }

    // Load recent records and totals
    loadRecentRecords();
    loadSummary();
}

/**
 * Updates a <select> element's options based on whether Type is INCOME or EXPENSE.
 */
function updateCategoryDropdown(selectedType, selectElement) {
    selectElement.innerHTML = "";
    const list = selectedType === "INCOME" ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;

    list.forEach(cat => {
        const option = document.createElement("option");
        option.value = cat;
        option.textContent = cat;
        selectElement.appendChild(option);
    });
}

/**
 * Sends a POST request to Java to save a new record.
 */
function handleAddTransaction(event) {
    event.preventDefault();

    const type = document.getElementById("type").value;
    const amountVal = parseFloat(document.getElementById("amount").value);
    const category = document.getElementById("category").value;
    const transactionDate = document.getElementById("transaction-date").value;
    const note = document.getElementById("note").value.trim();

    if (isNaN(amountVal) || amountVal <= 0) {
        showAlert("Please enter an amount greater than zero.", "error");
        return;
    }

    const payload = {
        type: type,
        amount: amountVal,
        category: category,
        transactionDate: transactionDate,
        note: note
    };

    fetch("/api/transactions", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }
            const errorText = await response.text();
            let errorMessage = errorText;
            try {
                const parsed = JSON.parse(errorText);
                if (parsed.message) errorMessage = parsed.message;
                else if (parsed.error) errorMessage = parsed.error;
            } catch (e) {}
            throw new Error(errorMessage || "Could not save record.");
        }
        return response.json();
    })
    .then(data => {
        showAlert(data.message || "Record saved successfully.", "success");

        document.getElementById("amount").value = "";
        document.getElementById("note").value = "";

        loadRecentRecords();
        loadSummary();
    })
    .catch(error => {
        console.error("Error adding transaction:", error);
        showAlert(error.message || "Could not connect to the server. Please try again.", "error");
    });
}

/**
 * Fetches transactions from Java and displays the latest 5 on the dashboard.
 */
function loadRecentRecords() {
    const tbody = document.getElementById("recent-records-body");
    if (!tbody) return;

    fetch("/api/transactions")
        .then(async response => {
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html";
                    return;
                }
                const errText = await response.text();
                throw new Error(errText || "Failed to load records from server.");
            }
            return response.json();
        })
        .then(records => {
            if (!records || records.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="empty-message">No records found. Add your first record above!</td></tr>`;
                return;
            }

            const recent = records.slice(0, 5);
            tbody.innerHTML = "";

            recent.forEach(record => {
                tbody.appendChild(createRecordRow(record, false));
            });
        })
        .catch(error => {
            console.error("Error fetching records:", error);
            tbody.innerHTML = `<tr><td colspan="5" class="empty-message" style="color: #dc3545;">${escapeHtml(error.message || "Could not connect to the server.")}</td></tr>`;
        });
}

/**
 * Fetches dashboard totals from /api/summary and displays them in the summary cards.
 */
function loadSummary() {
    const incomeElem = document.getElementById("total-income");
    const expensesElem = document.getElementById("total-expenses");
    const balanceElem = document.getElementById("total-balance");

    if (!incomeElem || !expensesElem || !balanceElem) return;

    fetch("/api/summary")
        .then(async response => {
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html";
                    return;
                }
                const errText = await response.text();
                throw new Error(errText || "Failed to load summary.");
            }
            return response.json();
        })
        .then(data => {
            const income = Number(data.totalIncome || 0);
            const expenses = Number(data.totalExpenses || 0);
            const balance = Number(data.balance || 0);

            incomeElem.textContent = "₹" + income.toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });

            expensesElem.textContent = "₹" + expenses.toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });

            balanceElem.textContent = "₹" + balance.toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });

            if (balance < 0) {
                balanceElem.className = "amount expense";
            } else {
                balanceElem.className = "amount balance";
            }
        })
        .catch(error => {
            console.error("Error loading summary totals:", error);
        });
}

/* ======================================================
   RECORDS PAGE LOGIC (records.html)
   ====================================================== */

function initRecordsPage() {
    const editForm = document.getElementById("edit-form");
    const editType = document.getElementById("edit-type");
    const editCategory = document.getElementById("edit-category");
    const cancelEditBtn = document.getElementById("cancel-edit-btn");
    const editModal = document.getElementById("edit-modal");

    // Dynamic category changing inside edit modal
    if (editType && editCategory) {
        editType.addEventListener("change", () => {
            updateCategoryDropdown(editType.value, editCategory);
        });
    }

    // Cancel button in edit modal
    if (cancelEditBtn && editModal) {
        cancelEditBtn.addEventListener("click", () => {
            editModal.style.display = "none";
        });
    }

    // Submit handler for edit form
    if (editForm) {
        editForm.addEventListener("submit", handleUpdateRecord);
    }

    // Close modal if user clicks outside of it
    if (editModal) {
        editModal.addEventListener("click", (e) => {
            if (e.target === editModal) {
                editModal.style.display = "none";
            }
        });
    }

    // Filter form submit listener
    const filterForm = document.getElementById("filter-form");
    if (filterForm) {
        filterForm.addEventListener("submit", handleFilterRecords);
    }

    // Clear filters button listener
    const clearFiltersBtn = document.getElementById("clear-filters-btn");
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener("click", handleClearFilters);
    }

    // Load and render all records
    loadAllRecords();
}

/**
 * Handles filtering records by type and date range.
 */
function handleFilterRecords(event) {
    event.preventDefault();

    const type = document.getElementById("filter-type").value;
    const startDate = document.getElementById("filter-start-date").value;
    const endDate = document.getElementById("filter-end-date").value;

    const params = new URLSearchParams();
    if (type && type !== "ALL") {
        params.append("type", type);
    }
    if (startDate) {
        params.append("startDate", startDate);
    }
    if (endDate) {
        params.append("endDate", endDate);
    }

    const queryString = params.toString() ? "?" + params.toString() : "";

    fetch("/api/transactions" + queryString)
        .then(async response => {
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html";
                    return;
                }
                const errText = await response.text();
                throw new Error(errText || "Failed to filter records.");
            }
            return response.json();
        })
        .then(records => {
            renderRecordsTable(records);
        })
        .catch(error => {
            console.error("Error filtering records:", error);
            showAlert(error.message || "Failed to filter records.", "error");
        });
}

/**
 * Clears active filters and restores full records list.
 */
function handleClearFilters() {
    document.getElementById("filter-type").value = "ALL";
    document.getElementById("filter-start-date").value = "";
    document.getElementById("filter-end-date").value = "";
    loadAllRecords();
}

/**
 * Fetches all transactions from Java and displays them in records.html table.
 */
function loadAllRecords() {
    const tbody = document.getElementById("all-records-body");
    if (!tbody) return;

    fetch("/api/transactions")
        .then(async response => {
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html";
                    return;
                }
                const errText = await response.text();
                throw new Error(errText || "Failed to load records from server.");
            }
            return response.json();
        })
        .then(records => {
            renderRecordsTable(records || []);
        })
        .catch(error => {
            console.error("Error loading records:", error);
            tbody.innerHTML = `<tr><td colspan="6" class="empty-message" style="color: #dc3545;">${escapeHtml(error.message || "Could not connect to the server.")}</td></tr>`;
        });
}

/**
 * Renders an array of records into the all-records table.
 */
function renderRecordsTable(records) {
    const tbody = document.getElementById("all-records-body");
    if (!tbody) return;

    if (!records || records.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-message">No records found.</td></tr>`;
        return;
    }

    tbody.innerHTML = "";

    records.forEach(record => {
        tbody.appendChild(createRecordRow(record, true));
    });
}

/**
 * Shared helper to build a <tr> element for a transaction record.
 * Eliminates duplicate row-building logic across pages.
 */
function createRecordRow(record, includeActions = false) {
    const tr = document.createElement("tr");

    const badgeClass = record.type === "INCOME" ? "badge-income" : "badge-expense";
    const amountClass = record.type === "INCOME" ? "income" : "expense";
    const formattedAmount = Number(record.amount).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    let html = `
        <td>${record.transactionDate || "-"}</td>
        <td><span class="badge ${badgeClass}">${record.type}</span></td>
        <td>${escapeHtml(record.category || "-")}</td>
        <td>${escapeHtml(record.note || "-")}</td>
        <td class="text-right ${amountClass}">₹${formattedAmount}</td>
    `;

    if (includeActions) {
        html += `
            <td style="text-align: center;">
                <button class="btn btn-edit" onclick="openEditModal(${record.id})">Edit</button>
                <button class="btn btn-danger" onclick="deleteRecord(${record.id})">Delete</button>
            </td>
        `;
    }

    tr.innerHTML = html;
    return tr;
}

/**
 * Opens the Edit Modal and pre-populates it with the record's current values.
 */
window.openEditModal = function(id) {
    const editModal = document.getElementById("edit-modal");
    const editId = document.getElementById("edit-id");
    const editType = document.getElementById("edit-type");
    const editAmount = document.getElementById("edit-amount");
    const editCategory = document.getElementById("edit-category");
    const editDate = document.getElementById("edit-date");
    const editNote = document.getElementById("edit-note");

    fetch(`/api/transactions/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Could not find record with ID " + id);
            }
            return response.json();
        })
        .then(record => {
            editId.value = record.id;
            editType.value = record.type;
            editAmount.value = record.amount;
            editDate.value = record.transactionDate;
            editNote.value = record.note || "";

            updateCategoryDropdown(record.type, editCategory);
            editCategory.value = record.category;

            editModal.style.display = "flex";
        })
        .catch(error => {
            console.error("Error loading record for edit:", error);
            showAlert("Could not load record details.", "error");
        });
};

/**
 * Handles saving updates from the Edit Modal.
 */
function handleUpdateRecord(event) {
    event.preventDefault();

    const id = document.getElementById("edit-id").value;
    const type = document.getElementById("edit-type").value;
    const amountVal = parseFloat(document.getElementById("edit-amount").value);
    const category = document.getElementById("edit-category").value;
    const transactionDate = document.getElementById("edit-date").value;
    const note = document.getElementById("edit-note").value.trim();

    if (isNaN(amountVal) || amountVal <= 0) {
        showAlert("Please enter an amount greater than zero.", "error", "modal-alert-box");
        return;
    }

    const payload = {
        type: type,
        amount: amountVal,
        category: category,
        transactionDate: transactionDate,
        note: note
    };

    fetch(`/api/transactions/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }
            const errorText = await response.text();
            let errorMessage = errorText;
            try {
                const parsed = JSON.parse(errorText);
                if (parsed.message) errorMessage = parsed.message;
                else if (parsed.error) errorMessage = parsed.error;
            } catch (e) {}
            throw new Error(errorMessage || "Failed to update record.");
        }
        return response.json();
    })
    .then(data => {
        document.getElementById("edit-modal").style.display = "none";
        showAlert(data.message || "Record updated successfully.", "success");
        loadAllRecords();
    })
    .catch(error => {
        console.error("Error updating record:", error);
        showAlert(error.message || "Failed to update record.", "error", "modal-alert-box");
    });
}

/**
 * Asks for confirmation and deletes a record.
 */
window.deleteRecord = function(id) {
    const confirmed = confirm("Are you sure you want to delete this record? This action cannot be undone.");
    if (!confirmed) {
        return;
    }

    fetch(`/api/transactions/${id}`, {
        method: "DELETE"
    })
    .then(async response => {
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }
            const errorText = await response.text();
            throw new Error(errorText || "Failed to delete record.");
        }
        return response.json();
    })
    .then(data => {
        showAlert(data.message || "Record deleted successfully.", "success");
        loadAllRecords();
    })
    .catch(error => {
        console.error("Error deleting record:", error);
        showAlert(error.message || "Failed to delete record.", "error");
    });
};

/* ======================================================
   HELPER UTILITIES
   ====================================================== */

/**
 * Displays a temporary green or red banner alert message.
 * Defaults to 'alert-box' on the main page, or can target 'modal-alert-box' inside modals.
 */
function showAlert(message, type, targetId = "alert-box") {
    const alertBox = document.getElementById(targetId) || document.getElementById("alert-box");
    if (!alertBox) return;

    alertBox.textContent = message;
    alertBox.className = "alert " + (type === "success" ? "alert-success" : "alert-error");
    alertBox.style.display = "block";

    setTimeout(() => {
        alertBox.style.display = "none";
    }, 4000);
}

/**
 * Escapes special HTML characters to prevent XSS issues.
 */
function escapeHtml(str) {
    if (!str) return "";
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
