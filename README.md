# Spring Boot REST API — Product Management

A complete **Spring Boot 3** REST API demonstrating all five HTTP methods, SQL database integration via Spring Data JPA, bean validation, global exception handling, and pagination with sorting.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Component Explanation](#component-explanation)
- [How the Request Flow Works](#how-the-request-flow-works)
- [API Endpoints](#api-endpoints)
- [Pagination & Sorting](#pagination--sorting)
- [Validation & Error Handling](#validation--error-handling)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [Database Configuration](#database-configuration)

---

## Architecture Overview

```
Client (Postman / Browser / Frontend)
        │
        ▼
┌──────────────────┐
│   Controller     │  ← receives HTTP requests, returns HTTP responses
├──────────────────┤
│   Service        │  ← business logic, data transformation
├──────────────────┤
│   Repository     │  ← database access (auto-generated SQL)
├──────────────────┤
│   Entity         │  ← Java class mapped to a database table
├──────────────────┤
│   Database       │  ← H2 (dev) or PostgreSQL (prod)
└──────────────────┘
```

---

## Project Structure

```
src/main/java/com/example/productapi/
├── ProductApiApplication.java          # Entry point — starts the Spring Boot app
├── controller/
│   └── ProductController.java          # REST endpoints (POST, GET, PUT, PATCH, DELETE)
├── service/
│   ├── ProductService.java             # Service interface (contract)
│   └── ProductServiceImpl.java         # Service implementation (business logic)
├── repository/
│   └── ProductRepository.java          # Database access interface (Spring Data JPA)
├── entity/
│   └── Product.java                    # JPA entity mapped to the "products" table
├── dto/
│   ├── ProductRequest.java             # Input DTO for POST & PUT (with validation)
│   ├── ProductPatchRequest.java        # Input DTO for PATCH (all fields optional)
│   ├── ProductResponse.java            # Output DTO returned to the client
│   ├── PagedResponse.java              # Wrapper for paginated results
│   └── ErrorResponse.java             # Standardised error JSON structure
└── exception/
    ├── ResourceNotFoundException.java  # Custom 404 exception
    └── GlobalExceptionHandler.java     # Catches exceptions → returns clean JSON errors
```

---

## Component Explanation

### 1. Entity (`Product.java`)

The **Entity** is a plain Java class annotated with `@Entity`. JPA (Java Persistence API) maps it directly to a **database table**. Each field becomes a column.

| Annotation         | What it does                                                    |
|--------------------|-----------------------------------------------------------------|
| `@Entity`          | Marks the class as a JPA entity (database table)               |
| `@Table`           | Customises the table name (defaults to class name)              |
| `@Id`              | Marks the primary key field                                     |
| `@GeneratedValue`  | Auto-generates the ID (IDENTITY = database auto-increment)      |
| `@Column`          | Customises column properties (nullable, length, precision)      |
| `@CreationTimestamp`| Automatically sets the timestamp when the row is first created |
| `@UpdateTimestamp`  | Automatically updates the timestamp on every save              |

**Think of it as:** _"This Java class IS a database table."_

---

### 2. Repository (`ProductRepository.java`)

The **Repository** is an interface that extends `JpaRepository<Product, Long>`. You **don't write any implementation** — Spring Data JPA generates all the SQL at runtime.

What you get for free by extending `JpaRepository`:

| Method                    | SQL equivalent                     |
|---------------------------|------------------------------------|
| `save(product)`           | `INSERT INTO products ...`         |
| `findById(id)`            | `SELECT * FROM products WHERE id=?`|
| `findAll(pageable)`       | `SELECT * ... LIMIT ? OFFSET ?`    |
| `deleteById(id)`          | `DELETE FROM products WHERE id=?`  |
| `count()`                 | `SELECT COUNT(*) FROM products`    |

Custom query methods like `findByCategory(String category, Pageable pageable)` are derived from the method name — Spring parses `findBy` + `Category` and generates the `WHERE category = ?` clause automatically.

**Think of it as:** _"I describe WHAT data I want; Spring writes the SQL."_

---

### 3. Service (`ProductService.java` + `ProductServiceImpl.java`)

The **Service** layer contains **business logic**. It sits between the Controller and the Repository.

Why not call the Repository directly from the Controller?

- **Separation of concerns** — the controller handles HTTP; the service handles logic.
- **Reusability** — multiple controllers (or scheduled jobs) can use the same service.
- **Testability** — you can mock the service in controller tests and mock the repository in service tests.
- **Transaction management** — `@Transactional` ensures database operations are atomic.

The service converts between **DTOs** (what the client sees) and **Entities** (what the database sees).

**Think of it as:** _"The middleman that transforms, validates, and orchestrates."_

---

### 4. Controller (`ProductController.java`)

The **Controller** is the entry point for HTTP requests. Annotations map URLs and HTTP methods to Java methods:

| Annotation       | HTTP Method | Purpose                     |
|------------------|-------------|-----------------------------|
| `@PostMapping`   | POST        | Create a new resource       |
| `@GetMapping`    | GET         | Read a resource             |
| `@PutMapping`    | PUT         | Replace a resource entirely |
| `@PatchMapping`  | PATCH       | Update specific fields      |
| `@DeleteMapping` | DELETE      | Remove a resource           |

Key annotations on parameters:

- `@RequestBody` — deserialises the JSON body into a Java object
- `@PathVariable` — extracts a value from the URL path (e.g., `/products/{id}`)
- `@RequestParam` — extracts query parameters (e.g., `?page=0&size=10`)
- `@Valid` — triggers bean validation on the request body

**Think of it as:** _"The front desk — receives requests, delegates work, returns results."_

---

### 5. DTOs (Data Transfer Objects)

DTOs decouple the **API contract** from the **database schema**.

| DTO                 | Used for                                             |
|---------------------|------------------------------------------------------|
| `ProductRequest`    | POST & PUT — all fields required, validated          |
| `ProductPatchRequest` | PATCH — all fields optional, only non-null applied |
| `ProductResponse`   | Response body — what the client receives             |
| `PagedResponse<T>`  | Wraps a list with pagination metadata                |
| `ErrorResponse`     | Standardised error body with status, message, errors |

**Think of it as:** _"The shape of data travelling across the wire."_

---

### 6. Exception Handling (`GlobalExceptionHandler.java`)

The `@RestControllerAdvice` class catches exceptions thrown anywhere in the application and converts them into a consistent JSON error response.

| Exception                          | HTTP Status | When it happens                |
|------------------------------------|-------------|--------------------------------|
| `ResourceNotFoundException`        | 404         | Product ID doesn't exist       |
| `MethodArgumentNotValidException`  | 400         | Validation fails on @Valid DTO |
| `Exception` (catch-all)            | 500         | Any unexpected error           |

**Think of it as:** _"A safety net that translates Java exceptions into HTTP error responses."_

---

## How the Request Flow Works

Here's what happens when you send `POST /api/products`:

```
1. Client sends HTTP POST with JSON body
                    │
2. Spring routes to ProductController.createProduct()
                    │
3. @Valid triggers validation on ProductRequest
   ├─ FAIL → MethodArgumentNotValidException → GlobalExceptionHandler → 400 JSON
   └─ PASS ↓
                    │
4. Controller calls productService.createProduct(request)
                    │
5. Service converts DTO → Entity, calls productRepository.save(entity)
                    │
6. Repository executes INSERT INTO products ...
                    │
7. Database returns saved entity with generated ID
                    │
8. Service converts Entity → ProductResponse DTO
                    │
9. Controller wraps in ResponseEntity with 201 CREATED status
                    │
10. Client receives JSON response
```

---

## API Endpoints

### Create a Product

```
POST /api/products
Content-Type: application/json

{
  "name": "MacBook Pro",
  "description": "Apple laptop with M3 chip",
  "price": 1999.99,
  "quantity": 50,
  "category": "Electronics"
}
```

### Get a Single Product

```
GET /api/products/1
```

### List All Products (Paginated & Sorted)

```
GET /api/products?page=0&size=10&sortBy=price&direction=desc
```

### Filter by Category

```
GET /api/products?category=Electronics
```

### Search by Name

```
GET /api/products?search=macbook
```

### Update a Product (Full Replace)

```
PUT /api/products/1
Content-Type: application/json

{
  "name": "MacBook Pro 2025",
  "description": "Updated description",
  "price": 2199.99,
  "quantity": 30,
  "category": "Electronics"
}
```

### Partial Update

```
PATCH /api/products/1
Content-Type: application/json

{
  "price": 1899.99
}
```

### Delete a Product

```
DELETE /api/products/1
→ 204 No Content
```

---

## Pagination & Sorting

The `GET /api/products` endpoint supports:

| Parameter   | Default | Description                          |
|-------------|---------|--------------------------------------|
| `page`      | `0`     | Page number (0-indexed)              |
| `size`      | `10`    | Number of items per page             |
| `sortBy`    | `id`    | Field to sort by (name, price, etc.) |
| `direction` | `asc`   | Sort direction (`asc` or `desc`)     |

**Example response:**

```json
{
  "content": [ { "id": 1, "name": "Laptop", ... } ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "last": false
}
```

**How it works internally:**

1. The controller builds a `Pageable` object from query params.
2. Spring Data JPA translates it into `LIMIT` and `OFFSET` SQL.
3. The `Page<Product>` result contains both the data and metadata (total count, total pages).
4. The service maps it into a `PagedResponse` DTO.

---

## Validation & Error Handling

### Validation annotations used in `ProductRequest`:

| Annotation     | What it checks                                |
|----------------|-----------------------------------------------|
| `@NotBlank`    | String must not be null, empty, or whitespace |
| `@NotNull`     | Value must not be null                        |
| `@Size`        | String length within min/max bounds           |
| `@DecimalMin`  | Number must be ≥ specified value              |
| `@Min`         | Integer must be ≥ specified value             |

### Example validation error response (400):

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-01-01T12:00:00",
  "errors": {
    "name": "Product name is required",
    "price": "Price must be at least 0.01",
    "category": "Category is required"
  }
}
```

### Example not-found error response (404):

```json
{
  "status": 404,
  "message": "Product not found with id: 99",
  "timestamp": "2025-01-01T12:00:00"
}
```

---

## Running the Application

### Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+** (or use the Maven wrapper)

### Quick Start (H2 in-memory database — no setup needed)

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080/api/products`.
The H2 console is at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:productdb`).

### With PostgreSQL

1. Create a database: `CREATE DATABASE productdb;`
2. Run with the `postgres` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Or set environment variables for custom credentials:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/productdb \
SPRING_DATASOURCE_USERNAME=myuser \
SPRING_DATASOURCE_PASSWORD=mypass \
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## Running Tests

```bash
./mvnw test
```

The tests use H2 in-memory database and cover all HTTP methods including pagination, validation errors, and 404 handling.

---

## Database Configuration

| Profile    | Database   | Connection                             | DDL Strategy   |
|------------|------------|----------------------------------------|----------------|
| (default)  | H2         | `jdbc:h2:mem:productdb` (in-memory)    | `create-drop`  |
| `postgres` | PostgreSQL | `jdbc:postgresql://localhost:5432/productdb` | `update` |

- **`create-drop`** — creates tables on startup, drops them on shutdown (perfect for development).
- **`update`** — creates/alters tables to match entities but never drops (safe for production).
