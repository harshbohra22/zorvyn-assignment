# Finance Backend API - Zorvyn Assignment

This is the backend implementation for the Finance Data Processing and Access Control assignment. I built this using **Spring Boot 3** and **Java 17**. 

My goal with this project was to go beyond basic CRUD and showcase structural thinking, security, and production-ready practices like rate limiting and audit logging.

## Tech Stack
- **Java 17 / Spring Boot 3.2.4**
- **Spring Security (JWT)** for stateless authentication
- **H2 Database** via Spring Data JPA (chosen for zero-config evaluation)
- **Bucket4j** for API rate limiting
- **JUnit 5 / Mockito** for unit testing
- **Docker** for seamless deployment

## Quick Start (How to Run)

I wanted to make it as easy as possible to review this code, so you have two options:

**Option 1: Using Docker (Recommended)**
```bash
docker-compose up --build
```

**Option 2: Using Maven**
```bash
./mvnw spring-boot:run
```

Once running, the API will be available at `http://localhost:8080`.

**Interactive API Documentation:**
I've included Swagger UI so you can easily test the endpoints without needing Postman. Just head over to:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**Pre-seeded Data:**
To save you time, the app automatically seeds some rich data on the first run:
- **Admin**: `admin@finance.com` / `admin123`
- **Analyst**: `analyst@finance.com` / `analyst123`
- **Viewer**: `viewer@finance.com` / `viewer123`
- Pre-seeded financial records going back a few months so the dashboard endpoints immediately return meaningful analytics.

---

## Architecture & Engineering Decisions

Here are a few specific decisions I made while building this:

### 1. Role-Based Access Control (RBAC)
Instead of hardcoding checks into the services, I used Spring Security's `@PreAuthorize` at the controller level. This makes the security boundaries very explicit:
- `Viewer`: Can only access the dashboard metrics.
- `Analyst`: Can view records and dashboard metrics contextually.
- `Admin`: Full CRUD over records and user management.

### 2. Soft Deletion
Financial data shouldn't be permanently deleted for compliance reasons. I added an `isDeleted` flag to the `FinancialRecord` entity. Deleting a record hides it from all queries using JPA `@Query` rules, rather than issuing a SQL `DELETE`.

### 3. Audit & Request Logging
I wanted to ensure traceability:
- **Audit Logging**: Any time an Admin creates, updates, or deletes a record or user, an `AuditLog` is saved tracking the action, the entity ID, and exactly who performed it.
- **Request Interceptor**: I wrote a `OncePerRequestFilter` to log every API request, tracking the HTTP method, endpoint, response status, and processing time.

### 4. Rate Limiting protection
To protect the dashboard APIs against abuse, I integrated `Bucket4j`. All APIs are intercepted by a `RateLimitFilter` giving a simple sliding window allowance (100 requests per minute per IP). Exceeding this returns a `429 Too Many Requests` response.

### 5. Big Decimal for Currency
Floating-point precision errors (like $0.1 + $0.2 != $0.3) are a classic mistake in finance apps. I used `BigDecimal` for all monetary amounts to ensure calculations are absolutely strict.

### 6. Centralized Error Handling
Instead of throwing raw stack traces, I wrote a `@ControllerAdvice` global exception handler. Whether it's a 404 (Resource Not Found), a 400 (Validation failure), or a 403 (Access Denied), the backend consistently returns a standard JSON envelope:
```json
{
  "success": false,
  "message": "Meaningful error message here."
}
```

---

## API Overview

If you prefer testing manually, here are the core routes:

### Authentication
- `POST /api/auth/register` - Create a new user account
- `POST /api/auth/login` - Authenticate and get JWT

### Financial Records (Admin/Analyst)
- `POST /api/records` - Create an income/expense record
- `GET /api/records` - List records. Supports filtering (`?type=INCOME&category=Salary&startDate=...`) and pagination (`?page=0&size=10`).
- `PUT /api/records/{id}` - Update record
- `DELETE /api/records/{id}` - Soft delete a record

### Dashboard Analytics
- `GET /api/dashboard/summary` - Total net worth, income, expenses
- `GET /api/dashboard/category-summary` - Grouped totals by category
- `GET /api/dashboard/trends` - Montly breakdown
- `GET /api/dashboard/recent` - Top 10 recent transactions

### User Management (Admin Only)
- `GET /api/users` - List all users
- `PUT /api/users/{id}` - Change user role or active status
- `DELETE /api/users/{id}` - Expel user

---
## Running Tests
I've included unit tests for the core service layers using JUnit 5 and Mockito to ensure the business logic is covered.
```bash
./mvnw test
```

Thanks for reviewing my code, I really enjoyed building this!
