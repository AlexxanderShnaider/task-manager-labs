# Task Manager

A Spring Boot task management application developed as part of a series of laboratory assignments. The project covers CRUD operations, Spring Security, database migrations, and unit testing.

## Laboratory Assignments

| Lab | Topic                               | Version                                                                       |
| --- | ----------------------------------- | ----------------------------------------------------------------------------- |
| 1   | Spring Security, CRUD, OIDC and JWT | [lab-01](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-01) |
| 2   | Unit Testing, Mockito and JaCoCo    | [lab-02](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-02) |

Each Git tag represents the project state after completing the corresponding laboratory assignment.

## Technologies

- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Hibernate
- Spring Security, OAuth2, OpenID Connect, JWT
- PostgreSQL, Liquibase
- Keycloak
- Lombok
- JUnit 5, Mockito, JaCoCo
- Thymeleaf, HTML, CSS, JavaScript
- Maven, Docker Compose

MapStruct was used for entity-to-DTO mapping in Lab 1. The current version uses a manual `TaskMapper` implementation.

## Features

- Create, retrieve, update, and delete tasks.
- Store tasks in PostgreSQL.
- Manage database schema changes with Liquibase.
- Convert between entities and DTOs.
- Authenticate users through Keycloak using OpenID Connect.
- Support JWT bearer token authentication for REST API requests.
- Restrict operations based on user roles.
- Test application components using JUnit 5 and Mockito.

## Running the Application

### Requirements

- JDK 17
- Maven
- Docker

### 1. Start PostgreSQL and Keycloak

From the project root, run:

```bash
docker compose up -d
```

The provided configuration uses:

| Service     | Address                 |
| ----------- | ----------------------- |
| PostgreSQL  | `localhost:5432`        |
| Database    | `app_db`                |
| Keycloak    | `http://localhost:8081` |
| Spring Boot | `http://localhost:8080` |

### 2. Configure Keycloak

Keycloak requires manual configuration before the first application launch.

Open the [Keycloak Admin Console](http://localhost:8081/admin) and create the following configuration:

| Setting               | Value                                              |
| --------------------- | -------------------------------------------------- |
| Realm                 | `demo-realm`                                       |
| Client ID             | `spring-client`                                    |
| Client type           | OpenID Connect                                     |
| Client authentication | Enabled                                            |
| Standard flow         | Enabled                                            |
| Valid redirect URI    | `http://localhost:8080/login/oauth2/code/keycloak` |
| Realm roles           | `ROLE_ADMIN`, `ROLE_USER`                          |

Create two test users:

- An administrator with the `ROLE_ADMIN` role.
- A regular user with the `ROLE_USER` role.

Configure the client so that the assigned roles are included in the token claims.

The client secret can be found under **Clients → spring-client → Credentials**.

For the prepared HTTP token requests, enable **Direct access grants** in the client settings.

### 3. Start Spring Boot

Set the Keycloak client secret:

```bash
export KEYCLOAK_CLIENT_SECRET="your-client-secret"
```

Make sure that the PostgreSQL credentials in `application.yml` match those configured in `docker-compose.yml`.

Start the application:

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

---

## Lab 1 — Spring Security

The first laboratory assignment establishes the basic application architecture and implements authentication and authorization.

### Application Structure

The application follows a layered Spring Boot architecture:

- **Entity** — represents tasks stored in PostgreSQL.
- **Repository** — provides database operations through Spring Data JPA.
- **Service** — implements task operations and business logic.
- **DTO** — transfers task data between application layers.
- **Mapper** — converts entities to DTOs and vice versa.
- **REST Controller** — exposes CRUD endpoints.
- **UI Controller** — serves the main application page.
- **SecurityConfig** — configures authentication and access rules.

Liquibase manages the database schema using versioned migrations.

### REST API

| Method | Endpoint          | Access | Success Status |
| ------ | ----------------- | ------ | -------------- |
| GET    | `/api/tasks`      | Public | 200            |
| GET    | `/api/tasks/{id}` | Public | 200            |
| POST   | `/api/tasks`      | ADMIN  | 201            |
| PUT    | `/api/tasks/{id}` | ADMIN  | 200            |
| DELETE | `/api/tasks/{id}` | ADMIN  | 204            |

### Authentication and Authorization

Two authentication mechanisms are supported.

**OIDC authentication**

Users can sign in through the web interface. Spring Security redirects them to Keycloak and establishes an authenticated session after successful login.

**JWT authentication**

REST API clients can authenticate using an access token issued by Keycloak:

```http
Authorization: Bearer <access-token>
```

Access restrictions are implemented at two levels:

- `SecurityFilterChain` restricts POST and DELETE requests to administrators.
- `@PreAuthorize("hasRole('ADMIN')")` protects the update operation in `TaskService`.

The custom OIDC user service and JWT authentication converter extract Keycloak roles and map them to Spring Security authorities.

### API Testing

Prepared HTTP requests are available in [requests/lab-01.http](requests/lab-01.http).

The collection includes:

- Obtaining access tokens for administrator and regular users.
- Retrieving all tasks and individual tasks.
- Creating, updating, and deleting tasks.
- Checking unauthorized access (401).
- Checking insufficient permissions (403).
- Verifying administrator access through JWT.

Access tokens and client credentials must be supplied locally and should not be committed to the repository.

### Screenshots

| Evidence                              | Screenshot                          |
| ------------------------------------- | ----------------------------------- |
| Task Manager interface                | [1.png](screenshots/lab-01/1.png)   |
| Task records in PostgreSQL            | [2.png](screenshots/lab-01/2.png)   |
| Liquibase migration history           | [3.png](screenshots/lab-01/3.png)   |
| Keycloak login page                   | [4.png](screenshots/lab-01/4.png)   |
| Unauthorized request (401)            | [5.png](screenshots/lab-01/5.png)   |
| Forbidden request (403)               | [6.png](screenshots/lab-01/6.png)   |
| Keycloak realm roles                  | [8.png](screenshots/lab-01/8.png)   |
| Authorized JWT request (201 Created)  | [10.png](screenshots/lab-01/10.png) |
| Method-level authorization (PUT, 403) | [12.png](screenshots/lab-01/12.png) |

---

## Lab 2 — Unit Testing

The second laboratory assignment introduces unit testing for the existing Task Manager application.

The tests verify task operations, controller responses, entity-to-DTO mapping, and the conversion of Keycloak roles into Spring Security authorities.

### Testing Tools

- **JUnit 5** — test execution and assertions.
- **Mockito** — mocking dependencies and verifying interactions.
- **JaCoCo** — measuring code coverage.
- **Maven Surefire** — executing tests and collecting results.

### Test Cases

| Test Class               |  Tests | Description                                                                 |
| ------------------------ | -----: | --------------------------------------------------------------------------- |
| `TaskServiceTest`        |      8 | CRUD operations, exceptions, repository interactions and argument capturing |
| `TaskRestControllerTest` |      5 | Controller responses and HTTP status codes                                  |
| `TaskMapperTest`         |      3 | Entity/DTO mapping and null handling                                        |
| `SecurityConfigTest`     |      5 | JWT and OIDC authority mapping                                              |
| `UIControllerTest`       |      1 | Main page view resolution                                                   |
| **Total**                | **22** |                                                                             |

All tests are unit tests and do not require a running PostgreSQL database, Keycloak server, or Spring application context.

Mockito is used to isolate components and control their dependencies.

### Running the Tests

Run all tests and generate the coverage report:

```bash
mvn clean verify
```

The JaCoCo HTML report is generated at:

```text
target/site/jacoco/index.html
```

On macOS, open it using:

```bash
open target/site/jacoco/index.html
```

The generated `target/` directory is excluded from version control.

### Test Results

All 22 tests passed after restoring the correct application logic.

| Metric                           | Result |
| -------------------------------- | ------ |
| Total tests                      | 22     |
| Passed                           | 22     |
| Failures                         | 0      |
| Errors                           | 0      |
| Skipped                          | 0      |
| Instruction coverage             | 69%    |
| Branch coverage                  | 75%    |
| TaskService instruction coverage | 100%   |

The overall instruction coverage exceeds the required minimum of 60%.

### Defect Detection

To demonstrate the effectiveness of unit testing, a temporary defect was introduced into `TaskService.updateTask()`.

The original implementation:

```java
existingTask.setCompleted(taskDto.isCompleted());
```

was temporarily replaced with:

```java
existingTask.setCompleted(false);
```

The test `updateTask_existingTask_updatesFieldsAndSaves` failed because the expected completion state was `true`, while the actual value was `false`.

The original implementation was restored, and the complete test suite passed successfully.

The final source code does not contain the introduced defect.

### Screenshots

| Evidence                               | Screenshot                        |
| -------------------------------------- | --------------------------------- |
| Failed test after introducing a defect | [1.png](screenshots/lab-02/1.png) |
| All 22 tests passed                    | [2.png](screenshots/lab-02/2.png) |
| TaskService coverage (100%)            | [3.png](screenshots/lab-02/3.png) |
| Overall JaCoCo coverage (69%)          | [4.png](screenshots/lab-02/4.png) |

---

## Notes

This application is intended for educational use and local development.

- Keycloak configuration must be completed manually.
- CSRF protection is disabled for the simplified laboratory setup.
- Browser-based sessions require appropriate CSRF protection in production.
- Development credentials should not be used in a production environment.
- Maven build outputs and generated reports are excluded from Git and can be regenerated locally.
