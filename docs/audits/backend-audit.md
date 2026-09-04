# Taska Backend Audit

Audit date: 2026-08-27

Scope: local repository only, including the backend, migrations, build and deployment configuration, tests, documentation, OpenSpec specifications, repository instructions and skills, and the frontend/Android API consumers.

Baseline: local master at 74a7678, with pre-existing uncommitted comment-only changes in FirebaseConfig.java and Jackson3JsonFormatMapper.java. Those changes were preserved.

Method: evidence-based, read-only inspection. All builds ran against a disposable copy under /tmp; this report is the only repository file created by the audit.

## Executive summary

The backend has a coherent, understandable single-module shape and a useful body of recurrence and priority-evaluation unit tests. It uses transport DTOs instead of serializing JPA entities directly, puts transaction boundaries primarily on services, protects application routes with a stateless JWT resource server, uses Flyway for PostgreSQL evolution, and returns generic Problem Details for unexpected failures. OpenSpec also gives the project substantially better behavioral documentation than most applications of this size.

The current release confidence is nevertheless low for several core paths. Six high-severity issues are supported by deterministic code paths: Android sends the wrong single-day task parameter; Android's running-timer request contradicts the non-null database schema; the release image has no viable Firebase credential path while local images can accidentally embed the ignored private key; reopening a modified recurring occurrence deletes its overrides; explicit null occurrence overrides are reported as inherited values; and the test suite never exercises the real PostgreSQL/Flyway/application configuration. The clean 111-test result therefore does not establish that a production-shaped application starts or that the clients and database agree.

The main systemic risks are:

- Backend, Angular, Android, MCP, and OpenSpec contracts are maintained manually and have drifted in query names, nullable semantics, fields, and side effects.
- Recurring occurrences are modeled with null serving both “inherit” and “explicitly clear,” and lifecycle operations do not form a tested state machine.
- Database constraints, DTO validation, and exception translation are not designed together, so invalid requests can persist incoherent state or become generic HTTP 500 responses.
- Release checks run only for tags and do not start the clean container against PostgreSQL. Firebase and schema contradictions can therefore reach the release workflow.
- Notification and analytics background paths make correctness, privacy, and delivery assumptions that are not observable in tests or operations.

The five highest-value actions are:

1. Restore the task query contract: accept the documented date parameter, keep a temporary singleDate compatibility alias, reject partial ranges, and add a cross-client contract test covering Angular and Android.
2. Decide and implement the running-time-entry model. The Android client clearly creates an open interval; either support that state in PostgreSQL and the response model or remove that client behavior and reject it explicitly.
3. Repair recurring-occurrence state semantics in two isolated changes: preserve overrides through close/reopen, then introduce a representation that distinguishes inherited values from explicit null clears.
4. Externalize Firebase and database credentials, default optional integrations deliberately, and add a clean-image startup smoke test using PostgreSQL.
5. Add a required pull-request backend workflow that runs the unit suite, real PostgreSQL/Flyway/API contract tests, OpenSpec validation, dependency/security checks, and a container health smoke test.

No Critical finding was identified. This is not evidence that production is free of critical vulnerabilities: no production configuration, database, access logs, dependency-vulnerability database, Firebase project, OpenAI account, or Authentik instance was available.

## Coverage and limitations

### Inspected

- All 100 production Java files and 15 backend test files (5,333 and 2,417 lines respectively).
- Maven configuration, dependency graph, application properties, Dockerfile, both Compose/Portainer definitions, Authentik blueprint, and all 20 Flyway migrations.
- Every REST controller mapping, Spring AI MCP tool, scheduler, security/configuration class, entity, repository, service, DTO, and mapper.
- All Angular HTTP services and their feature-level references.
- All Android Retrofit mappings, repositories, models, and feature/widget call sites.
- Root AGENTS.md, all six repository OpenSpec skills, all canonical OpenSpec specifications, archived changes relevant to the observed contracts, README, and CI workflows.
- Static searches for internal HTTP clients, scheduled/event/async callbacks, reflection-sensitive Spring components, logging of secrets/tokens, unbounded repository reads, unused repository methods, and analysis/lint/architecture tooling.

Only one instruction file is active: AGENTS.md at the repository root. There is no nested AGENTS.md or AGENTS.override.md. The six repository skills are all OpenSpec workflow skills under .codex/skills. There are no active OpenSpec changes.

The requested aidd-dev:04-audit skill was not available in this environment. The audit followed the full backend-applicable scope in the request directly. GitHub was not used, per the user's instruction.

### Versions established from the local build

| Component | Observed version | Evidence |
|---|---:|---|
| Java language/runtime target | 25; local Temurin 25.0.2 | taska-backend/pom.xml:20-23; java -version |
| Build tool | Maven; local 3.9.9; no Maven wrapper | mvn -version; no mvnw files |
| Spring Boot | 4.1.0 | taska-backend/pom.xml:7-12 |
| Spring Framework / Web MVC | 7.0.8 | resolved dependency tree |
| Spring Security | 7.1.0 | resolved dependency tree |
| Spring Data JPA | 4.1.0 | resolved dependency tree |
| Hibernate ORM | 7.4.1.Final | resolved dependency tree |
| Spring AI | 2.0.0 | taska-backend/pom.xml:22,25-34 |
| Flyway | 12.4.0 | resolved dependency tree |
| PostgreSQL driver / deployed database image | 42.7.11 / 18.1-alpine | resolved dependency tree; docker-compose.yml:3-4; deploy/stack-portainer.yaml:33-34 |
| Jackson | Jackson 3.1.4 plus transitive Jackson 2.21.4 | resolved dependency tree; the split is bridged intentionally by Jackson3JsonFormatMapper |
| Firebase Admin | 9.8.0 | taska-backend/pom.xml:84-88 |
| MapStruct | 1.6.3 | taska-backend/pom.xml:90-93 |
| iCal4j | 3.2.18 | taska-backend/pom.xml:100-103 |
| Embedded Tomcat | 11.0.22 | resolved dependency tree |

No upgrade is recommended solely from these version numbers. Vulnerability status was not verifiable without a configured vulnerability checker and current advisory database.

### Commands run

All Maven commands ran in a disposable backend copy that excluded target and the ignored Firebase service-account file.

| Command | Result |
|---|---|
| mvn -Dmaven.repo.local=/tmp/taska-m2 test | Passed: 111 tests, 0 failures, 0 errors, 0 skipped. The MCP integration opened a random local port successfully in this run. |
| mvn -o -DskipTests verify | Passed and produced the executable Boot jar. There are no additional verify-phase quality gates. |
| mvn -Dmaven.repo.local=/tmp/taska-m2 verify | Could not download maven-jar-plugin 3.5.0 because sandbox DNS/network access was unavailable; this was an environment limitation, not a source failure. |
| mvn -o verify | Re-ran 107 non-network tests successfully; the four MCP HTTP tests errored because this sandbox invocation denied opening a local socket. The earlier complete test run passed those same tests. |
| Maven dependency:tree | Passed; used to establish the versions above. |
| Maven dependency:analyze | Passed, but its starter-based “unused” and transitive “used undeclared” warnings are not actionable: Spring Boot starters, auto-configuration, annotations, and reflection invalidate its direct-bytecode assumptions. No dependency was classified as unused from this output alone. |
| openspec list --json | Passed; no active changes. |
| openspec validate --all --strict | Passed: 18 canonical specifications, 0 failures. This validates structure, not semantic agreement between specifications. |
| docker compose config --quiet | Passed; the Compose file is syntactically valid. It does not validate runtime health dependencies or application startup. |

There is no configured backend formatter, Checkstyle, PMD, SpotBugs, Error Prone, NullAway, Sonar, ArchUnit, JaCoCo gate, OWASP Dependency-Check, CycloneDX policy, or equivalent lint/static/dependency/architecture command. CI contains only release-tag image workflows. The Dockerfile's official backend build command is mvn package; README documents mvn spring-boot:run with the dev profile, but its directory/service names are stale (BA-028).

### Not verified

- Production startup, migrations against a copy of production data, query plans, row counts, latency, heap behavior, connection-pool behavior, or concurrent multi-instance behavior.
- Access logs or telemetry needed to prove that endpoints with no first-party caller are unused.
- Firebase delivery, token invalidation, credential mounting, OpenAI timeouts/retention, or Authentik issuer behavior.
- Current dependency CVEs or container-image vulnerabilities; no scanner/database was configured and no new tool was installed.
- Destructive migration duration/locking on real data. Historical V7, V9, V10, V17, and V19 transformations were inspected statically only.
- Frontend or Android builds/tests; those projects were read solely as API consumers, as requested.

## Architecture overview

Taska is one Maven module and one Spring Boot process. It uses feature-oriented packages under com.taska.domain for comments, filters, labels, notifications, planning calendars, priority evaluation, projects, sections, statistics, tasks, time entries, and version reporting. Each feature generally contains controller, service, repository, entity, request/response DTO, and MapStruct mapper classes. There is no separate domain module: services and entities depend directly on Spring/JPA, and feature packages have cycles (for example task to project/planning/priority and project back to task; task controllers to notification and notification back to task).

The normal request/data flow is:

~~~text
Angular / Android / external MCP client
                  |
        JWT resource-server filter
                  |
       +----------+-----------+
       |                      |
 REST controllers       Spring AI /mcp tools
       |                      |
       +------ application services -------+
                         |                  |
                Spring Data repositories   | outbound calls
                         |                  +-- OpenAI priority assessment
                    PostgreSQL             +-- Firebase Cloud Messaging

Scheduled paths:
TaskPriorityEvaluationScheduler -> OpenAI -> evaluation repository
TaskNotificationScheduler -> task/device repositories -> Firebase
~~~

REST task controllers publish account-scoped Firebase invalidation after successful mutations. MCP tools call TaskService directly, so that controller-owned side effect is bypassed (BA-012). This is the most concrete architectural cost of putting use-case completion behavior in a transport adapter. TaskService is also 634 lines and combines list-filter selection, creation, a legacy patch path, full replacements, occurrence expansion/state transitions, priority-evaluation invalidation, reminder selection, and planning-calendar validation. Its size is not itself a defect, but the recurrence regressions and transport divergence show that separating cohesive mutation/query responsibilities would now reduce real risk.

Persistence uses PostgreSQL/Flyway and raw UUID foreign-key fields rather than JPA associations. That keeps serialized graphs and lazy-loading behavior simple, which is reasonable for this application, but it also means semantic relationships such as “section belongs to project” and hierarchy acyclicity need explicit service/database enforcement; they currently do not have it (BA-015). Task labels are an eager ElementCollection.

Authentication is stateless OAuth2 bearer JWT validation against an Authentik issuer. With the documented mono-user shared-workspace model (README.md:56-65), every accepted token intentionally sees the same records. The audit therefore treats the absence of per-record ownership as an intentional design choice, not an IDOR finding. CSRF being disabled for a stateless bearer API is also acceptable. Moving to multi-user data isolation would be a product and migration project, not a hardening patch.

Entry points and integrations are:

- TaskaApplication with scheduling enabled: taska-backend/src/main/java/com/taska/TaskaApplication.java:7-11.
- REST controllers listed in the API matrix.
- Stateless authenticated Streamable HTTP MCP at POST /mcp, with ten project/task tools.
- PostgreSQL through Spring Data JPA and Flyway.
- Authentik/OIDC for JWT validation.
- Firebase Admin for reminders and task-change invalidations.
- Spring AI/OpenAI for scheduled priority evaluation.
- Actuator health endpoint.

## Findings

Classification in this table is the audit conclusion: Confirmed means the behavior follows deterministically from inspected code or an executed command; High/Medium confidence means runtime or product evidence is still needed. No unrelated issues are combined; repeated locations are grouped only when they share the same contract or control failure.

| ID | Severity | Confidence | Category | Concrete evidence | Problem | Practical impact | Recommended correction | Effort | Best prevention mechanism |
|---|---|---|---|---|---|---|---|---|---|
| BA-001 | High | Confirmed | REST/API contract | taska-backend/src/main/java/com/taska/domain/task/TaskController.java:30-64; taska-android/app/src/main/java/com/taska/android/data/api/TaskaApi.kt:27-34; taska-android/app/src/main/java/com/taska/android/ui/day/DayViewModel.kt:91-101; taska-android/app/src/main/java/com/taska/android/ui/today/TodayViewModel.kt:48-60; taska-frontend/src/app/core/services/task.service.ts:33-43 | Backend derives the single-day query name from the Java parameter singleDate, while Android sends date. The frontend contains a local singleDate workaround even though its public filter field is named date. | Android Today and Day requests fall through to TaskService.findAll instead of occurrence expansion, returning broad base-task data and losing recurring-occurrence semantics. | Make the public name explicit as @RequestParam("date"). During migration, accept singleDate as a deprecated alias and reject requests that supply both. Update Angular to date. | Small | Contract integration test |
| BA-002 | High | Confirmed | Persistence/API correctness | taska-backend/src/main/java/com/taska/domain/timeentry/TimeEntryRequest.java:6-22; taska-backend/src/main/java/com/taska/domain/timeentry/TimeEntry.java:29-43; taska-backend/src/main/resources/db/migration/V4__add_time_entries.sql:1-9; taska-backend/src/main/java/com/taska/domain/timeentry/TimeEntryService.java:52-65; taska-android/app/src/main/java/com/taska/android/data/model/TimeEntryRequest.kt:3-8; taska-android/app/src/main/java/com/taska/android/ui/taskdetail/TaskDetailViewModel.kt:277-289 | The request contract and Android model allow an in-progress entry with null endAt and optional projectId, but the entity and migration require both columns. Android's startTimer sends no endAt. No request validation catches the contradiction before persistence. | The Android running-timer action deterministically reaches a database not-null violation and becomes HTTP 500; the client silently swallows the exception. Invalid intervals such as end before start are also accepted until later logic fails. | Decide the product model. Given the active Android caller, support an explicit RUNNING state or nullable end_at with one-running-entry rules, then validate start/end/project semantics. If running entries are not supported, change the client and return a clear 400. | Medium | PostgreSQL integration test |
| BA-003 | High | High | Secrets/deployment | taska-backend/src/main/java/com/taska/config/FirebaseConfig.java:14-32; taska-backend/.gitignore:1; taska-backend/Dockerfile:5-13; .github/workflows/build-frontend-backend.yml:24-54; deploy/stack-portainer.yaml:16-31 | Firebase is enabled when the property is missing and defaults to a classpath service-account file. A clean release checkout omits that ignored file and the deployment supplies neither a mounted path nor a disable flag, so clean-image startup has no credential source. Conversely, a local Docker build copies src and Maven resources, so the ignored private key present under src/main/resources is embedded in the jar/image. The local key file was also mode 0644. | Release images can fail during bean initialization; developer-built images can permanently contain a private key in image layers and registries. | Remove credentials from source/resources and build contexts, rotate any key ever built into an image, mount a runtime secret at a filesystem path, make Firebase enablement explicit, and smoke-start the clean image in CI. | Medium | CI rule |
| BA-004 | High | Confirmed | Recurrence/data loss | taska-backend/src/main/java/com/taska/domain/task/TaskService.java:349-366,458-471,474-495; taska-backend/src/test/java/com/taska/domain/task/TaskServiceMutationTest.java:270-285,400-428 | A modified occurrence stores title/priority/schedule/deadline overrides. Closing it preserves the row and changes status to DONE; reopening deletes the entire row. Tests cover each operation independently but not modify, close, reopen as a sequence. | Reopening a completed modified occurrence permanently discards user edits and returns a virtual occurrence inherited from the series. | Define occurrence lifecycle transitions explicitly. Reopen a DONE row with overrides back to MODIFIED; delete only a DONE row that has no overrides. Add sequence tests for every state transition. | Medium | Unit state-transition test |
| BA-005 | High | Confirmed | Recurrence/null semantics | taska-backend/src/main/java/com/taska/domain/task/occurrence/OccurrenceUpdateRequest.java:8-12; taska-backend/src/main/java/com/taska/domain/task/TaskMapper.java:39-47; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:350-366; taska-frontend/src/app/core/services/task.service.ts:60-68; taska-frontend/src/app/core/services/task.service.spec.ts:70-80; openspec/specs/task-update-contract/spec.md:42-49 | TaskInstance null currently means both “no override; inherit the base” and “explicitly clear this occurrence's priority/schedule/deadline.” Clients deliberately serialize null, and the service persists it, but the mapper converts it back to the base value or occurrence schedule. | A successful single-occurrence unschedule, deadline clear, or priority clear does not produce the requested representation; a later base-series change can also alter an occurrence the user believed was explicitly cleared. | Persist field-presence/override state separately (for example per-field override flags or an explicit value-state type) and make the occurrence PUT a true replacement. Migrate existing rows with a documented inheritance rule. | Medium | Contract integration test |
| BA-006 | High | Confirmed | Test strategy | taska-backend/src/test/java/com/taska/mcp/McpEndpointIntegrationTest.java:32-45,114-141; taska-backend/src/test/java/com/taska/domain/task/TaskControllerPriorityEvaluationTest.java:19-45; no src/test/resources; executed test inventory | The only SpringBootTest excludes DataSource, Hibernate JPA, and Flyway and supplies mocked services/security. Other tests directly instantiate services/controllers with Mockito. No test starts the real application configuration or runs a repository/migration/controller path against PostgreSQL. | Entity/migration mismatch (BA-002), clean-start Firebase failure (BA-003), query parameter drift (BA-001/BA-008), database constraints, transaction behavior, and security routing can all pass the 111-test suite. | Add a small production-shaped suite: PostgreSQL/Flyway migration startup, real repository/entity mapping tests, MockMvc or live HTTP contract tests with real binding/security, and a clean-container health smoke test. Keep fast unit tests for recurrence logic. | Large | CI rule |
| BA-007 | Medium | Confirmed | Query correctness | taska-backend/src/main/java/com/taska/domain/task/TaskController.java:46-64; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:51-85; taska-frontend/src/app/core/services/task.service.ts:33-43; taska-frontend/src/app/shared/components/command-palette/command-palette.component.ts:134; taska-android/app/src/main/java/com/taska/android/widget/TaskWidgetRefresh.kt:85-103 | TaskService.findAll returns repository.findAll when no project/section/label/filter is supplied, ignoring showCompleted=false. Unknown named filters also return all. A lone from or to is silently ignored; the Android overdue widget sends only to. | Default lists can contain completed tasks; the command palette requests false but the Angular service omits false and receives all. The widget performs an unbounded fallback read and receives base recurring tasks instead of an occurrence range. | Define query composition and invalid combinations. Honor show_completed in every branch, reject unknown filters, and return 400 for a one-sided range (or define explicit open-ended range semantics). | Medium | REST contract integration test |
| BA-008 | Medium | Confirmed | Cross-client API drift | taska-backend/src/main/java/com/taska/domain/timeentry/TimeEntryController.java:21-38; taska-frontend/src/app/core/services/time-entry.service.ts:18-23; taska-frontend/src/app/features/time-tracker/time-tracker.component.ts:737-739; taska-backend/src/main/java/com/taska/domain/comment/CommentController.java:19-28; taska-frontend/src/app/core/services/comment.service.ts:12-16 | Backend expects project_id for comments and time entries; Angular sends projectId. The time-tracker actively supplies that filter. The comment project filter is currently only a latent service API. | Time-tracker project views receive every project's entries in the time range. A future project-comment caller will likewise be silently unscoped. | Select one public query naming convention, explicitly name every @RequestParam, update clients, and test the serialized query string for all supported consumers. | Small | Contract test |
| BA-009 | Medium | Confirmed | Input/error handling | taska-backend/src/main/java/com/taska/domain/task/TaskDeleteRequest.java:7-19; taska-backend/src/main/java/com/taska/domain/task/TaskCloseReopenRequest.java:5-12; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:389-423,440-495,540-547; taska-backend/src/main/java/com/taska/exception/GlobalExceptionHandler.java:58-73 | Scoped recurring delete does not validate that occurrenceScheduledAt is present before dereferencing it. Reopen allows a recurring task with no occurrence to take the base-task branch, and allows an arbitrary occurrence to be “reopened” without validating it or requiring a DONE instance. IllegalStateException becomes a generic 500. | Malformed recurrence requests can return 500, report success without changing the requested occurrence, or fabricate a virtual response for an invalid instant. | Add cross-field request validation, use the same validateOccurrence and current-state checks for close/reopen/delete, and map domain conflicts consistently to 409 or 400. | Small | Controller integration test |
| BA-010 | Medium | Confirmed | MCP correctness | taska-backend/src/main/java/com/taska/mcp/TaskMcpTools.java:52-60; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:166-176,577-586; taska-backend/src/test/java/com/taska/mcp/TaskMcpToolsTest.java:88-99 | MCP update passes priorityProvided=true only when clearPriority is true. A non-null priority with clearPriority false is validated but treated as omitted by TaskService. The only test covers clearing. | update_task reports success while leaving a requested priority unchanged. | Derive presence as priority non-null OR clearPriority true; reject priority plus clearPriority together; test set, clear, omit, and conflict cases. | Small | Unit test |
| BA-011 | Medium | Confirmed | MCP/OpenSpec contract | taska-backend/src/main/java/com/taska/mcp/TaskMcpTools.java:75-79,99-133,148-215; taska-backend/src/main/java/com/taska/mcp/ProjectMcpTools.java:40-58,69-86; openspec/specs/task-type-classification/spec.md:7-34; openspec/specs/planning-calendar-management/spec.md:33-46 | MCP task inputs/outputs omit task type, so MCP cannot create/update or identify APPOINTMENT tasks. MCP project inputs/outputs omit planningCalendarId and always pass null, so MCP cannot select or report the project's current calendar. | MCP representations lag the canonical domain and return incomplete “current state,” creating behavior differences between integrations and REST clients. | Use the same application DTO contract or a versioned adapter projection; add type and planningCalendarId to input/output schemas and test tool discovery plus round trips. | Medium | MCP contract test |
| BA-012 | Medium | Confirmed | Architecture/side effects | taska-backend/src/main/java/com/taska/domain/task/TaskController.java:73-78,99-119,129-169; taska-backend/src/main/java/com/taska/mcp/TaskMcpTools.java:24-27,41-72; openspec/specs/device-scoped-task-sync/spec.md:18-31 | Task-change publishing occurs only in REST controller methods. Authenticated MCP mutations invoke TaskService directly and have neither the principal nor a mutation facade that publishes the event. | Android devices do not receive the specified tasks_changed invalidation after MCP creates, updates, completes, or reopens a task. Other transports can bypass future controller-owned side effects the same way. | Move successful-mutation completion into a transport-independent application facade/domain event and publish after commit with actor context supplied by both REST and MCP security. | Medium | Integration test |
| BA-013 | Medium | Confirmed | Notification reliability | taska-backend/src/main/java/com/taska/domain/notification/TaskNotificationScheduler.java:34-53,66-75; taska-backend/src/main/java/com/taska/domain/notification/TaskChangePublisher.java:23-53 | Reminder delivery ignores the sendAsync Future and marks the task notified immediately. Unlike TaskChangePublisher, it neither observes failures nor removes invalid tokens. | Transient/permanent FCM failure permanently suppresses the reminder and leaves invalid tokens causing repeated work. There is no success/failure metric. | Mark delivery state only after acknowledged success, distinguish retryable/permanent failures, remove invalid tokens, and expose counts/latency/failures. Use a durable outbox if reminders must survive process crashes. | Medium | Unit test with controlled Future |
| BA-014 | Medium | Confirmed | Notification correctness | taska-backend/src/main/java/com/taska/domain/task/TaskRepository.java:119-124; taska-backend/src/main/java/com/taska/domain/notification/TaskNotificationScheduler.java:28-52; taska-backend/src/main/java/com/taska/domain/task/Task.java:138-144 | The “around 15 minutes” query has only an upper bound, so every old unnotified scheduled task is selected and described as due in 15 minutes. It includes recurring base tasks but stores isNotified only on the base task, so one delivery suppresses all later occurrences. | Restarting/enabling notifications can send misleading reminders for long-overdue tasks; recurring tasks get at most one correctly timed reminder for the entire series. | Define an explicit delivery window and occurrence-level reminder identity/state. Test late scheduler runs, restarts, recurring occurrences, reschedules, and no-token periods. | Medium | Unit state-transition test |
| BA-015 | Medium | High | Domain integrity | taska-backend/src/main/java/com/taska/domain/task/TaskService.java:110-137,604-623; taska-backend/src/main/resources/db/migration/V1__init.sql:14-38,54-59; taska-backend/src/main/java/com/taska/domain/project/ProjectService.java:56-67,79-98; taska-frontend/src/app/shared/components/add-project-modal/add-project-modal.component.ts:195-223; taska-backend/src/main/java/com/taska/domain/comment/Comment.java:10-16; taska-backend/src/main/java/com/taska/domain/comment/CommentService.java:41-46 | Cross-field invariants are documented but unenforced: task project/section/parent references are independently valid but may contradict each other; recurring tasks may lack schedule/RRULE; project parent updates allow self/descendant cycles and the web parent selector excludes only self; comments allow neither or both taskId/projectId. | Persisted data can be ambiguous or disappear from recurrence/date views; hierarchy traversal can loop; delete cascades can affect an unintended graph; comments cannot be assigned a single owner reliably. Runtime database contents were unavailable, so existing corruption is unverified. | Specify each invariant, validate it transactionally, and add database constraints where expressible (especially comment exactly-one). Detect hierarchy cycles with locked/current data before update. | Large | PostgreSQL integration test |
| BA-016 | Medium | Confirmed | Validation/error translation | taska-backend/src/main/java/com/taska/domain/label/LabelRepository.java:13-17; taska-backend/src/main/java/com/taska/domain/label/LabelService.java:45-51; taska-backend/src/main/resources/db/migration/V1__init.sql:46-51; taska-backend/src/main/java/com/taska/exception/GlobalExceptionHandler.java:58-73; taska-backend/src/main/java/com/taska/domain/project/ProjectService.java:117-127; taska-backend/src/main/java/com/taska/domain/notification/RegisterDeviceRequest.java:12-14; taska-backend/src/main/java/com/taska/domain/notification/DeviceToken.java:30-36 | Expected domain/database failures are not translated consistently. Duplicate label names hit the unique constraint because the declared existsByName check is unused; length limits exist mostly only in columns; inbox deletion and completed-occurrence conflicts throw IllegalStateException. These reach the catch-all 500. Several update controllers also omit @Valid. | Clients receive retryable-looking server errors for invalid input/conflicts, while oversized or blank updates fail late or persist inconsistently. Logs contain avoidable stack traces. | Validate at the request boundary and service invariant boundary, keep database constraints as the final authority, and translate known integrity/conflict exceptions to stable Problem Details (400/409) without parsing vendor text. | Medium | PostgreSQL controller integration test |
| BA-017 | Medium | High | Concurrency/persistence | taska-backend/src/main/resources/db/migration/V5__add_device_tokens.sql:1-6; taska-backend/src/main/resources/db/migration/V20__scope_device_tokens_to_accounts.sql:1-5; taska-backend/src/main/java/com/taska/domain/notification/RegisterDeviceController.java:27-35; taska-backend/src/main/java/com/taska/domain/notification/DeviceTokenRepository.java:12-19 | Device registration is a find-then-insert “upsert,” but token has no unique constraint. Concurrent registration can create duplicate rows; findByToken returns Optional and assumes at most one. | Duplicate sync/reminder sends, incorrect-result exceptions on later registration, and ambiguous account reassignment are possible. No concurrent runtime test was available. | Add a unique constraint/index on token, deduplicate existing rows in a migration, and use a database-safe upsert/retry strategy. | Medium | Concurrent PostgreSQL integration test |
| BA-018 | Medium | Confirmed | Secrets/configuration | taska-backend/src/main/resources/application.properties:2-4; deploy/stack-portainer.yaml:26-45 | The production stack sets only the datasource URL and relies on the checked-in static application username/password; the database service declares the same public literal. | Anyone with repository access knows the production database credential. The database is on an internal Docker network, which limits direct exposure but does not protect it after compromise of any attached service or backup/config leak. | Supply database username/password through a secret manager or mounted Docker secret, remove production-capable credentials from default properties, and rotate the deployed password. Keep explicit dev-only defaults in a dev profile. | Small | CI secret-policy rule |
| BA-019 | Medium | Medium | Privacy/external integration | taska-backend/src/main/java/com/taska/domain/priority/TaskPriorityEvaluationScheduler.java:14-25; taska-backend/src/main/java/com/taska/domain/priority/PriorityEvaluationBatchRequest.java:9-17; taska-backend/src/main/java/com/taska/domain/priority/OpenAiPriorityAssessmentClient.java:19-26; taska-backend/src/main/resources/application.properties:12-14,26-27; README.md:67-77 | Every eligible TODO is automatically sent to OpenAI with UUID, title, description, scheduled time, and creation time. There is no enable/disable property, user-facing README disclosure, or data-classification/redaction rule. The feature is intentional in OpenSpec, but consent, retention, and deployment policy are not documented locally. | Private task text is processed by a third party by default. Whether this is acceptable requires product/privacy confirmation and the external account's retention controls. | Make the integration explicitly configurable, document exactly what leaves the system and the provider/retention policy, minimize fields, and obtain the necessary product/privacy decision before enabling it by default. | Medium | Human ADR |
| BA-020 | Medium | Confirmed | Time correctness | taska-backend/src/main/java/com/taska/domain/stats/StatsService.java:45-59; taska-backend/src/main/resources/application.properties:11-13; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:51-63 | Task date queries use the configured Europe/Paris calendar zone, but statistics use ZoneId.systemDefault. The Alpine runtime normally derives its own zone independently of Taska configuration. | Overdue counts, completion day buckets, weekly totals, and streaks can disagree with Today/calendar views around local midnight and daylight-saving transitions. | Inject TaskaProperties (and preferably Clock) into StatsService and use the configured calendar zone for all day boundaries. | Small | Unit test with fixed Clock/zone |
| BA-021 | Medium | High | Performance/scalability | taska-backend/src/main/java/com/taska/domain/task/Task.java:70-78; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:64-85; taska-backend/src/main/java/com/taska/domain/filter/FilterService.java:104-124; taska-backend/src/main/java/com/taska/domain/stats/StatsService.java:91-108; all collection controllers return List | Task list/filter fallbacks are unbounded and labels are always eager. Stats loads all tasks twice and all projects once in addition to count/recent queries. No endpoint offers pagination or a maximum range. | Query count, transferred rows, label joins/selects, heap use, and serialization time grow with the workspace. This is a high-confidence risk, not a measured production defect; row counts and SQL telemetry are missing. | First capture row counts/query metrics. Replace stats scans with database aggregates, make high-cardinality task/comment/time-entry queries bounded, and fetch labels deliberately for DTO projections/pages rather than globally eager. | Large | Query-count integration test |
| BA-022 | Medium | High | Persistence performance | taska-backend/src/main/java/com/taska/domain/planningcalendar/PlanningCalendarService.java:5-12; taska-backend/src/main/java/com/taska/domain/project/ProjectService.java:91-95; taska-backend/src/main/resources/db/migration/V19__add_planning_calendars.sql:8-24 | Listing calendars queries rules once per calendar. Changing a project's calendar queries all scheduled tasks and then reloads the same calendar rules once per task. planning_calendar_rules.calendar_id has a foreign key but no index. | Calendar management and project reassignment have deterministic N+1 query shapes and repeated rule parsing. Actual latency is unmeasured. | Batch-load rules by calendar, load target rules once per project validation, and add an index on calendar_id after confirming the migration plan. | Medium | Query-count integration test |
| BA-023 | Medium | Confirmed | CI/release assurance | .github/workflows/release.yml:1-35; .github/workflows/build-frontend-backend.yml:1-56; taska-backend/Dockerfile:1-17 | CI runs only on release tags. The image build executes mvn package, but there is no pull-request/push workflow, no explicit OpenSpec validation, PostgreSQL migration test, architecture/static analysis, dependency vulnerability check, or container startup check before a release tag. | Broken contracts/configuration can be merged and are first exercised while producing/pushing deployable artifacts. A passing local suite is not an enforceable gate. | Add a required PR workflow and reuse the same verified artifact in release. Gate on backend tests, PostgreSQL/Flyway integration, OpenSpec validation, selected static/dependency checks, and clean-image health startup. | Medium | CI rule |
| BA-024 | Medium | Confirmed | Specification consistency | openspec/specs/taska-mcp-server/spec.md:7-20; openspec/specs/task-scheduling-and-priority-fields/spec.md:88; README.md:72; openspec validate command result | One canonical spec says MCP shall not accept or return dueAt, while another canonical spec and README require dueAt as an independent MCP deadline. The implementation accepts/returns it. Strict validation passes because it checks format, not semantic contradictions. | OpenSpec is declared authoritative, but an implementer or reviewer cannot satisfy both requirements and may regress either behavior while remaining “validated.” | Make an explicit product decision, update/replace the obsolete canonical requirement, and record ordering/supersession semantics for future capability changes. | Small | Human ADR |
| BA-025 | Medium | Confirmed | Repository instructions/skills | AGENTS.md:69-89,91-113,115-142; .codex/skills/openspec-explore/SKILL.md:13-17; .codex/skills/openspec-archive-change/SKILL.md:20-70; .codex/skills/openspec-apply-change/SKILL.md:71-92; .codex/skills/openspec-propose/SKILL.md:30-83 | Repository skills contradict the authoritative workflow: Explore permits OpenSpec artifact creation while AGENTS forbids it; Archive permits incomplete tasks and skipping sync while AGENTS requires completion and sync; Apply marks tasks complete before the root rule's verification gate. Skills also name AskUserQuestion, TodoWrite, Task, and openspec-continue-change capabilities that are not provided by these repository skills in this environment. | An agent following a named repository skill can violate branch/spec/archive policy or stop on unavailable tooling. The instructions are ambiguous precisely in high-impact workflow transitions. | Replace generated generic skills with Taska-specific wrappers that explicitly defer to AGENTS, use available tools, and encode the issue/PR/label checks. Add a repository audit skill separately; proposed text appears below. | Medium | Repository-scoped Codex skill |
| BA-026 | Medium | Confirmed | Validation/contract | taska-backend/src/main/java/com/taska/domain/task/TaskRequest.java:54-62; taska-backend/src/main/java/com/taska/domain/task/TaskUpdateRequest.java:20-29; taska-backend/src/main/java/com/taska/domain/task/TaskService.java:604-622; openspec/specs/task-update-contract/spec.md:7-19 | Create requires a positive estimateMinutes, but full PUT replacement omits @Positive and assigns the supplied integer directly. | A negative estimate is rejected on create but accepted on replacement despite the canonical requirement to reject invalid complete replacements. | Add the same positive constraint to TaskUpdateRequest and a real controller validation test proving no mutation occurs. | Small | Controller integration test |
| BA-027 | Low | Confirmed | Sensitive logging | taska-backend/src/main/java/com/taska/domain/notification/RegisterDeviceController.java:27-36; taska-backend/src/main/java/com/taska/domain/notification/TaskNotificationScheduler.java:66-74 | Full Firebase registration tokens are written to debug logs during registration and reminder sending. | Debug logging in a support incident can place persistent device identifiers/routing credentials in log stores and backups. | Log the database token record ID or a short one-way fingerprint, never the raw token. Review log retention. | Small | Static-analysis logging rule |
| BA-028 | Low | Confirmed | Documentation/operations | README.md:3-40; taska-backend/pom.xml:7-23; docker-compose.yml:20-40,55-73; deploy/stack-portainer.yaml:3-18 | README reports Boot 3.5/PostgreSQL 17 and directories backend/frontend; actual code is Boot 4.1/PostgreSQL 18.1 and taska-backend/taska-frontend. It tells users to start a nonexistent postgres service. Root Compose has build directives commented and depends on authentik-server being service_healthy even though that service defines no healthcheck. Image versions also drift between root Compose (0.0.16), deployment (0.0.17), and application/MCP metadata (0.0.8). | Official quick-start/run instructions are not a reliable way to reproduce the application, and operators cannot tell which version surface is authoritative. docker compose config validates syntax but not the missing healthcheck condition at runtime. | Correct and smoke-test the documented commands; use one version source for image/application/MCP metadata; either add the Authentik healthcheck or remove that condition. | Small | CI documentation smoke test |
| BA-029 | Low | Confirmed | Build reproducibility | taska-backend/Dockerfile:1-13; taska-backend/pom.xml:124-159; no mvnw/.mvn wrapper files | The container build installs whatever Maven apt currently supplies and uses mutable Temurin tags; local builds use the machine Maven. Plugin versions are mostly inherited, but the build-tool distribution itself is not pinned or checksummed. | The same source can build with different Maven/base-image bits over time, and a compromised/misconfigured package mirror has a broad build-stage role. No observed build failure is attributed to this. | Add a Maven wrapper with a pinned distribution/checksum and pin base images by reviewed digest through the normal dependency-update process. | Small | CI reproducibility check |
| BA-030 | Informational | Confirmed | Migration operations | taska-backend/src/main/resources/db/migration/V7__due_at_migration.sql:1-23; V9__convert_timestamps_to_timestamptz.sql:1-14; V10__fix_allday_task_timestamps.sql:1-12; V17__rename_task_scheduling_fields.sql:1-14; V19__add_planning_calendars.sql:17-24 | Historical upgrade steps drop the old due-date columns after copying, reinterpret every naive timestamp as Europe/Paris, repair all-day rows in a later migration, rename scheduling columns, and backfill then make project planning calendars non-null. There is no automated old-version-to-current migration test or checked operational/forward-fix note. | Fresh-schema syntax is likely covered only when the application starts; upgrade correctness, lock duration, backup expectations, and behavior for installations that did not use the Paris assumption remain unverified. These are historical risks, not evidence that current stored data is wrong. | Preserve the immutable migration history, add representative upgrade fixtures and backup/forward-fix procedures, and test the full chain on PostgreSQL before future data transformations. | Medium | PostgreSQL migration integration test |

## API usage matrix

Paths were normalized against Angular environment.apiUrl (taska-frontend/src/environments/environment.ts:3 and environment.prod.ts:3) and Android BuildConfig.API_URL (taska-android/app/build.gradle.kts:47,57). Dynamic IDs and encoded occurrence timestamps were normalized to path placeholders. MCP tools were counted as direct application-service consumers, not as callers of REST routes.

Path abbreviations used below:

- B = taska-backend/src/main/java/com/taska
- F = taska-frontend/src/app
- A = taska-android/app/src/main/java/com/taska/android

“Service-only” means a client service method exists but no feature call was found. “No first-party caller” means no Angular, Android, backend internal HTTP, test, scheduled, webhook, documented external, or generated-client caller was found statically; it is not proof of runtime non-use. Angular TaskService has HTTP-mock tests for create and the three update forms, and PlanningCalendarService has a list-client test. Android has no Retrofit/MockWebServer API contract tests.

| Backend method and route | Backend controller | Frontend callers | Android callers | Other known callers | Tests covering the endpoint/use case | Usage status | Confidence | Evidence and caveats |
|---|---|---|---|---|---|---|---|---|
| GET /comments?task_id=&project_id= | B/domain/comment/CommentController.java:26-28 | CommentService.getComments; TaskDetail uses task_id at F/features/task-detail/task-detail.component.ts:180 | — | — | No backend HTTP test | Active for task comments; project filter client name is broken | High | Angular sends task_id correctly but sends projectId, not project_id (BA-008). No feature calls project comments. |
| POST /comments | B/domain/comment/CommentController.java:37-40 | CommentService.createComment; TaskDetail:326 | — | — | No backend HTTP test | Active | High | Body invariant is incomplete (BA-015). |
| PUT /comments/{id} | B/domain/comment/CommentController.java:50-52 | CommentService.updateComment exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference; update also omits @Valid. Runtime logs needed before removal. |
| DELETE /comments/{id} | B/domain/comment/CommentController.java:60-63 | CommentService.deleteComment exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference. Runtime logs needed before removal. |
| GET /filters | B/domain/filter/FilterController.java:27-29 | FilterService.loadFilters; AppComponent and Filters feature | — | — | No backend HTTP test | Active | High | F/app.component.ts:143; F/features/filters/filters.component.ts:165. |
| GET /filters/{id} | B/domain/filter/FilterController.java:38-40 | FilterService.getFilter; FilterTasks:77 | — | — | No backend HTTP test | Active | High | Dynamic route normalized. |
| POST /filters | B/domain/filter/FilterController.java:49-52 | FilterService.createFilter; Filters:211 | — | — | No backend HTTP test | Active | High | — |
| PUT /filters/{id} | B/domain/filter/FilterController.java:62-64 | FilterService.updateFilter; Filters:202,224 | — | — | No backend HTTP test | Active | High | — |
| DELETE /filters/{id} | B/domain/filter/FilterController.java:72-75 | FilterService.deleteFilter; Filters:231 | — | — | No backend HTTP test | Active | High | — |
| GET /filters/{id}/tasks | B/domain/filter/FilterController.java:84-86 | FilterService.getFilterTasks; FilterTasks:78,86 | — | — | No backend HTTP test; no FilterService unit test | Active | High | Unbounded fallback described in BA-021. |
| GET /labels | B/domain/label/LabelController.java:24-26 | LabelService.loadLabels; AppComponent:142 and shared label consumers | TaskaApi.getLabels; inbox/task detail | — | No backend HTTP test | Active on both clients | High | A/data/api/TaskaApi.kt:72-73. |
| POST /labels | B/domain/label/LabelController.java:35-38 | LabelService.createLabel; Labels:125 | — | — | No backend HTTP test | Active | High | Duplicate name becomes database conflict/500 (BA-016). |
| GET /labels/{id} | B/domain/label/LabelController.java:47-49 | — | — | — | No backend HTTP test | No first-party caller candidate | Medium | No Angular service method, Retrofit route, docs, or internal HTTP caller found. |
| PUT /labels/{id} | B/domain/label/LabelController.java:59-61 | LabelService.updateLabel; Labels:123 | — | — | No backend HTTP test | Active | High | Controller omits @Valid. |
| DELETE /labels/{id} | B/domain/label/LabelController.java:69-72 | LabelService.deleteLabel; Labels:134 | — | — | No backend HTTP test | Active | High | — |
| GET /planning-calendars | B/domain/planningcalendar/PlanningCalendarController.java:3 | PlanningCalendarService.list; management view and project modal | — | — | PlanningCalendarService unit tests; Angular HTTP-mock list test; no backend HTTP test | Active | High | N+1 path in BA-022. |
| GET /planning-calendars/{id} | B/domain/planningcalendar/PlanningCalendarController.java:3 | — | — | — | PlanningCalendarService unit test only | No first-party caller candidate | Medium | Canonical spec requires retrieval; external use remains plausible. |
| POST /planning-calendars | B/domain/planningcalendar/PlanningCalendarController.java:3 | PlanningCalendarService.create; management view | — | — | Service unit tests; no backend HTTP test | Active | High | F/features/planning-calendars/planning-calendars.component.ts:6. |
| PUT /planning-calendars/{id} | B/domain/planningcalendar/PlanningCalendarController.java:3 | PlanningCalendarService.update; management view | — | — | Service unit tests; no backend HTTP test | Active | High | Same one-line source location. |
| GET /projects | B/domain/project/ProjectController.java:30-32 | ProjectService.loadProjects; AppComponent | TaskaApi.getProjects; multiple views | MCP list_projects calls ProjectService directly | No REST HTTP test; MCP tool/integration tests use mocks | Active on both clients | High | A/data/api/TaskaApi.kt:24-25. |
| POST /projects | B/domain/project/ProjectController.java:41-44 | ProjectService.createProject; project modal | — | MCP create_project calls ProjectService directly | No backend HTTP test; MCP tool unit test | Active | High | MCP omits calendar selection (BA-011). |
| GET /projects/{id} | B/domain/project/ProjectController.java:53-55 | ProjectService.getProject exists | — | MCP get_project calls ProjectService directly, not route | No REST HTTP test; MCP tool unit test | REST service-only candidate | Medium | No Angular feature reference; MCP preserves the use case but not the endpoint. |
| PUT /projects/{id} | B/domain/project/ProjectController.java:65-67 | ProjectService.updateProject; project modal | — | MCP update_project calls ProjectService directly | No backend HTTP test; MCP tool unit test | Active | High | Parent cycles and MCP calendar drift: BA-015, BA-011. |
| PATCH /projects/reorder | B/domain/project/ProjectController.java:75-78 | ProjectService.reorderProjects exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference found. |
| DELETE /projects/{id} | B/domain/project/ProjectController.java:87-90 | ProjectService.deleteProject; AppComponent:106 | — | — | No backend HTTP test | Active | High | Inbox conflict currently becomes 500 (BA-016). |
| GET /projects/{id}/tasks | B/domain/project/ProjectController.java:99-101 | ProjectService.getProjectTasks exists | — | — | No backend HTTP test | Service-only candidate | Medium | Project view uses GET /tasks?project_id instead. |
| GET /projects/{id}/sections | B/domain/project/ProjectController.java:110-112 | ProjectService.getProjectSections; ProjectView:120 | — | — | No backend HTTP test | Active | High | This is the live section-loading route. |
| GET /sections?project_id= | B/domain/section/SectionController.java:25-27 | SectionService.getSections exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference; project view uses /projects/{id}/sections. |
| POST /sections | B/domain/section/SectionController.java:36-39 | SectionService.createSection exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference found. |
| GET /sections/{id} | B/domain/section/SectionController.java:48-50 | — | — | — | No backend HTTP test | No first-party caller candidate | Medium | No Angular service method or Retrofit route. |
| PUT /sections/{id} | B/domain/section/SectionController.java:60-62 | SectionService.updateSection exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference; controller omits @Valid. |
| DELETE /sections/{id} | B/domain/section/SectionController.java:70-73 | SectionService.deleteSection exists | — | — | No backend HTTP test | Service-only candidate | Medium | No feature reference found. |
| GET /stats/overview | B/domain/stats/StatsController.java:21-23 | StatsService.getOverview; Stats feature:120 | — | — | No backend HTTP or StatsService test | Active | High | Timezone/performance issues: BA-020, BA-021. |
| GET /tasks?project_id=&section_id=&label=&filter=&show_completed=&date=&from=&to= | B/domain/task/TaskController.java:46-64 | TaskService.getTasks; Today, Week, Inbox, Project, label, done, tracker, command palette | TaskaApi.getTasks; Today, Day, Week, Inbox, Project, widgets, search | MCP list_tasks calls TaskService directly with a smaller filter set | TaskService query unit tests; no REST HTTP test; no Android contract test | Active; date and query semantics broken | High | BA-001 and BA-007. Backend currently names date as singleDate; Angular compensates, Android does not. |
| POST /tasks | B/domain/task/TaskController.java:73-78 | TaskService.createTask; quick add, CSV import, details, command palette | TaskaApi.createTask; Android add/subtask flows | MCP create_task calls TaskService directly | TaskService mutation tests; Angular HTTP-mock create tests; no REST HTTP test | Active on both clients | High | MCP side effects/fields differ (BA-011, BA-012). |
| GET /tasks/{id} | B/domain/task/TaskController.java:87-89 | TaskService.getTask; also chained before every Angular full replacement | TaskaApi.getTask; TaskDetail | MCP get_task calls TaskService directly | Angular update HTTP-mock tests include this GET; no backend HTTP test | Active on both clients | High | Dynamic ID normalized. |
| GET /tasks/{id}/priority-evaluation | B/domain/task/TaskController.java:92-96 | — | — | — | Direct controller unit test only, not HTTP | No first-party caller candidate | Medium | Canonical spec requires a read operation, so external/future use is plausible. |
| PUT /tasks/{id} | B/domain/task/TaskController.java:99-103 | TaskService.updateTask full replacement; many task views | TaskaApi.updateTask; day/week/detail/etc. | MCP update_task uses legacy TaskService patch directly, not route | TaskService mutation and Angular HTTP-mock tests; no backend HTTP test | Active on both clients | High | Negative estimate gap BA-026; MCP priority bug is separate BA-010. |
| PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}/following | B/domain/task/TaskController.java:106-111 | TaskService.updateTask when FROM_THIS | TaskaApi.updateFollowingTask | MCP legacy scoped update calls service directly | TaskService mutation and Angular HTTP-mock tests; no backend HTTP test | Active on both clients | High | Path placeholder names normalize correctly. |
| PUT /tasks/{id}/occurrences/{occurrenceScheduledAt} | B/domain/task/TaskController.java:114-119 | TaskService.updateTask when THIS_ONLY | TaskaApi.updateOccurrence | MCP legacy scoped update calls service directly | TaskService mutation and Angular HTTP-mock tests; no backend HTTP test | Active on both clients; null semantics broken | High | BA-005. |
| DELETE /tasks/{id} | B/domain/task/TaskController.java:129-135 | TaskService.deleteTask; detail/project/etc. | TaskaApi.deleteTask with body | MCP deletion intentionally not exposed | TaskService mutation tests; no backend HTTP test | Active on both clients | High | Scoped missing occurrence error BA-009. |
| POST /tasks/{id}/close | B/domain/task/TaskController.java:146-152 | TaskService.closeTask; all task views/tracker | TaskaApi.closeTask; views/widgets | MCP complete_task calls service directly | TaskService mutation/MCP unit tests; no REST HTTP test | Active on both clients | High | MCP sync bypass BA-012. |
| POST /tasks/{id}/reopen | B/domain/task/TaskController.java:163-169 | TaskService.reopenTask; all task views | TaskaApi.reopenTask; views/widgets | MCP reopen_task calls service directly | TaskService mutation/MCP unit tests; no REST HTTP test | Active on both clients; data-loss path | High | BA-004 and BA-009. |
| GET /tasks/{id}/subtasks | B/domain/task/TaskController.java:178-180 | TaskService.getSubtasks; TaskDetail:176 | TaskaApi.getSubtasks; TaskDetail | — | TaskService mutation/query coverage only; no HTTP test | Active on both clients | High | — |
| GET /time-entries?project_id=&start=&end= | B/domain/timeentry/TimeEntryController.java:31-38 | TimeEntryService.getEntries; time tracker:737 | — | — | No backend HTTP or service test | Active; project filter broken | High | Angular sends projectId (BA-008); project-only filter is also ignored by TimeEntryService.java:31-39. |
| POST /time-entries | B/domain/timeentry/TimeEntryController.java:47-50 | TimeEntryService.createEntry; time tracker | TaskaApi.createTimeEntry; TaskDetail timer | — | No backend HTTP, service, or Android API test | Active on both clients; Android call fails | High | BA-002. |
| GET /time-entries/{id} | B/domain/timeentry/TimeEntryController.java:59-61 | — | — | — | No backend HTTP test | No first-party caller candidate | Medium | No Angular service method or Retrofit route. |
| PUT /time-entries/{id} | B/domain/timeentry/TimeEntryController.java:71-73 | TimeEntryService.updateEntry; time tracker | — | — | No backend HTTP or service test | Active | High | Request is unvalidated; endAt cannot be cleared. |
| DELETE /time-entries/{id} | B/domain/timeentry/TimeEntryController.java:81-84 | TimeEntryService.deleteEntry; time tracker | — | — | No backend HTTP or service test | Active | High | — |
| GET /version | B/domain/version/VersionController.java:26-28 | VersionService.getVersion; About dialog | — | — | No backend HTTP test | Active | High | Version source differs from POM/image tags (BA-028). |
| POST /register-device | B/domain/notification/RegisterDeviceController.java:27-35 | — | TaskaApi.registerDevice; TodayActivity:75 and FirebaseMessagingService:32 | — | Direct controller unit test only | Active on Android | High | Race and logging issues BA-017, BA-027. |
| POST /mcp | Spring AI auto-configured at application.properties:16-24; secured by B/security/WebSecurityConfiguration.java:47-51 | — | — | Documented external MCP clients | Four live HTTP integration tests cover auth, initialize, discovery, and mocked list calls | Active external integration | High | README.md:54-65. Static client absence is not non-use. |
| GET /actuator/health and /actuator/health/** | Spring Boot Actuator; permitted at B/security/WebSecurityConfiguration.java:47-50 | — | — | Intended for operators/orchestrators | No production-context health test | Framework/operations endpoint | High | No checked-in deployment healthcheck calls it; retain as operational surface. |

### Matrix conclusions

- There are three confirmed active contract failures: Android date versus backend singleDate (BA-001), Angular projectId versus backend project_id for time entries (BA-008), and Android's open time-entry body versus the database schema (BA-002).
- The frontend has adapted to one backend implementation detail rather than the documented name: TaskFilters.date is serialized as singleDate.
- No OpenAPI document or generated client exists. The Angular and Android types are independent hand-written contracts, and the two available Angular HTTP-mock suites do not compare against backend mappings.
- No REST endpoint is classified as confirmed unused. The candidates need production access evidence before deprecation or removal.

## Dead-code candidates

### Confirmed production-dead or redundant internals

These conclusions account for Spring Data proxy generation, component scanning, configuration binding, JPA callbacks/construction, Jackson/record serialization, MapStruct generation, scheduled methods, and MCP annotation discovery. “Production-dead” allows a symbol to remain referenced by tests.

| Candidate | Confidence | Evidence and search method | Recommended disposition |
|---|---|---|---|
| LabelRepository.existsByName | Confirmed unreferenced | Declaration at taska-backend/src/main/java/com/taska/domain/label/LabelRepository.java:13-17. Exact-symbol search across backend main/test, frontend, and Android found no invocation. Spring Data generates a proxy method only when called; it is not a framework callback. | Either remove it and correct its misleading “used to enforce” Javadoc, or deliberately use it only for a friendly pre-check while retaining the database unique constraint for race safety (BA-016). |
| SectionRepository.deleteByProjectId | Confirmed unreferenced | Declaration at taska-backend/src/main/java/com/taska/domain/section/SectionRepository.java:12-13; exact-symbol search found no call. V1 has sections.project_id ON DELETE CASCADE at V1__init.sql:14-19. | Remove after a PostgreSQL cascade test proves project deletion behavior. |
| TimeEntryRepository.deleteByProjectId | Confirmed unreferenced | Declaration at taska-backend/src/main/java/com/taska/domain/timeentry/TimeEntryRepository.java:17-18; exact-symbol search found no call. V4 has project_id ON DELETE CASCADE at V4__add_time_entries.sql:1-9. | Remove after the same project-deletion integration test covers time entries. |
| TaskService.findOccurrencesForDateRange(LocalDate, LocalDate) overload | Confirmed production-unreachable; test helper | Defined at taska-backend/src/main/java/com/taska/domain/task/TaskService.java:277-278. Only test calls were found; TaskController calls the three-argument overload at lines 57-61. It has no framework annotation. | Make tests pass false explicitly and remove the overload, unless it is intentionally retained as a supported Java API (no such consumer/documentation was found). |
| TaskService.update(UUID, TaskRequest) overload | Confirmed production-unreachable; legacy test entry | Defined at taska-backend/src/main/java/com/taska/domain/task/TaskService.java:162-164. Production MCP calls the three-argument overload at TaskMcpTools.java:59; all two-argument calls are in TaskServiceMutationTest. It has no framework annotation. | Remove after updating tests to exercise explicit field-presence semantics; doing so will reduce the chance that tests validate an entry point no transport uses. |

No entire production type, Spring bean, mapper, DTO, migration, or declared dependency was proven dead. Maven dependency:analyze's starter warnings were rejected because they conflict with direct evidence of auto-configuration/annotation use.

### Candidates requiring runtime validation

| Candidate | Static evidence | Confidence | Runtime evidence required before deprecation/deletion |
|---|---|---|---|
| GET /labels/{id}; GET /sections/{id}; GET /time-entries/{id}; GET /tasks/{id}/priority-evaluation | No Angular/Android route, internal HTTP call, generated client, endpoint test, webhook, or README caller found. The priority-evaluation operation is required by a canonical spec. | Medium | Reverse-proxy/application access logs by normalized method/path over a representative retention window; external client inventory; support/automation scripts. |
| GET /projects/{id}; GET /projects/{id}/tasks; PATCH /projects/reorder; all /sections service routes | Angular service methods exist, but no feature references were found. MCP uses project get directly through ProjectService, not REST. Project view uses /tasks?project_id and /projects/{id}/sections. | Medium | Client-side telemetry/build variants, API access logs, desktop/Tauri runtime traces, and confirmation that no released older client uses them. |
| GET /planning-calendars/{id} | No first-party caller, but canonical OpenSpec explicitly requires retrieval. | Low as dead-code claim | Product confirmation and external API logs. Static absence is especially weak because the endpoint is specified behavior. |
| PUT and DELETE /comments/{id}; project_id mode of GET /comments | Angular methods/signature exist but no feature caller. The project filter is currently serialized with the wrong name. | Medium | Access logs including query keys, released-client inventory, and product decision on project comments. |
| TaskaProperties.Firebase fields and TaskaProperties.Notification fields | Bound reflectively, but no getters are called; conditionals/placeholders read the Environment directly. They may be documentation scaffolding rather than live typed configuration. | Medium | Configuration metadata expectations and planned consumers. If retained, add @Validated typed use; if not, remove only after confirming no external configuration tooling relies on generated metadata. |

No static or dynamic backend-internal HTTP client was found. No webhook controller, scheduled HTTP callback, or generated API client was found. The /mcp and Actuator health surfaces are documented/framework consumers and are not dead-code candidates.

## Prioritized remediation plan

The order below is based on risk reduction and prerequisites, not ease.

### Immediate contract and release safety

1. Establish failing tests for BA-001, BA-002, BA-004, BA-005, and BA-003 before changing behavior. Use a real PostgreSQL schema and HTTP binding for the API/schema cases.
2. In a small compatibility change, expose date explicitly, accept singleDate temporarily, reject conflicting/partial ranges, and fix show_completed behavior (BA-001, BA-007). Update Angular to the canonical date name. Do not combine this with time-entry or recurrence changes.
3. Decide the open-time-entry product contract, then implement the API, entity, migration, Android model, response semantics, and interval validation together (BA-002). This must be a dedicated migration change; combining it with unrelated database constraints would make rollback and diagnosis harder.
4. Fix recurrence reopen data loss without changing the occurrence schema (BA-004). Preserve modified fields and add transition-table tests. Keep this separate from the explicit-null schema change so regressions can be attributed.
5. Design and migrate explicit occurrence override state for null clears (BA-005), then apply the same state model consistently to replace, close, reopen, mapping, and clients.
6. Externalize Firebase credentials and production database credentials, rotate them, add a clean image smoke start, and make optional integrations explicit (BA-003, BA-018). Credential rotation/deployment should not be coupled to behavioral API releases.

### Correctness and integration consistency

7. Add the required PR workflow and production-shaped PostgreSQL/Flyway/application tests (BA-006, BA-023). Make this infrastructure change independently so subsequent fixes gain a stable gate.
8. Fix Angular project query names and normalize all query-parameter annotations; add a cross-client route contract test (BA-008).
9. Repair recurrence request validation and stable 4xx/409 error translation (BA-009, BA-016). Apply database length/check constraints in small, operationally reviewed migrations rather than one broad “validation cleanup.”
10. Fix MCP priority presence, add task type/planningCalendarId, and route all task mutations through a transport-independent mutation facade that publishes after commit (BA-010, BA-011, BA-012). The priority bug can be a small first change; the facade is structural and should be separately reviewable.
11. Repair reminder selection/state first, then delivery acknowledgement/retry/invalid-token cleanup (BA-013, BA-014). Add a unique token migration before relying on exactly-once targeting (BA-017).
12. Add cross-field invariants one aggregate at a time: comments exactly-one target, project acyclicity, task project/section/parent coherence, then recurrence coherence (BA-015). Each migration should include preflight queries, representative upgrade fixtures, and a policy for existing invalid rows (BA-030).
13. Add @Positive to full replacement and align create/update validation (BA-026).

### Privacy, performance, and maintainability

14. Record the OpenAI data-processing decision and add an explicit feature switch/disclosure/minimized payload (BA-019).
15. Unify all local-day calculations on configured timezone plus an injectable Clock (BA-020).
16. Instrument database query counts/latency and row counts, then replace Stats scans and bound high-cardinality lists (BA-021). Do not combine query behavior changes with API contract fixes.
17. Batch planning-calendar rules and add the missing rule foreign-key index after measuring/explaining the migration (BA-022).
18. Reconcile the dueAt specifications and replace incompatible OpenSpec skills before the next workflow transition (BA-024, BA-025).
19. Remove raw token logging, correct executable documentation, and pin build tooling (BA-027, BA-028, BA-029).
20. Only after access-log review, deprecate any endpoint candidates and remove the confirmed internal dead methods. Deprecation, telemetry observation, and deletion should be separate releases.

Changes specifically not to combine:

- Date/query compatibility, time-entry schema, Firebase secrets, and recurrence persistence are independent high-risk changes.
- Recurrence reopen preservation should land before the explicit-null representation migration.
- CI/test infrastructure should not be hidden inside a behavioral fix.
- Credential rotation should not share a commit/release with code that changes API behavior.
- Performance rewrites should not share a release with query-semantic fixes; otherwise result-set and latency regressions are difficult to distinguish.
- Endpoint deletion should never be bundled with contract renaming.

## Prevention and Codex guidance

### One primary mechanism per recurring pattern

| Recurring pattern | Findings | Primary prevention mechanism |
|---|---|---|
| Hand-maintained route/query/DTO drift across REST, Angular, Android, and MCP | BA-001, BA-007, BA-008, BA-010, BA-011 | Contract test that enumerates backend mappings and asserts each supported client serialization |
| Nullable fields and recurrence operations lack a complete state model | BA-004, BA-005, BA-009, BA-014 | Unit state-transition test suite generated from an explicit state table |
| Persistence/API/config contradictions and data migrations survive mocks | BA-002, BA-006, BA-015, BA-016, BA-017, BA-026, BA-030 | PostgreSQL integration test |
| Mutation completion behavior is transport-owned | BA-012 | Integration test that invokes every authenticated transport and observes the same after-commit event |
| Clean-release configuration/secrets are not exercised | BA-003, BA-018, BA-023, BA-028, BA-029 | CI rule that builds once and smoke-starts the clean artifact with explicit secrets/config |
| Sensitive fields can be logged by ordinary debug statements | BA-027 | Static-analysis logging rule |
| Calendar boundaries use ambient process time settings | BA-020 | Unit tests with a fixed Clock and configured ZoneId |
| Unbounded/N+1 reads lack objective limits | BA-021, BA-022 | Query-count integration test with representative cardinality |
| Privacy and contradictory product contracts need a human decision | BA-019, BA-024 | Human ADR |
| Generated repository skills conflict with the project's workflow | BA-025 | Repository-scoped Codex skill |

The table deliberately does not put formatter/linter rules into AGENTS.md. If the one-line planning-calendar files are normalized later, that is a formatter plus CI concern, not a human instruction.

### Assessment of existing AGENTS.md and skills

Strong and still useful:

- Root AGENTS clearly establishes OpenSpec as the observable-behavior contract, forbids invented behavior and direct default-branch pushes, and defines issue/PR label transitions.
- It distinguishes exploration input-needed from implementation/specification blocked.
- It requires tests, static checks, and OpenSpec validation before implementation tasks are marked complete.

Missing or too generic:

- “Relevant tests/static checks” at AGENTS.md:82-84 does not state the backend commands or require PostgreSQL/Flyway/API contract coverage (BA-006, BA-023).
- It has no cross-client contract rule despite three concrete client mismatches (BA-001, BA-002, BA-008).
- It does not say where cross-transport mutation side effects belong (BA-012), how null presence must be modeled (BA-005), or how clean-image secrets must be handled (BA-003, BA-018).
- It does not require reconciling contradictory canonical specs before implementation (BA-024).

Contradictory or obsolete:

- AGENTS.md:100 forbids OpenSpec artifact creation during exploration; openspec-explore/SKILL.md:15 permits it.
- AGENTS.md:120-129 requires complete tasks and sync before archive; openspec-archive-change/SKILL.md:39-70 permits archiving incomplete tasks and skipping sync.
- AGENTS.md:83-84 marks tasks complete after verification; openspec-apply-change/SKILL.md:71-78 marks each task complete immediately after implementation.
- Repository skills refer to AskUserQuestion, TodoWrite, Task subagents, and openspec-continue-change, which are not provided under those names by this repository/environment. These steps are impossible to follow literally here.
- The generic skills do not enforce the Taska-specific authoritative existing-PR branch and workflow-label rules. Repetition inside AGENTS is mostly deliberate safety reinforcement; the harmful redundancy is the second, divergent workflow in the skills.

### Proposed root AGENTS.md section: Backend Engineering Rules

Proposed text only; not applied:

~~~markdown
## Backend Engineering Rules

- Treat REST routes, query names, request/response fields, MCP schemas, and nullable-field semantics as cross-client contracts. For any change, inspect and update the backend, Angular, Android, MCP, and canonical OpenSpec representations together, and add an automated contract test. (BA-001, BA-002, BA-005, BA-007, BA-008, BA-010, BA-011)
- If canonical OpenSpec requirements conflict, stop implementation and obtain a product decision that reconciles the canonical specs. Passing OpenSpec validation does not resolve semantic conflicts. (BA-024)
- Model omitted, inherited, explicitly cleared, and assigned values as distinct states whenever the API gives them distinct meanings. Do not make one null value represent multiple states. (BA-004, BA-005, BA-009)
- Keep successful-mutation side effects in a transport-independent application boundary and run externally visible publication after commit. REST and MCP must exercise the same use case. (BA-012)
- Validate cross-field domain invariants before persistence and enforce them in PostgreSQL where possible. Expected validation, conflict, and constraint failures must return stable 4xx Problem Details, not generic 500 responses. (BA-002, BA-009, BA-015, BA-016, BA-017, BA-026)
- Persistence and migration changes require tests against PostgreSQL with Flyway; do not use an in-memory database as proof of PostgreSQL behavior. (BA-002, BA-006, BA-015, BA-016, BA-017)
- Credentials and private keys must not live under source/resources or enter container build contexts. Optional external integrations must have explicit enablement and clean-start behavior. (BA-003, BA-018, BA-019)
- Never log bearer tokens, Firebase tokens, credentials, or full secret values. (BA-027)
- Use the configured Taska calendar time zone and an injectable clock for calendar-day decisions. (BA-020)
- High-cardinality collection paths must have a documented bound or measured small-cardinality assumption. Review query count and indexes for every new repository access inside a loop. (BA-021, BA-022)
~~~

### Proposed root AGENTS.md section: Code Review Rules

Proposed text only; not applied:

~~~markdown
## Code Review Rules

- Trace every changed API operation end to end: security, binding/validation, controller or MCP adapter, transaction, domain invariants, persistence constraints, response/error mapping, side effects, and every supported client. (BA-001, BA-002, BA-008, BA-012, BA-016)
- For stateful behavior, review sequences rather than isolated methods. Require tests for valid and invalid transitions, retries, repeated requests, and explicit null values. (BA-004, BA-005, BA-009, BA-013, BA-014)
- Compare every entity change with all Flyway migrations and run it on PostgreSQL. Review uniqueness, foreign keys, indexes, existing-row cleanup, locking, and operational rollback/forward-fix strategy. (BA-002, BA-015, BA-017, BA-022)
- Verify that expected client mistakes and domain conflicts produce documented 4xx Problem Details without leaking internals. (BA-009, BA-016, BA-026)
- For external calls, review data disclosure, timeout/retry behavior, acknowledgement, idempotency, invalid-recipient cleanup, observability, and disabled/unavailable behavior. (BA-003, BA-013, BA-014, BA-019)
- For performance claims, require a concrete query/allocation path and identify the missing or collected measurement. Do not label a plausible risk as a measured defect. (BA-021, BA-022)
- Build and smoke-start the same clean artifact that will be released; do not rely on ignored local resources. (BA-003, BA-023, BA-029)
- Do not approve endpoint deletion from static reference counts alone; require access logs and an external-consumer check. (API matrix and dead-code candidates)
~~~

### Proposed nested taska-backend/AGENTS.md

A nested file is justified because Maven/PostgreSQL/Flyway and backend contract requirements do not apply to the frontend or Android modules. No AGENTS.override.md is justified: the backend does not need to override the root OpenSpec/GitHub workflow.

Proposed text only; not applied:

~~~markdown
# Taska backend instructions

This file supplements the repository-root AGENTS.md for taska-backend.

- The supported baseline is the Java version and Spring Boot parent declared in pom.xml. Do not recommend or use newer language/framework features without first changing that baseline through an approved change.
- The minimum local verification is mvn test and mvn verify. Run openspec validate --all --strict from the repository root for behavior changes.
- Any controller binding, entity, repository query, transaction, Flyway migration, security rule, scheduler, or external-integration change requires a focused integration test. Persistence/migration tests must use PostgreSQL and apply Flyway from an empty schema.
- API changes must include exact method, normalized path, path/query names, request/response nullability, status codes, Problem Details, and Angular/Android/MCP caller updates.
- Do not place credentials under src/main/resources. Tests and local profiles must explicitly disable unavailable integrations or supply test credentials outside the artifact.
- Preserve the documented mono-user shared-workspace authorization model unless an approved OpenSpec change introduces ownership or tenancy.
~~~

### Proposed repository-scoped backend audit skill

A reusable audit skill is justified. This audit required a repeatable multi-step workflow across one backend, two client projects, migrations, deployment, OpenSpec, framework-reflective dead-code rules, and a single-file write constraint. The requested aidd audit skill was unavailable, and the current repository skills cover only OpenSpec change workflows.

Suggested path: .codex/skills/backend-audit/SKILL.md

Proposed text only; not applied:

~~~markdown
---
name: backend-audit
description: Perform an evidence-based, read-only audit of the Taska backend and its Angular/Android/MCP API consumers, writing only the requested audit report.
---

# Taska backend audit

1. Read every active AGENTS file, canonical OpenSpec relevant to the backend, active changes, repository skills, build configuration, and official run/test commands. Report semantic instruction/spec conflicts separately from code findings.
2. Record the local commit and dirty files. Preserve user changes. Create no repository file except the requested report.
3. Establish Java, Maven, Spring Boot/Framework/Security/Data, Hibernate, Flyway, PostgreSQL, Spring AI, Jackson, Firebase, MapStruct, and recurrence-library versions from the resolved local build.
4. Inventory all controllers, MCP tools, schedulers, event listeners, entities, repositories, migrations, configuration properties, security boundaries, and external calls. Describe the observed architecture before judging it.
5. Enumerate backend endpoint mappings. Normalize Angular dynamic URLs and query construction plus Android Retrofit paths/queries/base URLs. Record direct MCP service calls separately. Produce one row per backend method/route.
6. Run only existing safe checks. Copy the backend to a disposable directory before Maven commands when the audit permits only the report to change. Do not install tools or download new analyzers. Record commands, outcomes, and environment-caused failures.
7. For persistence findings, compare request DTO, validation, service mutation, entity mapping, migration constraints/indexes, transaction behavior, and client payload. Do not infer PostgreSQL behavior from mocks or H2.
8. For security/privacy findings, trace the full request/object path and every external payload. Respect the documented shared-workspace model unless the approved specification changes it.
9. For dead code, search production, tests, all clients, docs/specs, configuration, and string-built routes. Account for Spring/JPA/Jackson/MapStruct/MCP reflection. Never call an endpoint unused without runtime access evidence.
10. Classify each item as Confirmed, High-confidence, or Possible/product-confirmation. Separate measured performance defects from plausible risks and state the missing measurement.
11. Write a severity-sorted finding table, complete API matrix, confirmed-versus-candidate dead code, dependency-aware remediation order, one primary prevention mechanism per recurring pattern, proposed AGENTS/skill text, rejected recommendations, and firm limitations.
12. Verify with git status and git diff --check that only the requested report was changed by the audit.
~~~

The existing OpenSpec implementation skills should be corrected rather than adding another implementation skill. Their workflow is already repeatable; the defect is divergence from Taska's root policy (BA-025).

## Rejected recommendations

- Do not split the backend into Maven modules or impose hexagonal/clean architecture now. The vertical feature packages are understandable at this scale; the evidenced need is a transport-independent mutation boundary, not a wholesale rewrite.
- Do not replace raw UUID persistence fields with JPA associations by default. The current choice avoids leaky serialized graphs and complex cascade/fetch behavior. Enforce the missing invariants directly (BA-015).
- Do not introduce per-user row ownership as a “security fix.” README explicitly defines one shared workspace for all valid tokens. Multi-tenancy requires product requirements, data migration, authorization policy, and client changes.
- Do not enable CSRF for this stateless bearer API or restrict CORS without an allowed-origin/product deployment inventory. The current wildcard does not allow credentials, and no concrete cross-origin exploit path was established.
- Do not add audience/scope rules from generic OAuth advice alone. The issuer is Taska-specific in the checked configuration; actual Authentik token claims and multi-application issuer behavior were not available.
- Do not upgrade Java, Spring Boot, Spring AI, Firebase, iCal4j, Jackson, or PostgreSQL merely because another version may exist. No current advisory/compatibility evidence was available.
- Do not remove Jackson 2 or Jackson 3 solely because both appear in the graph. Firebase/OpenAI integrations bring Jackson 2 while the Boot/Hibernate path uses Jackson 3; the custom mapper is an evidenced compatibility bridge.
- Do not add H2 to increase integration-test speed. It would fail to exercise PostgreSQL JSONB, timestamptz, gen_random_uuid, constraints, indexes, and Flyway behavior that matter here.
- Do not use a coverage-percentage target as the primary quality gate. The present problem is missing critical-path realism, not an unknown line percentage.
- Do not paginate every small reference list immediately. Projects, labels, filters, and calendars may remain acceptably small in a mono-user workspace; first bound tasks/comments/time entries and measure BA-021.
- Do not add optimistic locking to every entity without a concurrent-edit requirement. Full PUT can overwrite a stale representation, but current mono-user evidence is insufficient to justify a cross-client versioning migration. Revisit if telemetry or product requirements establish concurrent editors.
- Do not delete any REST endpoint listed as a candidate from static absence alone. Access logs, released clients, MCP/external tooling, and a deprecation period are required.
- Do not combine automatic formatting of the one-line planning-calendar classes with risk fixes. Readability is poor, but formatting is mechanically enforceable and should be a separate formatter/CI change, not an audit finding or AGENTS rule.
- Do not redesign reminder delivery into a durable outbox unless delivery requirements justify it. BA-013 can first be corrected by acknowledging Futures and modeling retry state; an outbox is the next step only if crash-safe delivery is required.

## Final assessment

The backend is not an incoherent or generally unsafe codebase. Its central architecture is suitable for a small shared-workspace application, its recurrence implementation has meaningful unit coverage, its JWT boundary and generic error leakage behavior are sensible, and its migrations show awareness of timezone/data-repair concerns.

It is not currently release-assured. The strongest evidence is that core Android calls disagree with controller/database contracts while all 111 tests pass, and that a clean release image has no checked-in way to satisfy default-enabled Firebase startup. Those are failures of contract ownership and production-shaped verification more than failures of Java syntax or Spring convention.

The firm conclusions are BA-001, BA-002, BA-004, BA-005, BA-007 through BA-014, BA-016, BA-018, BA-020, and BA-023 through BA-030: their behavior is established by code or executed configuration. BA-003, BA-015, BA-017, BA-021, and BA-022 have high confidence but need a production-shaped environment, data, or measurement to quantify occurrence and impact. BA-019 is an established data flow whose acceptability requires a product/privacy decision. Endpoint non-use cannot be concluded without runtime access evidence.
