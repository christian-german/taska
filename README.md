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
- **Backend API**: http://localhost:8080

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

### Projects  `GET/POST /projects`
### Sections  `GET/POST /sections?project_id=`
### Tasks     `GET/POST /tasks?project_id=&filter=today|overdue|upcoming`
### Labels    `GET/POST /labels`
### Comments  `GET/POST /comments?task_id=`

Task lifecycle: `POST /tasks/{id}/close` · `POST /tasks/{id}/reopen`

## MCP Server

The Spring backend exposes a stateless Streamable HTTP [Model Context Protocol](https://modelcontextprotocol.io/) endpoint at `POST /mcp`.

Every MCP request, including initialization and tool discovery, requires the same OAuth2 bearer JWT used by the REST API. Taska is currently a mono-user application: a valid token grants access to its one shared workspace and does not create a separate user or data partition.

The first release exposes these tools:

- Projects: `list_projects`, `get_project`, `create_project`, `update_project`
- Tasks: `list_tasks`, `get_task`, `create_task`, `update_task`, `complete_task`, `reopen_task`

Task and project tools call the backend application services directly, so they follow the same validation, inbox defaults, and recurring-task behavior as the REST API. Destructive delete and reorder actions are intentionally not exposed.

## Features

- **Views**: Inbox · Today · Upcoming · Project (List + Kanban Board)
- **Tasks**: optional priority (p1–p4), scheduled times (`scheduledAt`), labels, sub-tasks, comments
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

## Authentik

```shell
docker exec -it taska-backend-authentik-server-1 ak changepassword akadmin
```
