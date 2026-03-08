# Todo List API

A RESTful Todo List application built with Spring Boot 4, JPA, and H2 in-memory database.

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.3**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Lombok**
- **ModelMapper**
- **Jackson** (with JSR-310 support for `LocalDateTime`)
- **JUnit 5 + Mockito** (testing)

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run the Application

```bash
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

### Run Tests

```bash
mvn test
```

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

**Response:** `202 Accepted` — `"Description updated successfully"`

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

**Response:** `202 Accepted` — `"Status updated successfully"`

> **Note:** Setting status to `PAST_DUE` manually is not allowed and returns `400 Bad Request`.

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

**Response:** `200 OK` — array of `TodoDto`

## Todo Item Statuses

| Status     | Description                                     |
|------------|-------------------------------------------------|
| `NOT_DONE` | Default status when a todo is created           |
| `DONE`     | Set manually; records `completionDateTime`      |
| `PAST_DUE` | Set automatically when `dueDateTime` has passed |

## Past Due Scheduler

A background scheduler runs every **60 seconds** and automatically marks all `NOT_DONE` items with an expired
`dueDateTime` as `PAST_DUE`.

## Error Handling

| HTTP Status | Condition                              |
|-------------|----------------------------------------|
| `400`       | Missing or invalid request fields      |
| `400`       | Attempting to set status to `PAST_DUE` |
| `404`       | Todo item not found by ID              |

