## Context

Taska is a single-user Spring Boot application whose backend already exposes task and project operations through application services and a JWT-protected REST API. AI clients currently cannot invoke these operations through the Model Context Protocol (MCP).

The backend uses Spring MVC, Spring Security's OAuth2 resource-server support, and a shared task/project dataset. JWT validation is already configured, but the application intentionally does not use JWT claims to partition records because Taska is mono-user.

## Goals / Non-Goals

**Goals:**

- Host an MCP-compatible Streamable HTTP endpoint in the existing Spring Boot process.
- Use Spring AI's MCP server support and declarative tool definitions.
- Require the existing valid bearer JWT for MCP protocol and tool requests.
- Expose a deliberately small task/project operation set that delegates to existing services.
- Preserve existing Taska behavior, including task validation, inbox selection, recurrence handling, and project rules.

**Non-Goals:**

- Adding multi-user tenancy, record ownership, per-user inboxes, or authorization scopes beyond authenticated access.
- Exposing labels, sections, comments, filters, time entries, device registration, or administrative operations through MCP.
- Adding an LLM/chat model to Taska, or making Taska an MCP client.
- Replacing or versioning the REST API.

## Decisions

### Embed a Spring AI MCP server in `taska-backend`

Add Spring AI's WebMVC MCP server starter and configure it within the existing application instead of deploying a separate adapter service. This keeps deployment, database access, transaction handling, and security configuration in one process.

The alternative—a standalone MCP service that calls Taska REST endpoints—would duplicate authentication/client configuration and add a network hop while still needing to model the same DTOs and errors.

### Use stateless Streamable HTTP at a fixed MCP endpoint

Configure Spring AI's stateless Streamable HTTP protocol and expose the endpoint at `/mcp`. Stateless operation fits tool calls that do not require server-held client session state, permits horizontal deployment behind a load balancer, and ensures every request passes through the normal bearer-token filter.

The alternative stateful Streamable HTTP mode is unnecessary for this initial tool-only server. Legacy SSE transport is excluded.

### Protect the entire MCP endpoint with the existing JWT resource server

Keep `/mcp` inside the existing Spring Security filter chain and require authentication for all MCP requests, including initialization and tool discovery. An authenticated MCP client therefore presents the same bearer JWT already accepted by the REST API.

No per-record authorization is added: Taska is explicitly mono-user, so validated access grants access to the one shared workspace. Future multi-user support must introduce ownership scoping before exposing the service to more than one principal.

### Implement narrow MCP tool beans that call application services

Create MCP tool methods whose inputs are purpose-designed, stable MCP schemas and whose results are concise task/project representations. The methods call `TaskService` and `ProjectService` (and existing mappers where needed), never controllers or repositories directly. This preserves domain behavior and prevents the MCP transport from becoming a second business-logic path.

Initial tools cover:

- Projects: list, retrieve, create, and update.
- Tasks: list, retrieve, create, update, complete, and reopen.

Destructive delete and bulk-reorder operations are intentionally omitted from the first release. Recurring task mutations retain the existing explicit recurrence-scope inputs.

### Translate domain and validation failures into actionable tool errors

MCP tool failures MUST return safe, user-actionable error content for invalid input and missing resources, without exposing stack traces, JWT contents, or database details. Existing exception handling/domain exceptions remain the authoritative source for the underlying error conditions.

## Risks / Trade-offs

- [Mono-user data model offers no claim-level isolation] → Keep the endpoint limited to the intended Taska account/deployment; introduce ownership and authorization scopes before multi-user access.
- [MCP clients differ in Streamable HTTP and OAuth support] → Use the current MCP-standard Streamable HTTP transport, document the endpoint and bearer-token expectation, and add protocol-level integration tests.
- [Tool schemas can become a long-lived external contract] → Start with a small set, avoid exposing persistence entities, and use explicit input/output records.
- [AI-driven mutations can be surprising] → Omit delete/reorder tools initially and return created/updated representations so clients can confirm outcomes.
- [Spring AI MCP APIs evolve] → Pin a compatible Spring AI release through its BOM and cover startup plus tool discovery in tests.

## Migration Plan

1. Add and pin the Spring AI MCP server dependency/configuration, leaving REST behavior unchanged.
2. Add the secured `/mcp` endpoint and tool beans behind the existing JWT resource-server configuration.
3. Deploy with the endpoint enabled and validate it using an authenticated MCP client in a non-production environment.
4. Roll back by disabling/removing the MCP server configuration or deploying the previous backend version; no data migration is required.

## Open Questions

- The initial endpoint path is `/mcp`; a reverse-proxy base path can be added later if deployment topology requires it.
- The exact Spring AI version must be selected to match the project's Spring Boot 4.0.6 baseline and verified by the build during implementation.
