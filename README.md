# Lost & Found Portal

A web-based **college campus Lost & Found Portal** designed to help students report lost or found belongings, discover matching items, verify ownership, and coordinate the return of items through a structured and secure claim workflow.

The platform provides a centralized system where students can manage lost and found item reports, submit claims, verify ownership, and complete the return process.

---

## Overview

The portal provides two main item flows:

- **Lost Items** — Students can report and manage items they have lost.
- **Found Items** — Students can report items they have found on campus.

When a student believes a found item belongs to them, the system provides an automated claim-verification process. The verification location provided by the claimant is compared with the location stored for the found item.

The return process uses **two-sided confirmation**, allowing both the claimer and finder to confirm the handover before the item is marked as completed.

---

# Key Features

## Student Authentication

- Student registration and login
- JWT-based authentication
- Secure password hashing using BCrypt
- Protected API endpoints
- Forgot Password functionality
- Email-based password reset
- Secure, time-limited password reset tokens
- One-time password reset links

---

## Lost Item Management

- Report lost items with item details
- Add location, date/time, category, and description
- Optional image upload support
- View and search reported lost items
- Manage items reported by the logged-in student

---

## Found Item Management

- Report found items with relevant item and location details
- View and search found items
- Track found item status
- Prevent inappropriate claims on locked or completed items

---

## Automated Claim Verification

The claim process verifies ownership using the information provided by the potential owner.

```text
Request Claim
     │
     ▼
Enter Verification Location
     │
     ▼
Compare with Found Item Location
     │
 ┌───┴────┐
 ▼        ▼
Match   No Match
 │        │
 ▼        ▼
Approved Rejected
```

The location comparison normalizes capitalization and extra whitespace before checking for an exact match.

The system also limits rejected verification attempts to **three attempts per user, per found item, per day**.

---

## Claim & Return Workflow

After a claim is approved, both participants can independently confirm the return.

```text
                 Claim Approved
                      │
             ┌────────┴────────┐
             │                 │
      Claimer confirms    Finder confirms
             │                 │
             ▼                 ▼
      claimer_marked      finder_marked
             │                 │
             └────────┬────────┘
                      │
                      ▼
                Both Confirmed
                      │
                      ▼
                  Completed
                      │
                      ▼
              Found Item Closed
```

- If the **claimer confirms first**, the finder receives the claim details and can confirm the return.
- If the **finder confirms first**, the claimer receives the relevant information and can confirm the handover.
- Once both parties confirm, the claim is completed and the found item is closed.

---

## Finder-to-Owner Notification

A finder can notify the student associated with a lost-item report when they believe they have found the item.

The system links relevant lost and found records through the claim workflow and prevents conflicting active notifications.

---

## Claim Locking

The application tracks found and lost items that are already involved in an active claim or notification.

This prevents conflicting claim operations and ensures that completed or locked items cannot be claimed again.

---

# Password Reset Flow

Users can reset their password using their registered email address.

```text
Forgot Password
       │
       ▼
Enter Registered Email
       │
       ▼
Generate Secure Reset Token
       │
       ▼
Store Token + Expiry
       │
       ▼
Send Reset Link via Email
       │
       ▼
User Sets New Password
       │
       ▼
Password Hashed with BCrypt
       │
       ▼
Reset Token Invalidated
```

### Security Features

- Password reset tokens are time-limited.
- Tokens are linked to a specific student account.
- Passwords are stored using BCrypt hashing.
- Reset tokens are invalidated after successful password reset.
- The same reset link cannot be reused.

---

# Technology Stack

## Frontend

- HTML5
- CSS3
- JavaScript
- Font Awesome

## Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- RESTful APIs
- JWT Authentication
- BCrypt Password Encoder
- Spring Mail
- Maven

## Database

- MySQL
- Hibernate / JPA

---

# Application Architecture

```text
┌───────────────────────────────┐
│           Frontend            │
│       HTML / CSS / JS         │
└───────────────┬───────────────┘
                │
                │ REST API + JWT
                ▼
┌───────────────────────────────┐
│       Spring Boot Backend     │
│                               │
│  Authentication               │
│  Lost Items                   │
│  Found Items                  │
│  Claims                       │
│  Email Service                │
│  Password Reset               │
└───────────────┬───────────────┘
                │
                │ Spring Data JPA / Hibernate
                ▼
┌───────────────────────────────┐
│             MySQL             │
│                               │
│  students                     │
│  lost_items                   │
│  found_items                  │
│  claims                       │
│  categories                   │
└───────────────────────────────┘
```

---

# Database Relationships

```text
students
   │
   ├──────────────► lost_items
   │
   ├──────────────► found_items
   │
   └──────────────► claims
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
         lost_items            found_items
```

- `lost_items.student_id` identifies the student who reported the lost item.
- `found_items.finder_id` identifies the student who reported the found item.
- `claims.claimer_id` identifies the student attempting to claim a found item.
- `claims.lost_item_id` links a claim to a lost-item report when applicable.
- `claims.found_item_id` links a claim to a found-item report.

---

# Claim Statuses

| Status | Meaning |
|---|---|
| `approved` | Verification succeeded |
| `rejected` | Verification failed |
| `claimer_marked` | Claimer confirmed the handover |
| `finder_marked` | Finder confirmed the handover |
| `pending` | Claim is in an intermediate state |
| `completed` | Both parties confirmed the return |

Once the claim is successfully completed, the corresponding found item is marked as:

```text
closed
```

---

# Main API Areas

## Authentication

```text
/api/auth
```

Handles:

- Student registration
- Student login
- JWT authentication
- Forgot password
- Password reset

---

## Lost Items

```text
/api/lost-items
```

Handles creation, retrieval, search, and management of lost-item reports.

---

## Found Items

```text
/api/found-items
```

Handles creation, retrieval, search, and management of found-item reports.

---

## Claims

```text
/api/claims
```

Main claim operations include:

```text
POST  /verify-request
POST  /notify-owner

GET   /finder/pending
GET   /my
GET   /found/locked
GET   /lost/locked

PATCH /:id/claimer-confirm
PATCH /:id/finder-returned
```

---

# Project Structure

```text
lost-and-found-portal/
│
├── backend/
│   │
│   ├── .mvn/
│   │   └── wrapper/
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── lostandfound/
│   │   │   │           ├── config/
│   │   │   │           ├── controller/
│   │   │   │           ├── dto/
│   │   │   │           ├── entity/
│   │   │   │           ├── repository/
│   │   │   │           ├── security/
│   │   │   │           ├── service/
│   │   │   │           └── BackendApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       └── application-example.properties
│   │   │
│   │   ├── pom.xml
│   │   ├── mvnw
│   │   └── mvnw.cmd
│
├── frontend/
│   ├── index.html
│   ├── reset-password.html
│   ├── script.js
│   ├── styles.css
│   └── uploads/
│
├── database/
│   └── schema.sql
│
├── .gitignore
└── README.md
```

---

# Security & Validation

The application includes:

- JWT-based authentication for protected endpoints
- BCrypt password hashing
- Spring Security authorization
- Request validation
- Authorization checks for user-owned records
- Secure password reset using time-limited tokens
- One-time password reset links
- Email-based password recovery
- Protection against users claiming their own found-item reports
- Claim-state validation
- Rejected-attempt limits
- Locked-item checks
- Controlled claim completion and item closure

---

# Setup Instructions

## 1. Clone the Repository

```bash
git clone <your-repository-url>
cd lost-and-found-portal
```

---

## 2. Configure the Database

Create a MySQL database and configure your database credentials.

Copy:

```text
backend/src/main/resources/application-example.properties
```

to:

```text
backend/src/main/resources/application.properties
```

Then update the database, JWT, and email configuration with your own values.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lost_and_found
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

jwt.secret=your_jwt_secret
jwt.expiration=86400000

spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password
```

---

## 3. Run the Backend

Navigate to the backend directory:

```bash
cd backend
```

Run the Spring Boot application:

### Windows

```bash
.\mvnw spring-boot:run
```

The backend will start on:

```text
http://localhost:8080
```

---

# Project Goal

The project focuses on creating a structured digital process for handling campus lost-and-found items:

> **Report → Discover → Verify → Claim → Confirm → Return**

Instead of relying on manual coordination alone, the platform uses automated verification, secure authentication, email-based password recovery, and a two-party confirmation workflow to make the return process more organized, secure, and reliable.
