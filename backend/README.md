# Local Brand Store - Backend API

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
    - [Option 1: Docker Compose (Recommended)](#option-1-docker-compose-recommended)
    - [Option 2: Local Development](#option-2-local-development)
    - [Option 3: IDE Setup (IntelliJ IDEA)](#option-3-ide-setup-intellij-idea)

---

## Overview

E-commerce backend API built with Spring Boot, providing RESTful endpoints for product catalog, shopping cart, checkout,
and order management with JWT authentication and atomic stock management.

---

## Technology Stack

| Component  | Technology      | Version |
|------------|-----------------|---------|
| Language   | Java            | 17      |
| Framework  | Spring Boot     | 3.5.9   |
| Database   | PostgreSQL      | 13+     |
| Cache      | Redis           | 7       |
| ORM        | Hibernate (JPA) | 6.x     |
| Security   | Spring Security | 6.x     |
| Migration  | Flyway          | 10.x    |
| Build Tool | Maven           | 3.9+    |

---

## Prerequisites

- **JDK 17 or higher**
- **Maven 3.9 or higher**
- **Docker & Docker Compose** (for containerized setup)
- **PostgreSQL 13+** (for local development)
- **Redis 7+** (for local development)

---

## Setup Instructions

### Option 1: Docker Compose (Recommended)

This will start all services (Backend API, PostgreSQL, Redis) in Docker containers.

#### Step 1: Create .env file in project root

Create a `.env` file in the `local-brand-store` directory (project root):

```env
JWT_SECRET_KEY=your-secret-key-at-least-256-bits-long-here
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=http://localhost:5173
```

**Gmail App Password Setup:**

1. Enable 2-Factor Authentication in your Google account
2. Go to: https://myaccount.google.com/apppasswords
3. Generate an App Password for Mail
4. Use this password in MAIL_PASSWORD

#### Step 2: Start all services

Navigate to project root and run:

```bash
cd local-brand-store
docker-compose up -d
```

This starts:

- Backend API on port 8080
- PostgreSQL on port 5432
- Redis on port 6379

#### Step 3: Verify services are running

```bash
docker-compose ps
```

All services should show "Up" status.

#### Step 4: View logs (optional)

```bash
docker-compose logs -f backend
```

#### Step 5: Access the API

API is available at: http://localhost:8080/api/v1

#### Stop services

```bash
docker-compose down
```

---

### Option 2: Local Development

This setup runs the backend locally without Docker.

#### Step 1: Setup PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE lbs;

# Exit
\q
```

#### Step 2: Setup Redis

**Option A - Using Docker:**

```bash
docker run -d -p 6379:6379 redis:7-alpine
```

**Option B - Local installation:**

```bash
# Windows (via Chocolatey)
choco install redis-64

# Start Redis
redis-server
```

#### Step 3: Configure Environment Variables

Create a `.env` file in the `backend` directory:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/lbs
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET_KEY=your-secret-key-at-least-256-bits-long-here
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=http://localhost:5173
```

#### Step 4: Install Dependencies

```bash
cd backend
mvn clean install -DskipTests
```

#### Step 5: Run the Application

```bash
mvn spring-boot:run
```

API is available at: http://localhost:8080/api/v1

**Note:** Database migrations run automatically on startup. Refer to
`backend/src/main/resources/db/migration` to see what will be in database on first run.

---

### Option 3: IDE Setup (IntelliJ IDEA)

This setup runs the backend directly in IntelliJ IDEA, which provides easier debugging and development experience.

#### Step 1: Import Project

1. Open IntelliJ IDEA
2. Click **File > Open**
3. Navigate to `local-brand-store/backend` folder
4. Select `pom.xml` and click **Open**
5. Choose **Open as Project**
6. Wait for Maven to download dependencies

#### Step 2: Setup PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE lbs;

# Exit
\q
```

#### Step 3: Setup Redis

**Option A - Using Docker:**

```bash
docker run -d -p 6379:6379 redis:7-alpine
```

**Option B - Local installation:**

```bash
# Windows (via Chocolatey)
choco install redis-64

# Start Redis
redis-server
```

#### Step 4: Configure Environment Variables in IntelliJ

1. Open **Run > Edit Configurations**
2. Click **+** (Add New Configuration)
3. Select **Spring Boot**
4. Set configuration:
    - **Name:** LocalBrandStoreApplication
    - **Main class:** wandererpi.lbs.LocalBrandStoreApplication
    - **Environment variables:** Click **...** button and add:

```
DATABASE_URL=jdbc:postgresql://localhost:5432/lbs
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET_KEY=your-secret-key-at-least-256-bits-long-here
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=http://localhost:5173
```

5. Click **Apply** and **OK**

**Alternative - Using .env plugin:**

1. Install EnvFile plugin (File > Settings > Plugins > search "EnvFile")
2. Create `.env` file in backend directory (same format as above)
3. In Run Configuration, enable EnvFile and select the `.env` file

#### Step 5: Run the Application

1. Click the **Run** button (green triangle) next to LocalBrandStoreApplication configuration
2. Or press **Shift + F10**
3. Wait for the application to start

API is available at: http://localhost:8080/api/v1

**Note:** Database migrations run automatically on startup. Refer to
`backend/src/main/resources/db/migration` to see what will be in database on first run.

#### Step 6: Using .http Files for Testing

IntelliJ IDEA has built-in HTTP Client support:

1. Navigate to `docs/deliverables/api-documentation/` folders
2. Open any `.http` file (e.g., `authentication-api.http`)
3. Click the green arrow next to any request to execute it
4. Results will appear in the HTTP Response panel

---

## Testing the API

### Seeded Admin Account

Email: auquangkhanh@gmail.com
Password: 12345Ab!

### Example Customer Accounts

Email: nguyenvanan@gmail.com
Password: 12345Ab!

Email: tranthibinh@gmail.com
Password: 12345Ab!

### API Endpoints

**Base URL:** http://localhost:8080/api/v1

**Public Endpoints:**

- GET /product - List products
- GET /product/{id} - Get product details
- GET /category - List categories
- POST /cart/items - Add to cart
- GET /cart?cartToken={token} - Get cart
- POST /checkout/start - Reserve stock
- POST /order - Place order
- GET /order/track/{token} - Track order

**Admin Endpoints (Require JWT):**

- POST /auth/login - Login
- GET /order - List orders
- GET /order/{id} - Get order details
- PUT /order/{id}/status - Update order status
- POST /order/{id}/cancel - Cancel order

### Test with .http files

API test files are available in `docs/deliverables/api-documentation/` folders. Use VS Code with REST Client extension
or IntelliJ IDEA HTTP Client.

---

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ProductServiceTest
```

---

## Additional Resources

- API Documentation: `docs/deliverables/api-documentation/`
- Database Design: `docs/deliverables/database-design/`
- Sequence Diagrams: `docs/deliverables/sequence-diagram/`
- Requirement Analysis: `docs/deliverables/requirement-analysis/`

---
