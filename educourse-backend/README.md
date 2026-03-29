# EduCourse — Backend (Spring Boot REST API)

A full-stack online learning platform backend built with **Spring Boot 3**, **JWT authentication**, **role-based access control**, and **H2/PostgreSQL** database.

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | Spring Boot | 3.3.0 |
| Security | Spring Security + JWT (JJWT) | 6.x / 0.11.5 |
| Database | H2 (dev) / PostgreSQL (prod) | — |
| ORM | Spring Data JPA / Hibernate | 3.x |
| Validation | jakarta.validation | 3.x |
| Language | Java | 21 (LTS) |
| Build | Maven | 3.9+ |

---

## Quick Start (Local Development)

### Prerequisites
- Java 21+
- Maven 3.9+

### Run
```bash
cd educourse-final
mvn spring-boot:run
```

The API starts at **http://localhost:8080**

### Default Admin Credentials
```
Email:    elkorf@educourse.com
Password: Admin@123
```
*(Set in `src/main/resources/admin-users.properties`)*

### H2 Database Console
Access the in-memory database browser at: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:educourse`
- Username: `sa`
- Password: *(empty)*

---

## Project Structure

```
src/main/java/com/edu/educourse/
├── EduCourseApplication.java      — Entry point (@SpringBootApplication)
├── config/
│   ├── CorsConfig.java            — CORS filter (allows frontend to call API)
│   ├── DataInitializer.java       — Creates admin users at startup
│   └── SecurityConfig.java        — URL rules, JWT filter, BCrypt
├── controller/
│   ├── AdminController.java       — /api/admin/** (ADMIN only)
│   ├── AuthController.java        — /api/auth/register, /api/auth/login
│   ├── CourseController.java      — /api/courses/** (CRUD)
│   ├── FavoriteController.java    — /api/favorites/**
│   ├── PurchaseController.java    — /api/purchases/**
│   ├── UserController.java        — /api/user/me
│   └── TestController.java        — /test, /user, /admin (debug)
├── dto/                           — Data Transfer Objects (request/response shapes)
├── entity/                        — JPA entities (@Entity, @Table)
├── exception/                     — GlobalExceptionHandler + custom exceptions
├── repository/                    — Spring Data JPA interfaces
├── security/
│   └── JwtAuthFilter.java         — Validates JWT on every request
├── service/
│   ├── AuthService.java           — register() and login() logic
│   ├── CustomUserDetails.java     — Bridges User entity ↔ Spring Security
│   ├── CustomUserDetailsService.java — Loads user by email
│   └── JwtService.java            — Token generate/validate
└── util/
    └── JwtUtil.java               — Raw JJWT operations
```

---

## API Endpoints

### Public (No Authentication Required)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new student account |
| POST | `/api/auth/login` | Login — returns JWT token |
| GET | `/api/courses` | All courses |
| GET | `/api/courses/categories` | All category names |
| GET | `/api/courses/search?q=term` | Full-text search |
| GET | `/api/courses/filter?q=&category=` | Combined search + filter |

### Authenticated (Any Valid JWT)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/user/me` | Current user info + roles |
| GET | `/api/favorites` | My saved courses |
| POST | `/api/favorites/{id}` | Save a course |
| DELETE | `/api/favorites/{id}` | Remove from saved |
| GET | `/api/purchases` | My enrolled courses |
| POST | `/api/purchases/{id}` | Enroll in a course |

### Admin or Student Admin
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/courses` | Add new course |
| PUT | `/api/courses/{id}` | Update course (own only for SA) |
| DELETE | `/api/courses/{id}` | Delete course (own only for SA) |
| GET | `/api/courses/my` | My courses |

### Admin Only
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/users` | All users with roles |
| GET | `/api/admin/roles` | All available roles |
| POST | `/api/admin/make-student-admin/{email}` | Promote user to Student Admin |
| DELETE | `/api/admin/remove-student-admin/{email}` | Revoke Student Admin role |
| DELETE | `/api/admin/users/{email}` | Delete user |

---

## Role System

| Role | How Assigned | Permissions |
|------|-------------|-------------|
| `ROLE_STUDENT` | Auto on register | Browse, favorite, enroll |
| `ROLE_STUDENT_ADMIN` | Admin promotes via API | + Add/Edit/Delete own courses |
| `ROLE_ADMIN` | `admin-users.properties` only | Full access |

> Roles are **many-to-many**: a Student Admin has both `ROLE_STUDENT` and `ROLE_STUDENT_ADMIN` simultaneously.

---

## Authentication Flow

```
1. POST /api/auth/login  {email, password}
2. Server validates → returns {token: "eyJhbGci..."}
3. Client includes in all requests: Authorization: Bearer eyJhbGci...
4. JwtAuthFilter validates token on every request
5. SecurityContextHolder set → @PreAuthorize checks work
```

---

## Configuration

### `src/main/resources/application.yaml`
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:educourse   # Change to PostgreSQL for production
  jpa:
    hibernate:
      ddl-auto: create           # Use 'update' in production
jwt:
  secret: MyVeryStrongSecretKey  # Use env variable in production
  expiration: 86400000           # 24 hours in milliseconds
```

### `src/main/resources/admin-users.properties`
```properties
admin.emails=elkorf@educourse.com
admin.default-password=Admin@123
```

---

## Production Deployment (Railway)

1. Add PostgreSQL driver to `pom.xml`
2. Update `application.yaml` to use environment variables:
   ```yaml
   datasource:
     url: ${DATABASE_URL}
   jwt:
     secret: ${JWT_SECRET}
   jpa:
     hibernate:
       ddl-auto: update
   ```
3. Push to GitHub
4. Deploy on [Railway.app](https://railway.app) — connect GitHub repo
5. Add PostgreSQL database add-on in Railway
6. Set environment variables: `JWT_SECRET`, etc.

---

## Developer

**Nikhil Korkatti** — EduCourse Full-Stack Project
