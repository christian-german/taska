# Taska — Todoist Clone

A full-stack Todoist clone built with Angular 21 + Spring Boot 3.5 + PostgreSQL.

## Stack

| Layer      | Technology                                     |
|------------|------------------------------------------------|
| Frontend   | Angular 21, TailwindCSS v4, Angular CDK        |
| Backend    | Spring Boot 3.5, Spring Data JPA, Flyway       |
| Database   | PostgreSQL 17                                  |
| Container  | Docker Compose                                 |

## Quick Start

```bash
docker compose up -d --build
```

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1

## Development

### Backend (Java 25 / Spring Boot 3.5)

```bash
# Start only postgres
docker compose up -d postgres

# Run backend locally
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend (Angular 21)

```bash
cd frontend
npm install
npm start          # dev server on :4200 with API proxy
```

## REST API

### Projects  `GET/POST /api/v1/projects`
### Sections  `GET/POST /api/v1/sections?project_id=`
### Tasks     `GET/POST /api/v1/tasks?project_id=&filter=today|overdue|upcoming`
### Labels    `GET/POST /api/v1/labels`
### Comments  `GET/POST /api/v1/comments?task_id=`

Task lifecycle: `POST /api/v1/tasks/{id}/close` · `POST /api/v1/tasks/{id}/reopen`

## Features

- **Views**: Inbox · Today · Upcoming · Project (List + Kanban Board)
- **Tasks**: priority (p1–p4), due dates, labels, sub-tasks, comments
- **Projects**: colors, favorites, nested projects, sections
- **Quick Add**: press `Q` — supports `#project`, `@label`, `p1–p4`, `today/tomorrow`
- **Drag & Drop**: reorder tasks within sections (Angular CDK)
- **Dark mode**: auto-detects system preference, toggleable in sidebar
- **Optimistic UI**: completions animate immediately before server confirmation

## Data Model

```
Project → Sections → Tasks → Sub-tasks
                           → Comments
Labels ←→ Tasks (string references)
```

Inbox project is auto-created on startup (`isInboxProject=true`). Tasks without a
`projectId` are automatically routed there.
