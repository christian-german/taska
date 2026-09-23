---
name: spring-development
description: Apply reusable Spring Framework and Spring Boot standards when implementing, modifying, refactoring, or reviewing Spring components, configuration, persistence, security, or framework tests.
  For Java code, also use java-development. Do not activate solely because an unrelated module in the repository uses Spring.
---

# Spring development

Apply this framework-specific layer to the Spring behavior affected by the task.
For Java source or tests, also load the installed `java-development` skill from this plugin using its name in the current skill catalog.
If it is unavailable, disclose that and continue with the available project instructions; do not invent its content.
Naming, language design, general error handling, and general test discipline belong to the Java skill and are not restated here.

## Match the project's Spring setup

Prefer the project's Boot dependency management for managed libraries.
Override an individual managed version only for a concrete compatibility or security requirement, with the reason recorded.

## Dependency injection and application boundaries

Use constructor injection for required dependencies in application components. Keep those dependencies final where the class model permits it.
Use Lombok only for constructor arguments. Use the @RequiredArgsConstructor for this purpose. Don't use Lombok for anything else.

## REST API naming conventions

Spring REST endpoints must preserve the naming defined by the OpenAPI contract unless it is mentioned to adapt the specification accordingly.

For APIs under our control:
- `@PathVariable`, `@RequestParam`, `@RequestBody` names use camelCase.
```java
    @PutMapping("/{taskId}")
    public ProjectDto update(@PathVariable UUID taskId, @RequestParam(name = "projectId") UUID projectId, @RequestBody ProjectRequest projectRequest) {
        // ...
    }
```
- JSON request and response properties use camelCase.
- Static URL path segments containing multiple words use kebab-case, for example, /following-occurrences.

Do not introduce Jackson naming overrides or aliases unless required by an external contract.

## Method naming conventions

Controller and service method names follow one fixed vocabulary. Do not introduce synonyms (`get`, `find`, `fetch`, `retrieve`, `save`, `add`, `remove`, `edit`, `patch`) for the operations below.

**Controllers** — the HTTP-facing name, one per route:
- `getById` — `GET /{id}`
- `getAll` — `GET`
- `create` — `POST`
- `update` — `PUT /{id}`, full replacement
- `delete` — `DELETE /{id}`

**Services** — transport-independent, aligned with the `findBy…`/`findAll` idiom Spring Data already imposes on repositories:
- `findById` — returns the entity, or throws `ResourceNotFoundException`
- `findAll` — returns the collection, including a filtered variant of "all" (`findAll(UUID taskId)`, `findByProject`)
- `create` — persists a new entity
- `replace` — full replacement of every mutable field; the service-layer counterpart of a controller `update`
- `update` — partial update (PATCH semantics): only the fields the caller explicitly supplied change. Reserve this name for genuine partial updates — a method that replaces every field is `replace`, never `update`
- `delete` — permanently removes the entity

A controller method keeps the name `update` for its `PUT` endpoint even when it delegates to a service `replace` method: the controller name describes the HTTP contract, the service name describes what the method does to the entity.

**Existence checks**:
- `exists` returns a `boolean`.
- `requireExists` throws `ResourceNotFoundException` when absent, for a caller that only needs to assert a related resource is present before proceeding.

## API Contracts and Mutation Rules

### Resource contracts

- Define separate contracts for each supported operation:
    - `XxxDto` for resource responses.
    - `XxxCreateRequest` for creation.
    - `XxxUpdateRequest` for full replacement.
- Do not create request types for operations the API does not expose.
- Keep creation and replacement contracts separate even when their properties are currently identical.
- Creation requests contain only properties accepted at creation.
- Replacement requests contain every property explicitly covered by the replacement contract. “Full replacement” refers to these properties, not to the entire persisted entity.
- Require every replacement property to be present in the JSON payload. Allow `null` only when it has an explicit business meaning.
- Reject missing replacement properties, including those that accept `null`. Do not silently convert an omitted property into an explicit null value.
- Apply nullable replacement values explicitly. `null` must never mean “leave unchanged.”
- An empty array clears the corresponding collection. Do not interpret it as an omitted modification.
- Reject unknown JSON properties. See *Cross-cutting contract enforcement* for where that rejection is implemented.
- Exclude server-controlled properties from writable contracts, including the target resource identifier, generated timestamps, computed values, and internal technical state.
- Related resource identifiers may be writable when changing the relationship is supported.
- Do not introduce generic partial-update objects or infer update behavior from non-null properties.

### Cross-cutting contract enforcement

A rule that applies to every resource contract MUST be enforced once, by a single application-wide mechanism.
Never restate it on individual request types, DTOs, or controllers: a rule implemented per class is a rule that is applied to the classes someone remembered.

- Reject unknown JSON properties through the application's Jackson configuration (`FAIL_ON_UNKNOWN_PROPERTIES`).
  Do not add a `@JsonAnySetter` method that throws, a `@JsonIgnoreProperties(ignoreUnknown = false)` annotation, or any equivalent per-record guard.
- Translate framework and persistence exceptions that any feature can raise, such as `DataIntegrityViolationException`, in the application-wide `@RestControllerAdvice`.
  Reserve a feature-scoped `@RestControllerAdvice(assignableTypes = ...)` for a translation that is genuinely specific to that feature and would be wrong elsewhere.
- Before implementing such a rule on a class, check whether a global mechanism already covers it. If it does, add nothing.
- When you introduce the global mechanism, remove the per-class implementations it replaces in the same change, and cover the rule with a single test rather than one test per resource.

### Full replacement and dedicated operations

- Use full replacement by default for general resource editing.
- Introduce a dedicated operation when the functional specification identifies an intent distinct from general editing, such as a business transition, an operation with a distinct target or scope, or coordinated changes across multiple resources.
- Reducing payload size, or requiring validation or side effects does not independently justify a dedicated operation.
- During implementation, use the dedicated operations defined by the specification. If another operation appears necessary, raise it as a design decision instead of introducing it silently.
- Dedicated operations accept only the parameters required for their execution.
- Create a dedicated request type when an operation requires a structured request body. Do not create empty request objects for operations that require no body.
- Properties controlled exclusively through dedicated operations must be absent from the full-replacement contract.

### Shared business behavior

- Give each business invariant, transition rule, and business side effect a single authoritative implementation, reused by every applicable mutation path.
- When both full replacement and a dedicated operation can modify the same property, route both through the same business behavior, including its rules and applicable effects.
- Do not duplicate business rules across controllers, transport adapters, or separate mutation paths.
- Do not use generic property copying to bypass business behavior or overwrite server-controlled state.
- Keep API DTOs out of the service layer. Map transport-specific inputs to the service contract at the application boundary.
- Apply the same business guarantees across HTTP APIs, tool integrations, scheduled jobs, imports, and batch processing.
- Verify shared behavior through tests that exercise overlapping mutation paths, including rejection of invalid changes and preservation of server-controlled properties.

## API boundary mapping

Each feature owns one mapper per transport adapter, and that mapper is the only place where transport types and application types meet.

- Every conversion between a transport type (`XxxDto`, `XxxCreateRequest`, `XxxUpdateRequest`) and an application type (entity, `XxxParameters`, application result) MUST go through the feature's `XxxMapper`, declared in the adapter package.
- `XxxMapper` MUST be a MapStruct interface. There is no alternative idiom: no handwritten `@Component` mapper, no static `from(...)` factory on a DTO or a record, no `toXxx()` instance method on a transport type, and no mapper that exists only to delegate to one of those.
  Transport types are data carriers; the mapper owns the translation.
- All mappers MUST share one `@MapperConfig` setting `componentModel = SPRING` and `unmappedTargetPolicy = ReportingPolicy.ERROR`, referenced with `@Mapper(config = ...)`.
  Failing the build on an unmapped target is the reason to impose MapStruct: a property added to a DTO or to a `XxxParameters` then breaks compilation instead of silently serializing `null`.
  A target deliberately left unmapped is declared with `@Mapping(target = "...", ignore = true)`, which records the decision instead of hiding it.
- Prefer a declarative mapping wherever MapStruct can express it: renames, defaults, nested records, collections of records, `null`-to-empty through `NullValueMappingStrategy.RETURN_DEFAULT`.
  Hand-writing what the processor can generate is the error to avoid.
- A `default` method inside the mapper is legitimate and is the right tool where MapStruct has no declarative equivalent: several sources combined into one target, or a value resolved by falling back from an override to a base value.
  Do not contort such a mapping into `expression = "java(...)"`. An expression string is not checked by the IDE, not refactorable, and not debuggable; a `default` method in the same interface is clearer and keeps the logic inside the mapper.
- Adding a conversion to a feature means adding a method to its existing mapper. Never introduce a second mapping type for the same feature.

```java
// Avoid: the annotation generates nothing, and the DTO knows how to build itself.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabelMapper {
  default LabelDto toDto(Label label) {
    return LabelDto.from(label);
  }
}

// Prefer: the mapping is declared, the processor generates it, an unmapped target fails the build.
@Mapper(config = ApiMapperConfig.class)
public interface LabelMapper {
  @Mapping(target = "order", source = "position")
  LabelDto toDto(Label label);
}

// Also correct: MapStruct has no way to coalesce an override against a base value,
// so the interface keeps a default method rather than an `expression` string.
@Mapper(config = ApiMapperConfig.class)
public interface TaskMapper {
  @Mapping(target = "order", source = "position")
  @Mapping(target = "instanceId", ignore = true)
  TaskDto toDto(Task task);

  default TaskDto toOccurrenceDto(Task task, TaskInstance instance, Instant occurrenceScheduledAt) {
    // Several sources, per-property fallback: no declarative equivalent.
  }
}
```

## Package organization

Application code is organised into Spring Modulith application modules, all rooted directly under the application package: one per feature, plus exactly one `platform` module holding the application's own base layer.

**Each level of nesting uses one axis only.** Getting this wrong is the common failure: a module's direct subpackages must not be a mix of technical layers and functional areas.

- **Level 0** — the application package. It holds the application class, the feature modules, and `platform`. Nothing else: a technical concern sitting beside the features is the same axis mix as a functional package sitting beside the layers, one level up.
- **Level 1** — the module.
- **Level 2** — the layer, always these four, and nothing else:
  - `model` — entities, enumerations, value objects, application results, `XxxParameters`, published events, and the invariants over those types, including constraint annotations and their validators. It MUST NOT depend on any other package of its own module; that is what keeps a module's internal graph a tree rather than a cycle between services and the types they exchange.
  - `persistence` — repository interfaces and persistence access implementations.
  - `application` — the application services, and any port they declare for an outbound call.
  - `adapter` — everything entering or leaving the application.
- **Level 3** — one axis per parent, and only where there is more than one value:
  - under `application`, the functional sub-domain. Add one only when a module has enough services to group; do not create a package for a single class.
  - under `adapter`, the transport: `http`, `mcp`, `scheduler`, `events`, and outbound clients such as `firebase` or `openai`.

The module root holds only `package-info.java`. What another module may use is designated by `@NamedInterface`, not by sitting at the root.

Rules:

- Do not create a package named `controller`, `service`, or `repository`. That is the technical axis, and it is already expressed by `adapter` / `application` / `persistence`.
- Do not create a functional package at level 2. A functional area is a subdivision of the application layer, never a sibling of the layers.
- Do not create application-wide packages that group unrelated features.
- Nothing outside `adapter` may depend on an `adapter` package — including a module's own outbound adapter. A service that needs an outbound call declares a port in `application` and lets the adapter implement it.
- `model` and `persistence` MUST NOT depend on `application` or `adapter`.
- Keep feature-specific classes inside their module. Do not move them into generic `common`, `util`, or `shared` packages merely because they have similar technical roles.
- Application-wide infrastructure — configuration, the shared error vocabulary, shared transport helpers, security — belongs to the `platform` module. It is the one module divided by **concern** rather than by layer, because it has no domain and no use cases, so there are no layers to separate. Each concern is a `@NamedInterface`, so a feature declares the parts of the base layer it actually uses.
- Nothing in `platform` may depend on a feature module. It is the bottom of the dependency graph.

Example structure:

```text
com.example.application
├── Application                     @SpringBootApplication @Modulithic
├── platform                        the base layer: one module, divided by concern
│   ├── package-info                @ApplicationModule
│   ├── config                      @NamedInterface("config")
│   ├── exception                   @NamedInterface("exception")
│   └── security                    @NamedInterface("security")
├── label                           a small module: no functional sub-domain
│   ├── package-info                @ApplicationModule(allowedDependencies = …)
│   ├── model
│   │   ├── Label
│   │   ├── LabelCreateParameters
│   │   └── LabelUpdateParameters
│   ├── persistence
│   │   └── LabelRepository
│   ├── application
│   │   └── LabelService
│   └── adapter
│       └── http
│           ├── LabelController, LabelDto, LabelCreateRequest,
│           └── LabelUpdateRequest, LabelMapper
└── task                            a large module: same four layers, one of them subdivided
    ├── package-info
    ├── model                       @NamedInterface — the published vocabulary
    ├── persistence                 @NamedInterface — queried in bulk by two modules
    ├── application
    │   ├── TaskMutationService     the module's boundary
    │   ├── definition              TaskDefinitionService, TaskDefinitionRules
    │   ├── occurrence              TaskOccurrenceService, RecurringTaskSeriesService
    │   └── recurrence              @NamedInterface — RRULE expansion
    └── adapter
        ├── http
        ├── mcp
        └── events
```

## Module boundaries

Each module declares what it may use, and what others may use of it. `ApplicationModules.verify()` enforces both; it is the architecture test, not a hand-written path check. Rules *inside* a module — which layer a type belongs to, the direction between sub-domains — are out of its reach and are written with ArchUnit, which the Modulith test starter already brings in. Neither is ever expressed by matching file paths or import strings.

Nested application modules (`@ApplicationModule` on a sub-package) are the framework's answer for governing a module's internals, but they only fit a sub-part the parent calls into one way. A sub-domain that reads its parent's `model` while the parent's adapters call it forms a cycle that verification rejects, whatever the documented parent-access allowance. Reach for nesting only when the sub-part owns its own types and persistence.

Nesting is also easy to trigger by accident. Grouping several modules under one package makes that package a module, and its children become *nested* modules — unreachable from the rest of the application, which then fails with `Invalid sub-module reference`. To group modules without nesting them, make the group a single module and publish each part as a `@NamedInterface`.

A class named in `application.properties` is not covered by any of this. Renaming or moving such a class is a silent break until something boots — keep an integration test that starts the real persistence configuration.

- Every module carries a `package-info.java` with `@ApplicationModule(allowedDependencies = …)` naming each module or named interface it depends on.
- A package other modules are allowed to use carries `@NamedInterface`. Everything else is internal. Expose the smallest surface that works, and say in the package's Javadoc why it is exposed.
- **The module graph MUST be acyclic.** Two modules must never depend on each other.
- When a module must trigger behaviour in a module that already reads it, it publishes a domain event from its own `model` package instead of calling into it. The reader listens; the writer stays unaware.
  - Use a plain `@EventListener` to keep the previous semantics of a direct call: synchronous, inside the publishing transaction, and able to fail the whole operation.
  - Use `@TransactionalEventListener` only when the effect must survive independently of the publishing transaction, and say so.
- An endpoint belongs to the module that owns what it returns, not to the module its URL names. `GET /projects/{projectId}/tasks` returns tasks, so the task module serves it. Relocating an endpoint MUST NOT change its path, payload, or status codes.

## Transport adapters beyond HTTP

An entry point that is not an HTTP controller — MCP tools, scheduled jobs, message listeners, imports, batch runners — is another transport adapter of the feature it exposes, and follows the same rules as `controller`.

- Place the adapter in `adapter/<transport>` inside the module it exposes, named after the transport. Do not create an application-wide package outside the module tree to hold it.
- The adapter calls the same application services with the same `XxxParameters`, and never reaches into a repository.
- Response types are shared by default. An adapter SHOULD depend on the module's `adapter/http` package to reuse its `XxxDto` and the mapper method that produces it, rather than declaring a parallel output record.
  One projection means one place to change, and the mapper's `unmappedTargetPolicy = ERROR` then covers every transport at once.
  Accept the consequence deliberately: a change to the shared `XxxDto` changes every transport exposing it. That is the point. An adapter that must evolve its response independently declares its own record — and still derives it from the shared mapper.
- The dependency is one-directional: `adapter/http` MUST NOT depend on another adapter's package, and two non-HTTP adapters MUST NOT depend on each other.
- Input types stay per-adapter. Transport's input carries that transport's annotations and semantics — Jakarta constraints and full-replacement rules for HTTP, tool-parameter descriptions and partial-patch rules for MCP — and forcing one record to serve both breaks one of the two contracts.
- Whatever is not shared, the logic deriving exposed values from an application result MUST have a single implementation. Hand-copying a projection from one adapter into another is the failure mode to avoid: the copies drift silently, and the drift surfaces as a field that exists on one transport and not the other.
- When a property is added to an entity, an application result, or a `XxxParameters`, update every adapter of that feature in the same change, or state explicitly why a transport does not expose it.

```text
task
├── model                       Task, TaskResult, TaskCreateParameters, TaskChangedEvent
├── persistence                 TaskRepository
├── application                 TaskMutationService, definition/, occurrence/, recurrence/
└── adapter
    ├── http                    HTTP adapter, and owner of the shared response contract:
    │                           TaskController, TaskDto, TaskCreateRequest, TaskMapper
    ├── mcp                     MCP adapter: TaskMcpTools, TaskCreateInput, TaskUpdateInput.
    │                           Depends on adapter/http for TaskDto and TaskMapper.
    │                           Never the reverse.
    └── events                  listeners on other modules' published events
```

## Validation at Application Boundaries

Validate data independently at each architectural boundary. Do not rely on validation performed by an upstream caller.

### Controller requests

HTTP request objects (`XxxCreateRequest`, `XxxUpdateRequest`, etc.) MUST declare Jakarta Validation constraints describing the API contract.

Controllers MUST trigger validation using `@Valid`.

```java
public record LabelCreateRequest(
    @NotBlank
    @Size(max = 100)
    String name
) {}
```

```java
@PostMapping
public LabelDto create(@Valid @RequestBody LabelCreateRequest request) {
    return labelMapper.toDto(
        labelService.create(labelMapper.toParameters(request))
    );
}
```

### Service parameters

Parameter objects passed to services (`XxxParameters`) MUST independently declare the constraints required by the service contract.

A service MUST NOT assume that its parameters have already been validated by a controller or another caller.

```java
public record CreateLabelParameters(
    @NotBlank
    @Size(max = 100)
    String name
) {}
```

Service parameter validation SHOULD use Jakarta Validation rather than duplicating equivalent validation manually.

```java
@Service
@Validated
public class LabelService {

    public Label create(@Valid CreateLabelParameters parameters) {
        // Business logic
    }
}
```

This guarantees the same service contract when the service is invoked from HTTP controllers, batch processing, tests, or any other entry point.

Identical constraints MAY appear on both a request and its corresponding service parameters. This is intentional: they belong to two independent contracts and MUST NOT depend on each other remaining identical.

### Business rules

Jakarta Validation SHOULD be used for constraints that can be evaluated from the object itself, such as nullability, string length, format, ranges, or cross-field consistency.

Rules requiring application state or dependencies MUST be enforced by the service or domain logic instead.

For example, checking that a label name is non-blank belongs to validation constraints. Checking that no label with the same name already exists requires repository state and belongs to business logic.

### Principle

Each boundary owns and validates its contract:

`HTTP Request → Controller → Service Parameters → Service → Domain`

Validation at one boundary MUST NOT be considered a substitute for validation at another boundary.

## Transaction Management

- Service classes that access persistent data MUST be annotated with `@Transactional(readOnly = true)` at class level.
- Read operations MUST rely on the class-level read-only transaction and MUST NOT repeat `@Transactional(readOnly = true)` at method level.
- Any service method that creates, updates, or deletes persistent data MUST explicitly override the class-level default with `@Transactional`.
- Transaction boundaries MUST be defined at the service layer, not in controllers or repositories.
- A method MUST NOT use a writable transaction unless it performs or coordinates a persistent state change.

Example:

```java
@Service
@Transactional(readOnly = true)
public class PlanningCalendarService {

    public List<PlanningCalendarDetails> all() {
        // Read-only transaction inherited from the class.
    }

    @Transactional
    public PlanningCalendarDetails create(
            PlanningCalendarCreateParameters planningCalendarCreateParameters) {
        // Explicit writable transaction.
    }
}
```

This convention makes read-only access the default and requires database writes to be explicitly visible at method level.
