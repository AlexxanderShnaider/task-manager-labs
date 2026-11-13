# Task Manager

A Spring Boot application for managing tasks. This is the first laboratory assignment, covering CRUD operations, database migrations, and authentication and authorization with Spring Security and Keycloak.

## Technologies

- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Hibernate
- PostgreSQL, Liquibase
- Spring Security, OAuth2 / OpenID Connect, JWT
- MapStruct, Lombok
- Thymeleaf, HTML, CSS, JavaScript
- Docker Compose

## Features

- Create, view, update, and delete tasks through a REST API and a simple web interface.
- Store tasks in PostgreSQL and manage the database schema with Liquibase.
- Convert between entities and DTOs using MapStruct.
- Sign in through Keycloak using OpenID Connect.
- Authenticate API requests with JWT bearer tokens.
- Restrict task modifications based on user roles.

## Running locally

**Requirements:** JDK 17, Maven, and Docker.

### 1. Start the services

Run the following command from the project directory:

```bash
docker compose up -d
```

The provided configuration uses PostgreSQL on `localhost:5432` (database `app_db`) and Keycloak on `localhost:8081`.

### 2. Configure Keycloak

Keycloak must be configured manually before the first application launch. Open the [Keycloak Admin Console](http://localhost:8081/admin) and set up:

- **Realm:** `demo-realm`
- **Client:** `spring-client` (OpenID Connect, client authentication enabled, standard authorization code flow enabled)
- **Valid redirect URI:** `http://localhost:8080/login/oauth2/code/keycloak`
- **Realm roles:** `ROLE_ADMIN` and `ROLE_USER`
- **Test users:** an administrator and a regular user, with their respective roles assigned

Ensure that the realm roles are included in the token claims so that Spring Security can map them to authorities. The client secret is available under **Clients → spring-client → Credentials**.

### 3. Start Spring Boot

Set the Keycloak client secret in the terminal:

```bash
export KEYCLOAK_CLIENT_SECRET="your-client-secret"
mvn spring-boot:run
```

The PostgreSQL connection settings in `src/main/resources/application.yml` must match those in `docker-compose.yml`.

Open the application at [http://localhost:8080](http://localhost:8080).

## API and access control

| Method | Endpoint | Access |
| --- | --- | --- |
| `GET` | `/api/tasks` | Public |
| `GET` | `/api/tasks/{id}` | Public |
| `POST` | `/api/tasks` | `ROLE_ADMIN` |
| `PUT` | `/api/tasks/{id}` | `ROLE_ADMIN` |
| `DELETE` | `/api/tasks/{id}` | `ROLE_ADMIN` |

`POST` and `DELETE` are restricted by `SecurityFilterChain`. The update operation is protected by `@PreAuthorize` in `TaskService`, in addition to requiring authentication at the HTTP level.

The browser UI uses OIDC login with a session. API clients can send an access token using the `Authorization: Bearer <access-token>` header.

## Testing the API

Prepared HTTP requests are available in [`requests/lab-01.http`](requests/lab-01.http). They include CRUD operations and negative authorization cases. Use access tokens issued by your local Keycloak instance; do not commit tokens or client secrets to the repository.

The main authorization checks are:

- An unauthenticated request to create a task is rejected with `401 Unauthorized`.
- A user without the administrator role receives `403 Forbidden` when attempting to create a task.
- A user without the administrator role receives `403 Forbidden` when attempting to update a task, demonstrating method-level security.
- An administrator can create a task using a valid bearer token (`201 Created`).

## Screenshots

Selected screenshots from the first laboratory assignment are stored in [`screenshots/`](screenshots/).

| Evidence | Screenshot |
| --- | --- |
| Task Manager interface | [1.png](screenshots/1.png) |
| Task records in PostgreSQL | [2.png](screenshots/2.png) |
| Liquibase migration history | [3.png](screenshots/3.png) |
| Keycloak sign-in page | [4.png](screenshots/4.png) |
| Unauthorized task creation (`401`) | [5.png](screenshots/5.png) |
| Forbidden task creation (`403`) | [6.png](screenshots/6.png) |
| Keycloak realm roles | [8.png](screenshots/8.png) |
| Authorized JWT request (`201 Created`) | [10.png](screenshots/10.png) |

## Notes

This project is intended for local development and educational use. CSRF protection is disabled in this version; it should be enabled or appropriately configured for a production application using browser sessions.

Keycloak realm configuration is not automatically imported. A fresh Keycloak instance requires the manual setup described above.
