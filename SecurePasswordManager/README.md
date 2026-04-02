# 🔐 Secure Web-Based Password Manager

A **production-grade password manager** built entirely with Core Java technologies. No Spring Boot, no frameworks — pure Servlets, JSP, JDBC, and MySQL.

## Tech Stack

| Layer | Technology |
|---|---|
| **Controller** | Java Servlets (javax.servlet) |
| **View** | JSP (JavaServer Pages) |
| **Model** | Java Beans (POJOs) |
| **Database** | MySQL 8.0+ via JDBC |
| **Server** | Apache Tomcat 9.0+ |
| **Security** | SHA-256, AES-128, CSRF Tokens |

## Features

### 🔑 Authentication System
- User registration with server-side validation
- SHA-256 password hashing (never stores plain text)
- Session management with 5-minute timeout
- Account lockout after 5 failed login attempts

### 🏦 Encrypted Password Vault
- Add, view, edit, and delete stored credentials
- **AES encryption** with per-user key derivation
- Passwords decrypted only for authenticated owner
- Copy-to-clipboard functionality

### ⚡ Password Generator
- Cryptographically secure random generation (SecureRandom)
- Configurable length (8–16 characters)
- Guarantees uppercase, lowercase, digits, and symbols

### 📊 Password Strength Checker
- Real-time client-side strength evaluation
- Visual strength bar (Weak → Medium → Strong)
- Server-side validation enforces minimum strength

### 🛡️ Security Suite
- **SQL Injection Prevention** — PreparedStatement everywhere
- **XSS Prevention** — Output escaping in all JSP files
- **CSRF Protection** — Synchronizer Token Pattern
- **Session Protection** — Filter-based route protection
- **Activity Logging** — All actions logged with IP address

## Quick Start

### 1. Database Setup
```sql
mysql -u root -p < database/schema.sql
```

### 2. Configure Connection
Edit `src/com/util/DBConnection.java` with your MySQL credentials.

### 3. Deploy to Tomcat
Import as Dynamic Web Project in Eclipse → Run on Server.

### 4. Access
Open `http://localhost:8080/SecurePasswordManager/login.jsp`

## Architecture

```
MVC Pattern:
  Model     → com.model    (User, Vault, Log beans)
  View      → WebContent/  (JSP pages)
  Controller → com.controller (Servlets)
  
Supporting:
  DAO Layer → com.dao      (Database operations)
  Utilities → com.util     (Security, DB, Filters)
```

## Sample Users

| Username | Password | Notes |
|---|---|---|
| john_doe | Test@1234 | Active account |
| jane_smith | Test@1234 | Active account |
| locked_user | Test@1234 | Locked (demo) |

## License

Educational project — built for learning and portfolio demonstration.
