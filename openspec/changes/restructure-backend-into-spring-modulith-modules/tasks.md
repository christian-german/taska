## 1. Module structure

- [x] 1.1 Add the Spring Modulith BOM, `spring-modulith-starter-core` and `spring-modulith-starter-test`.
- [x] 1.2 Move every type from `com.taska.domain.<feature>` to `com.taska.<module>` under the `model` / `persistence` / `application` / `adapter` layer layout, with sub-domains and transports at the third level.
- [x] 1.3 Fold `task/series` into `task/occurrence` and split `task/recurrence` out of it.
- [x] 1.5 Push the task sub-domains and every module's services under `application`, so the second level carries only the layer axis, and fold `planningcalendar/validation` into `model`.
- [x] 1.4 Declare `@ApplicationModule` on every module and `@NamedInterface` on every published package.
- [x] 1.6 Group `config`, `exception`, `mcp` and `security` into one `platform` module whose concerns are named interfaces, and fix the class name referenced from `application.properties`.

## 2. Acyclic module graph

- [x] 2.1 Replace the task module's calls into notification with `TaskChangedEvent` and `TaskOccurrenceRescheduledEvent`.
- [x] 2.2 Replace the task module's priority-evaluation purge with `TaskMutatedEvent`.
- [x] 2.3 Move `GET /tasks/{taskId}/priority-evaluation` to the priority module, unchanged.
- [x] 2.4 Move `GET /projects/{projectId}/tasks` to the task module, unchanged.
- [x] 2.5 Move the planning-calendar compatibility check to the task module behind `ProjectPlanningCalendarChangeRequested`.
- [x] 2.6 Replace the task module's use of `ProjectRepository` and the project module's use of `PlanningCalendarRepository` with service calls.
- [x] 2.7 Extract the `PriorityAssessmentClient` port out of the OpenAI adapter.

## 3. Verification

- [x] 3.1 Add `ModularityTest` running `ApplicationModules.verify()`.
- [x] 3.2 Rewrite `BackendArchitectureTest` with ArchUnit for the in-module invariants module verification does not reach, and confirm each rule fails on an injected violation.
- [x] 3.6 Evaluate nested application modules for the task sub-domains; record why they do not apply.
- [x] 3.3 Move tests to mirror the module layout and rewire the collaborators that became events.
- [x] 3.4 Run the full backend suite and the formatter.
- [x] 3.5 Update `SKILL-spring.md` to describe the layout that now exists.
