# Lost & Found Portal

A full-stack **College Campus Lost & Found Portal** that allows students to report lost or found items, verify ownership, and coordinate the return of items through a claim workflow.

## Features

- Student registration and login with JWT authentication
- Password hashing with bcrypt
- Report lost items with:
  - Item name
  - Description
  - Location
  - Date/time lost
  - Category
  - Optional image
- Report found items with similar details
- Browse active lost and found items
- Search lost and found items from the frontend
- Delete items created by the authenticated user
- Claim verification using the reported found-item location
- Automatic claim approval/rejection based on location matching
- Maximum of 3 rejected verification attempts per user per found item per day
- Finder-to-owner notification workflow
- Two-sided claim confirmation:
  - Claimer confirms the item was claimed back
  - Finder confirms the item was returned
- Claim completion when both sides confirm
- Automatically closes the found item after successful completion
- Locked-item handling to prevent conflicting claims
- Finder-side claim information with claimant contact details
- Claimer-side claim information with finder contact details

## Tech Stack

### Frontend
- HTML5
- CSS3
- JavaScript
- Font Awesome

### Backend
- Node.js
- Express.js
- REST APIs
- JWT authentication
- bcryptjs
- Express Validator
- Multer

### Database
- MySQL
- mysql2 connection pool

## Architecture

```text
Browser
   │
   ▼
HTML / CSS / JavaScript
   │
   │ REST API + JWT
   ▼
Node.js / Express
   │
   ├── Authentication
   ├── Lost Items
   ├── Found Items
   └── Claims
   │
   ▼
MySQL Database
```

Uploaded images are stored under:

```text
public/uploads/
```

## Claim Workflow

The application uses automatic verification rather than manual finder approval.

```text
User requests a found item
        │
        ▼
Location verification
        │
   ┌────┴────┐
   │         │
 Match    No Match
   │         │
   ▼         ▼
approved  rejected
   │
   ▼
Claimer / Finder confirmation
   │
   ├── Claimer confirms → claimer_marked
   │
   └── Finder confirms  → finder_marked
             │
             ▼
       Both confirm
             │
             ▼
          completed
             │
             ▼
       Found item closed
```

The system also checks existing claim states so that an item already involved in an active/completed claim is treated as unavailable for another claim.

## Main API Routes

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/profile
```

### Lost Items

```text
GET    /api/lost-items
GET    /api/lost-items/:id
POST   /api/lost-items
DELETE /api/lost-items/:id
```

### Found Items

```text
GET    /api/found-items
GET    /api/found-items/:id
POST   /api/found-items
DELETE /api/found-items/:id
```

### Claims

```text
POST  /api/claims/verify-request
POST  /api/claims/notify-owner

GET   /api/claims/finder/pending
GET   /api/claims/my
GET   /api/claims/found/locked
GET   /api/claims/lost/locked

PATCH /api/claims/:id/claimer-confirm
PATCH /api/claims/:id/finder-returned
```

## Database

The main tables are:

```text
students
lost_items
found_items
claims
categories
```

Core relationships:

```text
students
   │
   ├── lost_items
   ├── found_items
   └── claims (as claimer)

lost_items ────── claims
found_items ───── claims
```

`found_items.finder_id` identifies the student who reported the found item.

`claims.claimer_id` identifies the student attempting to claim the item.

## Project Structure

```text
Lost_and_found-Portal-main/
│
├── config/
│   └── database.js
│
├── database/
│   └── schema.sql
│
├── middleware/
│   └── auth.js
│
├── public/
│   ├── assets/
│   ├── uploads/
│   ├── index.html
│   ├── script.js
│   └── styles.css
│
├── routes/
│   ├── auth.js
│   ├── claims.js
│   ├── simple-foundItems.js
│   └── simple-lostItems.js
│
├── scripts/
│   ├── create-tables.js
│   └── init-database.js
│
├── env.example
├── package.json
└── server.js
```

## Setup

### 1. Prerequisites

Install:

- Node.js
- npm
- MySQL

### 2. Clone the repository

```bash
git clone <your-repository-url>
cd Lost_and_found-Portal-main
```

### 3. Install dependencies

```bash
npm install
```

### 4. Configure environment variables

Create a `.env` file based on `env.example`.

Example:

```env
PORT=3000
NODE_ENV=development

DB_HOST=localhost
DB_PORT=3306
DB_NAME=lost_and_found
DB_USER=root
DB_PASSWORD=your_mysql_password

JWT_SECRET=your_secret_key
JWT_EXPIRES_IN=7d

FRONTEND_URL=http://localhost:3000
```

Use your own database password and JWT secret. Do not commit `.env` to GitHub.

### 5. Create the database

Create the MySQL database:

```sql
CREATE DATABASE lost_and_found;
```

Then initialize the required tables using the project's database scripts or execute:

```text
database/schema.sql
```

### 6. Start the application

```bash
npm start
```

The server runs on:

```text
http://localhost:3000
```

Open the application in a browser:

```text
http://localhost:3000
```

For development, the project also includes a `dev` npm script.

## Environment Variables

| Variable | Purpose |
|---|---|
| `PORT` | Backend server port |
| `NODE_ENV` | Application environment |
| `DB_HOST` | MySQL host |
| `DB_PORT` | MySQL port |
| `DB_NAME` | Database name |
| `DB_USER` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Secret used to sign JWTs |
| `JWT_EXPIRES_IN` | JWT expiration period |
| `FRONTEND_URL` | Frontend URL configuration |

## Security

- Passwords are stored as bcrypt hashes rather than plain text.
- Protected endpoints require JWT authentication.
- Users can delete only their own lost/found reports.
- Users cannot claim their own found-item report.
- Claim verification limits repeated failed attempts.
- Claim status checks prevent invalid state transitions.

## License

This project is licensed under the MIT License.

