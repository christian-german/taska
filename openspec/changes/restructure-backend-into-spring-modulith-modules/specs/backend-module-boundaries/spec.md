## ADDED Requirements

### Requirement: Backend features are verified application modules

Each backend feature SHALL be a Spring Modulith application module rooted at `com.taska.<module>`, declaring in its `package-info.java` the modules and named interfaces it is allowed to use. An automated test SHALL run `ApplicationModules.verify()` over the application.

The application package SHALL contain the application class, the feature modules, and one `platform` module holding the application's own base layer, and nothing else. `platform` SHALL be divided by concern, each concern published as a `@NamedInterface`, and SHALL NOT depend on any feature module.

#### Scenario: Architecture verification inspects the application package
- **WHEN** automated architecture checks inspect backend production classes
- **THEN** every class SHALL reside in a feature module, in `platform`, or directly in the application package

#### Scenario: A module reaches a dependency it did not declare
- **WHEN** a type in one module references a type in another module that its `@ApplicationModule` does not list
- **THEN** module verification SHALL fail

#### Scenario: A module reaches another module's internals
- **WHEN** a type references a package of another module that is not the module root and carries no `@NamedInterface`
- **THEN** module verification SHALL fail

### Requirement: The module dependency graph is acyclic

No two backend modules SHALL depend on each other. A module that must trigger behaviour in a module that already reads it SHALL publish a domain event from its own `model` package instead of calling into it.

#### Scenario: Module verification inspects the dependency graph
- **WHEN** automated module verification runs
- **THEN** it SHALL report no cycle between modules

#### Scenario: A task mutation reaches notification and priority
- **WHEN** a task mutation completes
- **THEN** the task module SHALL publish `TaskChangedEvent` and `TaskMutatedEvent` rather than calling the notification or priority modules
- **AND** their listeners SHALL run synchronously inside the publishing transaction

#### Scenario: A project moves to another planning calendar
- **WHEN** the project module applies a planning-calendar change
- **THEN** it SHALL publish `ProjectPlanningCalendarChangeRequested` before applying it
- **AND** the change SHALL be rejected when an open scheduled task of that project falls outside the target calendar

### Requirement: A module's internal layout uses one axis per level

A feature module's second level SHALL be the layer axis and nothing else: `model` for entities, enumerations, value objects, application results, service parameters, published events and the invariants over those types; `persistence` for repositories; `application` for application services and the ports they declare; `adapter` for everything entering or leaving. A feature module root SHALL hold only `package-info.java`.

A module's third level SHALL be the functional sub-domain under `application` and the transport under `adapter`, and SHALL be introduced only where there is more than one value. A functional area SHALL NOT appear at the second level. A module SHALL NOT declare a package named `controller`, `service` or `repository`.

The `platform` module is the exception: having no domain and no use cases, it SHALL be divided by concern rather than by layer.

#### Scenario: Architecture verification inspects a module's layout
- **WHEN** automated architecture checks inspect backend production classes
- **THEN** every second-level package of a feature module SHALL be `model`, `persistence`, `application` or `adapter`
- **AND** no type SHALL reside directly in a feature module root
- **AND** every JPA entity SHALL reside in a `model` package
- **AND** every repository SHALL reside in a `persistence` package
- **AND** every transport type SHALL reside under an `adapter` package
- **AND** no type outside an `adapter` package SHALL reference an `adapter` package

### Requirement: A module publishes an explicit surface

Types other modules are allowed to use SHALL reside in a package annotated `@NamedInterface`. The task module SHALL publish `model`, `persistence` and `recurrence`; the project module SHALL publish `model` and `application`; the planning calendar module SHALL publish `application`. Every other package SHALL remain internal.

#### Scenario: A module consumes a published surface
- **WHEN** the notification or priority module reads task entities, repositories or recurrence expansion
- **THEN** it SHALL do so through a named interface listed in its own allowed dependencies

### Requirement: Transport behavior is unaffected by the module layout

Relocating an endpoint to the module that owns what it returns SHALL NOT change its HTTP method, path, request payload, response payload or status codes.

#### Scenario: A relocated endpoint is called
- **WHEN** a client calls `GET /projects/{projectId}/tasks` or `GET /tasks/{taskId}/priority-evaluation`
- **THEN** the response SHALL be identical to the response produced before the relocation
