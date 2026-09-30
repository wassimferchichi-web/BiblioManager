# BiblioManager

A complete Library Management System built with JavaFX and MySQL, developed as part of a software development project at Ecole Polytechnique de Tunisie.

---

## Features

- Secure Authentication — BCrypt password hashing with role-based access (Admin / Librarian / Member)
- Dashboard — Real-time statistics with Bar Chart and Pie Chart
- Books Management — Full CRUD with category selection and real-time search
- Members Management — Register and manage library members
- Loans Management — Issue loans, return books, automatic overdue detection
- PDF Export — Generate professional loan reports with iTextPDF
- Modern UI — Dark theme

---

## Tech Stack

| Technology | Role |
|---|---|
| Java 17+ | Core language |
| JavaFX 21 | GUI framework |
| MySQL 8.4 | Database (via WAMP Server) |
| JDBC | Database connectivity |
| BCrypt (jBCrypt) | Password hashing |
| iTextPDF 5.5 | PDF generation |
| Maven | Dependency management |

---

## Architecture

The project follows MVC + DAO design patterns:

src/
├── controllers/ # UI logic (LoginController, DashboardController...)
├── dao/ # Database layer (BookDAO, LoanDAO...)
├── models/ # Data models (User, Book, Member, Loan)
├── utils/ # DBConnection (Singleton), PDFExporter
└── resources/fxml/ # UI views (login.fxml, dashboard.fxml...)


---

## Database Schema

6 tables: roles, users, categories, books, members, loans

---

## Getting Started

### Prerequisites
- JDK 17+
- WAMP Server (MySQL running on port 3306)
- Maven

### Setup

1. Clone the repo
```bash
   git clone https://github.com/wassimferchichi-web/BiblioManager.git
```

2. Create the database — run the SQL script in phpMyAdmin:
```sql
   CREATE DATABASE bibliomanager;
```

3. Run the app

mvn javafx:run


4. Login with default admin account

Username: admin
Password: admin123


---

## Authors

| Name | School |
|---|---|
| Wassim Ferchichi | Ecole Polytechnique de Tunisie |
| Kabil Gannouni | Ecole Polytechnique de Tunisie |

Supervised by Mme Bouthaina Fessi

---

## License

This project was developed for educational purposes at Ecole Polytechnique de Tunisie.
