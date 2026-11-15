# Task Manager

A Spring Boot task management application developed as part of a series of laboratory assignments. The project covers CRUD operations, Spring Security, database migrations, and unit testing.

## Laboratory Assignments

| Lab | Topic                               | Version                                                                       |
| --- | ----------------------------------- | ----------------------------------------------------------------------------- |
| 1   | Spring Security, CRUD, OIDC and JWT | [lab-01](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-01) |
| 2   | Unit Testing, Mockito and JaCoCo    | [lab-02](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-02) |
| 3 | Custom Annotations and Spring MVC Extensions | [lab-03](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-03) |
| 4 | Aspect-Oriented Programming (AOP) | [lab-04](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-04) |
| 5 | Asynchronous Processing | [lab-05](https://github.com/AlexxanderShnaider/task-manager-labs/tree/lab-05) |

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

---

## Lab 3 — Custom Annotations

The third laboratory assignment demonstrates three different mechanisms for creating and processing custom annotations in a Spring Boot application.

### Implemented Annotations

| Annotation | Mechanism | Purpose |
|------------|-----------|---------|
| `@ValidTaskDetails` | Bean Validation Constraint | Validates the relationship between task title and description |
| `@CreateTaskEndpoint` | Composed Annotation | Combines POST request mapping and HTTP 201 Created status |
| `@CurrentUsername` | MVC Argument Resolver | Automatically injects the current user's principal name into a controller parameter |

### 1. Bean Validation — `@ValidTaskDetails`

A class-level constraint was added to `TaskDto`.

The corresponding `TaskDetailsValidator` checks that a task description does not simply repeat its title, ignoring leading/trailing whitespace and letter case.

The validation mechanism is integrated into POST and PUT requests using `@Valid`.

**Expected behavior:**

- Valid task details are accepted.
- An empty description is allowed.
- Matching title and description produce a validation error.
- Invalid requests return HTTP 400 Bad Request.

### 2. Composed Annotation — `@CreateTaskEndpoint`

The custom annotation combines two Spring MVC annotations:

- `@RequestMapping(method = RequestMethod.POST)`
- `@ResponseStatus(HttpStatus.CREATED)`

It is applied to `TaskRestController.createTask()`.

This demonstrates how a single custom annotation can provide the same routing and response status behavior as the original annotations.

A MockMvc test verifies that POST `/api/tasks` is correctly mapped and returns HTTP 201 Created.

### 3. MVC Argument Resolver — `@CurrentUsername`

A custom argument resolution mechanism was implemented using `HandlerMethodArgumentResolver`.

The implementation consists of:

- `@CurrentUsername` — identifies controller parameters requiring automatic resolution.
- `CurrentUsernameResolver` — checks supported parameters and extracts the current principal name.
- `WebConfig` — registers the resolver with Spring MVC.

The following endpoint demonstrates the mechanism:

```http
GET /api/tasks/whoami
```

Example response:

```json
{
  "username": "9be3e6f7-b87c-4f9c-9036-9d6289dad4e1"
}
```

For JWT authentication, the principal name may be the Keycloak user ID (the `sub` claim), rather than the human-readable username.

If no principal is available, the resolver returns `"anonymous"`.

### Testing

JUnit 5, Mockito, Jakarta Validation, and Spring MockMvc are used to verify the three annotation mechanisms.

| Test Class | Tests | Description |
|------------|------:|-------------|
| `TaskDetailsValidatorTest` | 2 | Valid and invalid DTO validation |
| `CurrentUsernameResolverTest` | 3 | Parameter recognition and principal resolution |
| `TaskAnnotationsMvcTest` | 3 | HTTP routing, response status, validation, and argument injection |
| **New tests** | **8** | |
| **Total tests (Labs 1–3)** | **30** | |

The tests cover all eight acceptance criteria:

- AC1–AC3: Bean Validation constraint behavior.
- AC4–AC5: Composed annotation routing and response status.
- AC6–AC7: Argument resolver parameter support and value extraction.
- AC8: Resolved argument usage in a controller response.

Run the complete test suite:

```bash
mvn clean verify
```

The tests do not require a running PostgreSQL database or Keycloak server.

### Screenshots

| Evidence | Screenshot |
|----------|------------|
| Successful Maven test execution | [1.png](screenshots/lab-03/1.png) |
| Invalid task details (400 Bad Request) | [2.png](screenshots/lab-03/2.png) |
| Task creation (201 Created) | [3.png](screenshots/lab-03/3.png) |
| Current user resolution (200 OK) | [4.png](screenshots/lab-03/4.png) |


---

## Lab 4 — Aspect-Oriented Programming (AOP)

The fourth laboratory assignment introduces Spring AOP and demonstrates how custom annotations and aspects can implement cross-cutting functionality without modifying the main business logic.

### Implemented Aspects

Three custom annotations and their corresponding aspects were implemented.

| Annotation | Aspect | Functionality | Advice |
|------------|--------|---------------|--------|
| `@TaskAudit` | `TaskAuditAspect` | Records successful task operations | `@Around` |
| `@MeasureTaskDuration` | `TaskTimingAspect` | Measures execution time and counts method invocations | `@Around` |
| `@RequirePositiveTaskId` | `TaskIdAspect` | Rejects invalid task IDs before method execution | `@Before` |

All three aspects use annotation-based pointcuts and are applied to existing Task Manager service methods.

### 1. Task Auditing

The `@TaskAudit` annotation identifies operations that should be recorded in the audit history.

It is applied to task creation, updating, and deletion.

`TaskAuditAspect` intercepts the method call, executes the original operation, obtains the task ID, and records the operation through `TaskAuditTrail`.

The annotation value determines the action type: `CREATED`, `UPDATED`, or `DELETED`.

Audit records contain the action, task ID, and timestamp. They are stored in memory and written to the application log.

Audit history can be retrieved by an administrator:

```http
GET /api/tasks/aop/audit
```

### 2. Execution Time Measurement

The `@MeasureTaskDuration` annotation enables execution time measurement for selected service methods.

`TaskTimingAspect` uses `@Around` advice to:

- Record the start time using `System.nanoTime()`.
- Execute the intercepted method through `ProceedingJoinPoint.proceed()`.
- Calculate the execution duration.
- Save the duration and invocation count in `TaskTimingMetrics`.

The `finally` block ensures that execution time is recorded even if the intercepted method throws an exception.

### 3. Task ID Validation

The `@RequirePositiveTaskId` annotation is applied to service methods that receive a task ID.

`TaskIdAspect` uses `@Before` advice to validate the first method argument.

If the ID is zero, negative, missing, or not a `Long`, an `IllegalArgumentException` is thrown before the original method executes.

`TaskInputExceptionHandler` converts this exception into HTTP 400 Bad Request.

Example:

```http
GET /api/tasks/0
```

### 4. Self-Invocation Demonstration

A separate `TaskAopDemoService` demonstrates the self-invocation limitation of Spring AOP.

The same annotated method, `measuredStep()`, is called in two ways:

**External invocation:**

```http
GET /api/tasks/aop/external
```

The call passes through the Spring AOP proxy, so the timing aspect executes.

Expected result:

```json
{
  "call": "external",
  "result": "Task operation executed",
  "aspectTriggered": true,
  "countBefore": 0,
  "countAfter": 1
}
```

**Internal invocation:**

```http
GET /api/tasks/aop/internal
```

The service calls `this.measuredStep()` from another method of the same class.

This invocation bypasses the AOP proxy, so the timing aspect does not execute.

Example result after the external request:

```json
{
  "call": "internal",
  "result": "Task operation executed",
  "aspectTriggered": false,
  "countBefore": 1,
  "countAfter": 1
}
```

The counter values depend on previous requests. The important distinction is whether the aspect was triggered.

This behavior is intentional and demonstrates the proxy-based nature of Spring AOP.

### Testing

JUnit 5, Mockito, and Spring Test are used to verify aspect behavior.

| Test Class | Tests | Description |
|------------|------:|-------------|
| `TaskAuditAspectTest` | 2 | Successful auditing and handling failed operations |
| `TaskTimingAspectTest` | 2 | Execution measurement and exception handling |
| `TaskIdAspectTest` | 2 | Acceptance and rejection of task IDs |
| `AopSelfInvocationTest` | 2 | External invocation and self-invocation behavior |
| **New tests** | **8** | |
| **Total tests (Labs 1–4)** | **38** | |

The advice logic is tested directly using mocked `ProceedingJoinPoint` or `JoinPoint` objects without starting the full Spring application.

A separate test uses a minimal Spring AOP context to demonstrate the difference between external method calls and self-invocation.

Run all tests:

```bash
mvn clean verify
```

### Screenshots

| Evidence | Screenshot |
|----------|------------|
| Successful Maven test execution | [1.png](screenshots/lab-04/1.png) |
| External invocation triggers the aspect | [2.png](screenshots/lab-04/2.png) |
| Self-invocation bypasses the aspect | [3.png](screenshots/lab-04/3.png) |
| Invalid task ID returns HTTP 400 | [4.png](screenshots/lab-04/4.png) |
| Successful task creation | [5.png](screenshots/lab-04/5.png) |
| Audit history containing the created task | [6.png](screenshots/lab-04/6.png) |


---

## Lab 5 — Asynchronous Processing

The fifth laboratory assignment introduces asynchronous task execution in Spring Boot using `@Async`, `CompletableFuture`, and explicitly configured thread pools.

### Implemented Functionality

Three simulated slow operations were implemented in `TaskAsyncWorker`:

| Operation | Method | Description |
|-----------|--------|-------------|
| Notification | `sendNotification()` | Fire-and-forget notification |
| Report generation | `generateReport()` | Asynchronous report generation |
| External status | `checkExternalStatus()` | Simulated external API request |

Each operation simulates a delay to demonstrate asynchronous execution.

### 1. Custom Thread Pool

`AsyncConfig` enables asynchronous execution using `@EnableAsync`.

A custom `ThreadPoolTaskExecutor` is configured with:

- Core pool size: 2
- Maximum pool size: 4
- Queue capacity: 2
- Thread name prefix: `task-async-`

All `@Async` worker methods explicitly use this executor.

### 2. Fire-and-Forget

Notifications are executed asynchronously without waiting for completion.

Endpoint:

`POST /api/tasks/async/notify/{id}`

The request returns HTTP 202 Accepted while notification processing continues in a separate thread.

### 3. Parallel Execution

Two independent asynchronous operations are started:

- Report generation
- External status checking

Both return `CompletableFuture<String>`.

Their results are combined using `thenCombine()`.

Endpoint:

`GET /api/tasks/async/summary/{id}`

The operations can execute concurrently, reducing total processing time compared to sequential execution.

### 4. Error Handling

Asynchronous failures are handled using `exceptionally()`.

If an external operation fails, the application logs the error and returns a fallback result instead of failing the entire request.

Example:

`GET /api/tasks/async/summary/15?fail=true`

For fire-and-forget methods returning `void`, errors are logged using `AsyncUncaughtExceptionHandler`.

Example:

`POST /api/tasks/async/notify/15?fail=true`

### 5. Timeout Handling

`CompletableFuture.orTimeout()` limits waiting time to 2 seconds.

A simulated slow external operation takes 4 seconds.

Endpoint:

`GET /api/tasks/async/summary/15?slow=true`

After the timeout, the application returns a fallback value.

The timeout limits waiting for the result but does not necessarily cancel the underlying running task.

### 6. Executor Overload

A separate executor demonstrates controlled resource exhaustion.

Configuration:

- Core pool size: 1
- Maximum pool size: 1
- Queue capacity: 1

Submitting three slow tasks causes the third task to be rejected.

Endpoint:

`POST /api/tasks/async/overload`

`TaskRejectedException` is handled by `AsyncTaskExceptionHandler`, returning HTTP 503 Service Unavailable with a clear error message.

### Testing

JUnit 5, Mockito, and Spring Test are used to verify asynchronous behavior.

| Test Class | Tests | Description |
|------------|------:|-------------|
| `TaskAsyncServiceTest` | 4 | Dispatch, result combination, error fallback, timeout |
| `AsyncExecutionTest` | 2 | Concurrent execution, thread prefix, non-blocking invocation |
| `TaskAsyncOverloadTest` | 2 | Pool saturation and HTTP 503 handling |
| **New tests** | **8** | |

The tests verify actual concurrent execution using `CountDownLatch`, without relying only on execution time measurements.

Run all tests:

`mvn clean verify`

### Screenshots

| Evidence | Screenshot |
|----------|------------|
| Successful Maven test execution | [1.png](screenshots/lab-05/1.png) |
| Fire-and-forget notification | [2.png](screenshots/lab-05/2.png) |
| Combined asynchronous results | [3.png](screenshots/lab-05/3.png) |
| External service failure fallback | [4.png](screenshots/lab-05/4.png) |
| Timeout handling | [5.png](screenshots/lab-05/5.png) |
| Async void exception handling | [6.png](screenshots/lab-05/6.png) |
| Executor overload and HTTP 503 | [7.png](screenshots/lab-05/7.png) |



## Notes

This application is intended for educational use and local development.

- Keycloak configuration must be completed manually.
- CSRF protection is disabled for the simplified laboratory setup.
- Browser-based sessions require appropriate CSRF protection in production.
- Development credentials should not be used in a production environment.
- Maven build outputs and generated reports are excluded from Git and can be regenerated locally.



