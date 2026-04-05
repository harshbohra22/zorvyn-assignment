# Finance Data Processing & Access Control Backend

A Spring Boot backend for a finance dashboard system with role-based access control, financial records management, and analytics APIs.

## Tech Stack

- **Java 17** + **Spring Boot 3.2.4**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with **H2** (file-based) database
- **Jakarta Bean Validation** for input validation
- **SpringDoc OpenAPI** for auto-generated Swagger documentation
- **Lombok** for reducing boilerplate
- **Maven** for build management

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+ (or use the included Maven wrapper)

### Run the Application

```bash
# Clone the repository
git clone <repository-url>
cd assignment

# Build and run
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

### Useful URLs

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI — interactive API docs |
| `http://localhost:8080/h2-console` | H2 Database Console (JDBC URL: `jdbc:h2:file:./data/financedb`) |

### Default Admin Credentials

A default admin user is seeded on first startup:
- **Email:** `admin@finance.com`
- **Password:** `admin123`

---

## Architecture

```
com.finance/
├── config/          # Security config, JWT utility, data seeder
├── controller/      # REST API controllers
├── dto/
│   ├── request/     # Incoming request DTOs with validation
│   └── response/    # Outgoing response DTOs
├── entity/          # JPA entities
├── enums/           # Role, RecordType enums
├── exception/       # Custom exceptions + global handler
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic layer
```

### Design Principles
- **Separation of Concerns**: Controllers handle HTTP, services handle business logic, repositories handle data access
- **DTO Pattern**: Request/response objects separate API contracts from internal entities
- **Global Exception Handling**: `@ControllerAdvice` ensures consistent error JSON across all endpoints
- **Stateless Auth**: JWT tokens — no server-side session storage

---

## API Documentation

### Authentication

#### POST `/api/auth/register`
Register a new user (defaults to VIEWER role).

```json
// Request
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}

// Response (201)
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUz...",
    "type": "Bearer",
    "email": "john@example.com",
    "name": "John Doe",
    "role": "VIEWER"
  }
}
```

#### POST `/api/auth/login`
```json
// Request
{
  "email": "admin@finance.com",
  "password": "admin123"
}

// Response (200)
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUz...",
    "type": "Bearer",
    "email": "admin@finance.com",
    "name": "System Admin",
    "role": "ADMIN"
  }
}
```

### User Management (Admin Only)

All endpoints require `Authorization: Bearer <admin_token>`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user role/status/name |
| DELETE | `/api/users/{id}` | Delete user |

#### PUT `/api/users/{id}` — Update user
```json
{
  "name": "Updated Name",
  "role": "ANALYST",
  "isActive": true
}
```

### Financial Records

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/records` | Admin | Create record |
| GET | `/api/records` | Admin, Analyst | List with filters & pagination |
| GET | `/api/records/{id}` | Admin, Analyst | Get single record |
| PUT | `/api/records/{id}` | Admin | Update record |
| DELETE | `/api/records/{id}` | Admin | Soft-delete record |

#### POST `/api/records` — Create record
```json
{
  "type": "INCOME",
  "category": "salary",
  "amount": 5000.00,
  "date": "2024-03-15",
  "description": "Monthly salary"
}
```

#### GET `/api/records` — Filtered listing
Query Parameters:
- `type` — `INCOME` or `EXPENSE`
- `category` — filter by category name
- `startDate` — ISO date (e.g., `2024-01-01`)
- `endDate` — ISO date (e.g., `2024-12-31`)
- `page` — page number (default: 0)
- `size` — page size (default: 10)

Example: `GET /api/records?type=INCOME&category=salary&page=0&size=5`

### Dashboard / Analytics

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/dashboard/summary` | All Authenticated | Total income, expenses, net balance |
| GET | `/api/dashboard/category-summary` | Analyst, Admin | Category-wise breakdown |
| GET | `/api/dashboard/trends` | Analyst, Admin | Monthly income/expense trends |
| GET | `/api/dashboard/recent` | All Authenticated | Last 10 transactions |

---

## Role-Based Access Control (RBAC)

Three roles with tiered permissions:

| Action | Viewer | Analyst | Admin |
|--------|--------|---------|-------|
| View dashboard summary | ✅ | ✅ | ✅ |
| View recent transactions | ✅ | ✅ | ✅ |
| View financial records | ❌ | ✅ | ✅ |
| View category/trend analytics | ❌ | ✅ | ✅ |
| Create/Update/Delete records | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

**Implementation**: `@PreAuthorize` annotations on controller methods enforce role checks. The `JwtAuthFilter` extracts roles from JWT tokens and populates Spring Security context.

---

## Error Handling

All errors return consistent JSON:

```json
{
  "success": false,
  "message": "Error description"
}
```

| Status | Scenario |
|--------|----------|
| 400 | Validation error (missing/invalid fields) |
| 401 | Missing or invalid JWT token |
| 403 | Insufficient role permissions |
| 404 | Resource not found |
| 409 | Duplicate resource (e.g., email already exists) |
| 500 | Unexpected server error |

Validation errors include field-level detail:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email must be valid",
    "amount": "Amount must be greater than 0"
  }
}
```

---

## Assumptions & Design Decisions

1. **H2 File-Based Database**: Chosen for zero-config setup. Data persists in `./data/financedb` across restarts. Easily switchable to PostgreSQL/MySQL by changing `application.properties`.

2. **New users default to VIEWER role**: Only an Admin can promote users to ANALYST or ADMIN via the user management API.

3. **Soft Delete for Records**: Financial records are not permanently deleted — they are flagged with `isDeleted = true`. This preserves audit trails.

4. **Stateless JWT Authentication**: No server-side sessions. Each request must include a valid `Authorization: Bearer <token>` header.

5. **BCrypt Password Hashing**: Industry-standard password hashing via Spring Security's `BCryptPasswordEncoder`.

6. **BigDecimal for Amounts**: Avoids floating-point precision issues inherent in financial calculations.

7. **Pagination**: Record listing uses Spring Data's `Pageable` for efficient data retrieval on large datasets.

---

## Project Structure

```
src/main/java/com/finance/
├── FinanceApplication.java              # Main entry point
├── config/
│   ├── DataSeeder.java                  # Seeds default admin user
│   ├── JwtAuthFilter.java              # JWT authentication filter
│   ├── JwtUtil.java                     # JWT token utility
│   └── SecurityConfig.java             # Spring Security configuration
├── controller/
│   ├── AuthController.java             # POST /api/auth/register, /login
│   ├── DashboardController.java        # GET /api/dashboard/*
│   ├── RecordController.java           # CRUD /api/records
│   └── UserController.java            # CRUD /api/users (admin)
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RecordRequest.java
│   │   ├── RegisterRequest.java
│   │   └── UpdateUserRequest.java
│   └── response/
│       ├── ApiResponse.java
│       ├── AuthResponse.java
│       ├── CategorySummary.java
│       ├── DashboardSummary.java
│       ├── MonthlyTrend.java
│       ├── RecordResponse.java
│       └── UserResponse.java
├── entity/
│   ├── FinancialRecord.java
│   └── User.java
├── enums/
│   ├── RecordType.java
│   └── Role.java
├── exception/
│   ├── DuplicateResourceException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── RecordRepository.java
│   └── UserRepository.java
└── service/
    ├── AuthService.java
    ├── DashboardService.java
    ├── RecordService.java
    └── UserService.java
```
