# Todo List API

A RESTful service for managing todo items. Supports creating, updating, and retrieving todos with automatic detection of
past-due items.

## Assumptions

- Data is stored in an **H2 in-memory database** — all data is lost on restart.
- **No authentication** is implemented — all endpoints are publicly accessible.
- A background scheduler automatically marks overdue items as `PAST_DUE` every 60 seconds.
- Setting status to `PAST_DUE` manually is forbidden — it is managed by the system only.
- Enum values (e.g. status) are accepted case-insensitively (`done`, `Done`, `DONE` all work).

## Tech Stack

- **Runtime:** Java 21, Spring Boot 4.0.3
- **Database:** H2 (in-memory)
- **Frameworks:** Spring Web MVC, Spring Data JPA
- **Key Libraries:** Lombok, ModelMapper, Jackson (with JSR-310 for `LocalDateTime`)
- **Testing:** JUnit 5, Mockito, Spring MockMvc
- **Containerization:** Docker (multi-stage build)

## How To

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for containerized run)

### Build the Service

```bash
mvn clean package
```

This compiles the code, runs tests, and produces a JAR in `target/`.

To build without running tests:

```bash
mvn clean package -DskipTests
```

### Run Automatic Tests

```bash
mvn test
```

Tests cover three layers:

- **Repository tests** — integration tests with H2 database (`@SpringBootTest`)
- **Service tests** — unit tests with mocked repository (`@ExtendWith(MockitoExtension.class)`)
- **API tests** — controller tests with MockMvc (`@WebMvcTest`)

### Run the Service Locally

```bash
mvn spring-boot:run
```

Or run the built JAR directly:

```bash
java -jar target/todo-list-0.0.1-SNAPSHOT.jar
```

The application starts at `http://localhost:8080`.

### Run with Docker

Build the Docker image:

```bash
docker build -t todo-list .
```

Run the container:

```bash
docker run -p 8080:8080 todo-list
```

The application will be available at `http://localhost:8080`.

## API Endpoints

Base URL: `/api/todo`

### Create a Todo

```
POST /api/todo
```

**Request Body:**

```json
{
  "description": "Buy groceries",
  "dueDateTime": "2026-03-10T10:00:00"
}
```

**Response:** `201 Created`

---

### Update Description

```
PUT /api/todo/description
```

**Request Body:**

```json
{
  "id": 1,
  "description": "Updated description"
}
```

**Response:** `202 Accepted`

---

### Update Status

```
PUT /api/todo/status
```

**Request Body:**

```json
{
  "id": 1,
  "status": "DONE"
}
```

**Response:** `202 Accepted`

> **Note:** Setting status to `PAST_DUE` is forbidden and returns `403 Forbidden`.

---

### Get Todo by ID

```
GET /api/todo/{id}
```

**Response:** `200 OK`

```json
{
  "id": 1,
  "description": "Buy groceries",
  "status": "NOT_DONE",
  "creationDateTime": "2026-03-01T10:00:00",
  "dueDateTime": "2026-03-10T10:00:00",
  "completionDateTime": null
}
```

---

### Get Todo List

```
GET /api/todo/list?onlyNotDone={true|false}
```

| Parameter     | Type    | Description                              |
|---------------|---------|------------------------------------------|
| `onlyNotDone` | boolean | If `true`, returns only `NOT_DONE` items |

**Response:** `200 OK` — array of todo items

## Todo Item Statuses

| Status     | Description                                     |
|------------|-------------------------------------------------|
| `NOT_DONE` | Default status when a todo is created           |
| `DONE`     | Set manually; records `completionDateTime`      |
| `PAST_DUE` | Set automatically when `dueDateTime` has passed |

## Error Handling

| HTTP Status | Condition                              |
|-------------|----------------------------------------|
| `400`       | Missing or invalid request fields      |
| `403`       | Attempting to set status to `PAST_DUE` |
| `404`       | Todo item not found by ID              |
