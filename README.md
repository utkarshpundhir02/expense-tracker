# 💰 Expense Tracker App

A full-featured personal Expense Tracker backend web application built using **Spring Boot (Java)**. It supports user authentication, expense/income tracking, budget management,category management, and analytics.

---

## 🚀 Features

- 🔐 User Sign-in & Login (JWT-based)
- 📊 Income & Expense Management
- 🗂️ Category-based Budgeting
- 🌐 RESTful API design
- 📦 Modular Spring Boot multi-module architecture

---

## 🛠️ Tech Stack

- **Java 17**, **Spring Boot**
- **Maven**, **JUnit**
- **MySQL**, **JPA/Hibernate**
- **JWT + RBAC Security**
- **GitHub Actions** (for CI/CD)

---

## 🧑‍💻 How to Run

1. Clone the repo:
   ```bash
   git clone https://github.com/utkarshpundhir02/expense-tracker.git
   cd expense-tracker

2. Build the project:
   mvn clean install

3. Run the API module:
   cd api
   mvn spring-boot:run

4. API will be available at:
   http://localhost:8080


expense-tracker/
├── api/             # REST API layer (controllers)
├── application/     # Business logic layer (services)
├── domain/          # Domain models and repositories
├── infrastructure/  # Security & exception handling
├── config/          # Shared configuration (optional)
├── common/          # Utility/shared modules


| Endpoint    | Method | Description              |
| ----------- | ------ | ------------------------ |
| `/signin`   | POST   | Register a new user      |
| `/login`    | POST   | Authenticate & get token |
| `/expenses` | CRUD   | Manage expenses          |
| `/incomes`  | CRUD   | Manage incomes           |
| `/budgets`  | CRUD   | Set monthly budgets      |

🧪 Running Tests
mvn test
