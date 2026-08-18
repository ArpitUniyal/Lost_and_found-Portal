# Lost & Found Portal

A **college campus Lost & Found Portal** designed to help students report lost or found belongings, discover matching items, verify ownership, and coordinate the return of items through a structured claim workflow.

## Overview

The portal provides two main item flows:

- **Lost Items** — students can report and manage items they have lost.
- **Found Items** — students can report items they have found on campus.

When a student believes a found item belongs to them, the system provides an automated claim-verification process. The reported location is compared with the location stored for the found item. A successful match automatically approves the claim; an unsuccessful verification is rejected.

The return process uses **two-sided confirmation**, allowing both the claimer and finder to confirm the handover before the item is finally closed.

## Key Features

### Student Authentication
- Student registration and login
- JWT-based authentication
- Password hashing with bcrypt
- Protected operations based on the authenticated student

### Lost Item Management
- Report lost items with item details, location, date/time, category, description, and optional image
- View and search reported lost items
- Manage items reported by the logged-in student

### Found Item Management
- Report found items with relevant item and location details
- View and search found items
- Track the status of found items
- Prevent inappropriate claims on items that are already locked or closed

### Automated Claim Verification
The claim process is based on the information provided by the potential owner.

```text
Request Claim
     │
     ▼
Enter verification location
     │
     ▼
Compare with found-item location
     │
 ┌───┴────┐
 ▼        ▼
Match   No Match
 │        │
 ▼        ▼
Approved Rejected
```

Location comparison normalizes capitalization and extra whitespace before checking for an exact match.

The system also limits rejected verification attempts to **three attempts per user, per found item, per day**.

### Claim & Return Workflow

After a claim is automatically approved, both participants can confirm the return independently.

```text
                 Claim Approved
                       │
             ┌─────────┴─────────┐
             │                   │
      Claimer confirms      Finder confirms
             │                   │
             ▼                   ▼
      claimer_marked        finder_marked
             │                   │
             └─────────┬─────────┘
                       ▼
                Both confirmed
                       │
                       ▼
                   completed
                       │
                       ▼
                Found item closed
```

If the **claimer confirms first**, the finder receives a claim card containing the claimant's details and can confirm the return.

If the **finder confirms first**, the claimer receives a claim card containing the finder's details and can confirm the return.

### Finder-to-Owner Notification

A finder can notify the student associated with a lost-item report when they believe they have found that item.

The system links the relevant lost and found records through the `claims` table and prevents conflicting active notifications.

### Claim Locking

The application tracks found and lost items that are already involved in an active claim/notification or have been completed/closed.

This prevents users from creating conflicting claim operations for the same item.

## Technology Stack

### Frontend
- HTML5
- CSS3
- JavaScript
- Font Awesome

### Backend
- Node.js
- Express.js
- REST APIs
- JWT
- bcryptjs
- Express Validator
- Multer

### Database
- MySQL
- mysql2

## Application Architecture

```text
┌───────────────────────────────┐
│          Frontend             │
│       HTML / CSS / JS         │
└───────────────┬───────────────┘
                │
                │ REST API + JWT
                ▼
┌───────────────────────────────┐
│        Node.js / Express      │
│                               │
│  Authentication              │
│  Lost Items                  │
│  Found Items                 │
│  Claims                      │
└───────────────┬───────────────┘
                │
                ▼
┌───────────────────────────────┐
│            MySQL              │
│                               │
│ students                     │
│ lost_items                   │
│ found_items                  │
│ claims                       │
│ categories                   │
└───────────────────────────────┘
```

## Database Relationships

The main entities are:

```text
students
   │
   ├──────────────► lost_items
   │
   ├──────────────► found_items
   │
   └──────────────► claims
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
        lost_items              found_items
```

- `lost_items.student_id` identifies the student who reported the lost item.
- `found_items.finder_id` identifies the student who reported the found item.
- `claims.claimer_id` identifies the student attempting to claim a found item.
- `claims.lost_item_id` links a claim to a lost-item report when applicable.
- `claims.found_item_id` links a claim to a found-item report.

## Claim Statuses

The claim workflow uses the following states:

| Status | Meaning |
|---|---|
| `approved` | Verification succeeded |
| `rejected` | Verification failed |
| `claimer_marked` | Claimer confirmed the handover |
| `finder_marked` | Finder confirmed the handover |
| `pending` | Claim is in an intermediate state |
| `completed` | Both sides have completed the confirmation process |

Found items are ultimately marked:

```text
closed
```

when the claim is successfully completed.

## Main API Areas

### Authentication

```text
/api/auth
```

Handles student registration, login, and authenticated user information.

### Lost Items

```text
/api/lost-items
```

Handles creation, retrieval, and management of lost-item reports.

### Found Items

```text
/api/found-items
```

Handles creation, retrieval, and management of found-item reports.

### Claims

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
│   ├── index.html
│   ├── script.js
│   ├── styles.css
│   └── uploads/
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
├── server.js
├── env.example
└── package.json
```

## Security & Validation

The backend includes:

- JWT authentication for protected endpoints
- bcrypt password hashing
- Request validation using Express Validator
- Authorization checks for user-owned records
- Protection against users claiming their own found-item reports
- Claim-state validation
- Rejected-attempt limits
- Locked-item checks
- Controlled claim completion and item closure

## Project Goal

The project focuses on creating a structured digital process for handling campus lost-and-found items:

> **Report → Discover → Verify → Claim → Confirm → Return**

Instead of relying on manual coordination alone, the system uses automated verification and a two-party confirmation workflow to make the return process more organized and reliable.
