🧳 Lost & Found Management System

A full-stack Lost & Found Portal designed to help students report, discover, and claim lost or found items in an institution.
The system provides secure authentication, item reporting, and claim management with a real database backend.

🔗 Live Demo:

https://lostandfound-portal-production.up.railway.app

🔗 GitHub Repository:

https://github.com/ArpitUniyal/Lost_and_found-Portal

📌 Project Overview

The Lost & Found Management System digitizes the traditional lost-and-found process by allowing users to:

Register and authenticate securely

Report lost or found items

Browse reported items

Claim found items

Track claim status

The project is built using a Node.js backend, MySQL database, and a responsive HTML/CSS/JavaScript frontend, and is fully deployed on the cloud.

🚀 Features

🔐 User Authentication

Secure registration and login using JWT

📦 Lost Item Reporting

Submit details of lost items

🔎 Found Item Reporting

Report items that have been found

🧾 Claim Management

Users can claim found items

Claim status tracking

🗄️ Relational Database Design

Proper normalization with foreign key constraints

🌐 Public Cloud Deployment

Accessible from any device and network

🛠️ Tech Stack
Frontend

HTML5

CSS3

Vanilla JavaScript (Fetch API)

Backend

Node.js

Express.js

JSON Web Tokens (JWT)

bcrypt (password hashing)

Database

MySQL

Relational schema with foreign keys

Deployment

Railway (Backend + MySQL)

GitHub (Version Control)

🗂️ Project Structure
Lost_and_found-Portal/
│
├── public/
│   ├── assets/
│   ├── uploads/
│   │   └── .gitkeep
│   ├── index.html
│   ├── styles.css
│   └── script.js
│
├── routes/
│   ├── auth.js
│   ├── lostItems.js
│   ├── foundItems.js
│   └── claims.js
│
├── database/
│   └── schema.sql
│
├── config/
│   └── database.js
│
├── server.js
├── package.json
├── package-lock.json
├── env.example
└── README.md

🧠 Database Design
Tables

students – user accounts

categories – item categorization

lost_items – lost item reports

found_items – found item reports

claims – claim requests and status

The schema enforces referential integrity using foreign keys.

🔐 Environment Variables

Create the following variables in your deployment environment:

DB_HOST
DB_PORT
DB_USER
DB_PASSWORD
DB_NAME
JWT_SECRET
PORT


Sensitive values are never committed to GitHub.

▶️ Running the Project Locally
# Install dependencies
npm install

# Start the server
npm start


Access locally at:

http://localhost:3000

🌍 Deployment Details

The backend and database are deployed on Railway

MySQL is used as the production database

The application is publicly accessible via a single URL

File uploads are stored locally (ephemeral on free tier)
