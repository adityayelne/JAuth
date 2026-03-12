# JAuth — Project Overview

## Purpose
JAuth is a small Spring Boot authentication application demonstrating JWT-based stateless authentication with Spring Security, JPA (Hibernate), and MySQL. It includes simple Thymeleaf templates for login, registration, and a protected dashboard.

---

## Quick start
1. Ensure MySQL is running and accessible.
2. (Optional) Create the `jauth` database manually or let Hibernate create it if the DB user has privileges.
   - Manual (MySQL Workbench):
     - CREATE DATABASE jauth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     - GRANT ALL ON jauth.* TO 'root'@'localhost' IDENTIFIED BY 'password';
3. Build and run the app:

```powershell
cd F:\prj_UserAuth\JAuth
mvn clean install
mvn spring-boot:run
```

4. Open the frontend:
- Login: http://localhost:3002/
- Register: http://localhost:3002/register
- Dashboard: http://localhost:3002/dashboard (requires login)

---

## Key configuration (`src/main/resources/application.properties`)
- `spring.datasource.url` — JDBC URL for MySQL (example: `jdbc:mysql://localhost:3306/jauth?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true`)
- `spring.datasource.username`, `spring.datasource.password` — DB credentials
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate will create/alter tables to match entities (development). For production use migrations (Flyway/Liquibase).
- `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect`
- `jwt.secret` — HMAC secret for signing tokens (move to env var in production)
- `jwt.expiration` — token lifetime in milliseconds

---

## Dependencies (from `pom.xml`)
- `spring-boot-starter-web`: Spring MVC and embedded server
- `spring-boot-starter-data-jpa`: Spring Data JPA + Hibernate
- `mysql-connector-j`: MySQL JDBC driver (runtime)
- `spring-boot-starter-security`: Spring Security
- `io.jsonwebtoken` (jjwt-api, jjwt-impl, jjwt-jackson): JWT creation/parsing
- `lombok`: reduces boilerplate (optional)
- `spring-boot-devtools`: developer convenience (optional)
- `spring-boot-starter-test`: testing

---

## File responsibilities and workflow

- `src/main/java/com/authsystem/jauth/JAuthApplication.java`
  - Application entry point. Starts Spring and component scanning rooted at this package.

- `src/main/java/com/authsystem/jauth/controllers/PageController.java`
  - Serves HTML pages and exposes REST endpoints:
    - `GET /` -> `login.html`
    - `GET /register` -> `register.html`
    - `GET /dashboard` -> `dashboard.html` (protected)
    - `POST /api/auth/register` -> calls `AuthService.register`
    - `POST /api/auth/login` -> calls `AuthService.login`

- `src/main/java/com/authsystem/jauth/controllers/GlobalErrorController.java`
  - Handles `/error` and returns structured JSON error responses to avoid unmapped /error issues.

- `src/main/java/com/authsystem/jauth/service/AuthService.java`
  - Core auth logic:
    - `register(RegisterRequest)` — checks email uniqueness, hashes password, saves `User`, returns JWT
    - `login(LoginRequest)` — finds user, verifies password, returns JWT
    - Uses `PasswordEncoder` (BCrypt) and `JwtProvider`

- `src/main/java/com/authsystem/jauth/repository/UserRepository.java`
  - Extends `JpaRepository<User, Long>` with `findByEmail` and `existsByEmail`.

- `src/main/java/com/authsystem/jauth/entity/User.java`
  - JPA entity mapped to `users` table. Fields: `id` (IDENTITY), `email`, `password`, `fullName`, `enabled`.

- `src/main/java/com/authsystem/jauth/config/SecurityConfig.java`
  - Registers security rules, CORS, `PasswordEncoder` bean, and the `JwtAuthenticationFilter` in the filter chain.
  - Public endpoints: `/api/auth/**`, static assets, `/`, `/login`, `/register`.
  - Protected endpoints: `/api/protected/**`, `/dashboard`.

- `src/main/java/com/authsystem/jauth/security/JwtProvider.java`
  - Creates signed JWTs (`generateToken(email)`), validates and parses tokens to extract `email`.

- `src/main/java/com/authsystem/jauth/security/JwtAuthenticationFilter.java`
  - A `OncePerRequestFilter` that extracts `Authorization: Bearer <token>` header, validates the token, and if valid sets a `UsernamePasswordAuthenticationToken` (email as principal) in `SecurityContextHolder` for downstream controllers.

- `src/main/resources/templates/*.html`
  - Thymeleaf templates for `login`, `register`, `dashboard`, `firstpage` — the frontend UI. Client-side JS should store the returned JWT and send it in `Authorization` header for protected requests.

---

## Database behavior and MySQL Workbench steps
- With `spring.jpa.hibernate.ddl-auto=update`, Hibernate will create or update tables automatically when the app starts (if the DB exists or `createDatabaseIfNotExist=true` is in the URL and the DB user has CREATE privileges).
- Recommended Workbench steps (manual database creation):
  1. Connect to your MySQL server in MySQL Workbench.
  2. Run:

```sql
CREATE DATABASE jauth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- replace 'root' and 'password' with your chosen user/credentials
GRANT ALL ON jauth.* TO 'root'@'localhost' IDENTIFIED BY 'password';
```

- After DB exists, start the app and Hibernate will create the `users` table according to the `User` entity.

---

## JWT flow (summary)
1. User registers or logs in; backend validates credentials.
2. Backend issues a JWT signed with `jwt.secret` containing `sub` (email), `iat`, `exp`.
3. Client stores token (localStorage or cookie) and sends it in `Authorization: Bearer <token>` for protected requests.
4. `JwtAuthenticationFilter` validates token and sets authentication in Spring Security context.
5. Protected endpoints use the Security context to identify the user.

Security recommendations:
- Store `jwt.secret` and DB credentials in environment variables, not in commit.
- Use HTTPS in production.
- Prefer httpOnly secure cookies for tokens to reduce XSS exposure.
- Add refresh tokens for better UX.

---

## Common troubleshooting
- "No mapping for /error": resolved by `GlobalErrorController`.
- Bean not found for `PasswordEncoder`: ensure `SecurityConfig` declares the bean and is in the scanned package.
- Package mismatch errors: ensure package declarations match directory structure (`src/main/java/com/authsystem/jauth/...`) or use the `main.java.com.authsystem.Jauth` variant if your IDE/tool expects that layout.
- MySQL connection issues: check URL, credentials, and that MySQL is running.

---

## Files modified during setup/fixes
- `README_PROJECT_OVERVIEW.md` (this file)
- `src/main/resources/application.properties` (MySQL + error settings)
- `src/main/java/com/authsystem/jauth/controllers/GlobalErrorController.java`
- Several package declaration adjustments across source files to ensure consistent scanning

---

## Next recommended improvements
- Move secrets to environment variables or a secrets manager.
- Replace `spring.jpa.hibernate.ddl-auto=update` with a migration tool (Flyway/Liquibase).
- Harden security: HTTPS, HttpOnly cookies, token rotation.
- Add integration tests for auth flows.

---

If you want, I can also commit these changes to a Git branch and open a PR, or create a downloadable markdown file archive. Which would you prefer?