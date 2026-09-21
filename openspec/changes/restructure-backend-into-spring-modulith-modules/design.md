## Context

The user asked for a full restructuring onto Spring Modulith conventions, choosing explicitly: break cycles with events and endpoint relocation, expose module surfaces through `@NamedInterface` on a `model` package, and add `spring-modulith-starter-core` plus `spring-modulith-starter-test`.

## Goals / Non-Goals

Goals: one axis per package level, an acyclic and verified module graph, a declared public surface per module, and identical observable behaviour.

Non-goals: change any HTTP or MCP contract, change the database, alter recurrence semantics, or split `TaskOccurrenceService`.

## Decisions

### The application package

The application package holds the application class, the eight feature modules, and `platform`. The four infrastructure packages that previously sat there — `config`, `exception`, `mcp`, `security` — were the same axis mix as a functional package sitting beside the layers, one level up: sorted alphabetically they interleaved with the features.

`platform` is one module divided by concern, each concern a `@NamedInterface`, so a feature declares `platform :: exception` rather than the whole base layer. Two alternatives were tried and rejected. Collapsing the four into one flat package produces a bag of eight unrelated classes — security, JSON, MapStruct, Firebase, errors — which is a `util` package by another name. Keeping them as four modules under a `platform` grouping package, via `@Modulithic(additionalPackages)`, makes `platform` itself a module and therefore turns its children into *nested* modules: verification then rejects every feature that reaches them with `Invalid sub-module reference`. The named-interface form keeps the four concerns separate and addressable while leaving one entry in the application package.

### Modules and their layout

Each level of nesting carries one axis. A feature module's second level is the layer, always the same four:

- `model` — entities, enumerations, value objects, application results, service parameters, published events, and the invariants over those types. Depends on no other package of its own module, which is what keeps the module's internal graph a tree instead of a cycle between services and the types they exchange.
- `persistence` — Spring Data repositories.
- `application` — application services and the ports they declare.
- `adapter/<transport>` — `http`, `mcp`, `scheduler`, `firebase`, `openai`, `events`. Anything driven from outside or reaching outside.

The third level is the functional sub-domain under `application` and the transport under `adapter`, introduced only where there is more than one value. Only `task` earns sub-domains: `definition`, `occurrence`, `recurrence`. They subdivide one layer; they are not siblings of the layers, which is what a first attempt got wrong by leaving them at the second level beside `model`, `persistence` and `adapter` — a level carrying two axes at once.

The module root holds only `package-info.java`. What another module may use is designated by `@NamedInterface`, never by sitting at the root. The four application-wide infrastructure modules have a single concern each and stay flat.

`series` disappears as a package: stopping a series is occurrence work, and one class does not need two levels of nesting.

### Breaking the cycles

**task → notification** and **task → priority** become events published by the task module: `TaskChangedEvent`, `TaskOccurrenceRescheduledEvent`, `TaskMutatedEvent`. Listeners are plain `@EventListener`, therefore synchronous and inside the publishing transaction, which preserves the previous ordering exactly — in particular a priority evaluation is purged before a task deletion reaches its foreign key.

**project → task** goes away by moving `GET /projects/{projectId}/tasks` into the task module, which owns the projection it returns, and by moving the planning-calendar compatibility check there too. The project module announces `ProjectPlanningCalendarChangeRequested` before applying the change; the task module vetoes it by throwing, since "a scheduled task must fit its project's planning calendar" is a task invariant already enforced on every task write.

**task → priority** also required moving `GET /tasks/{taskId}/priority-evaluation` to the priority module. Both relocations keep the URL and the response identical; only the owning class changes.

### Published surfaces

`task` exposes three named interfaces because three different things are legitimately shared: `model` (its vocabulary), `persistence` (the notification sweep and the priority batch query tasks in bulk on their own schedule) and `recurrence` (RRULE expansion, so the notification sweep expands occurrences the same way the calendar does). `project` exposes `model` for its event and `application` for the queries the task module needs instead of a repository; `planningcalendar` exposes `application`. Every other package of every module is internal.

### Priority assessment port

`TaskPriorityEvaluationService` depended on `OpenAiPriorityAssessmentClient` and on the request/response records beside it — a service depending on its own outbound adapter. A `PriorityAssessmentClient` port now sits in the application layer, the OpenAI client implements it, and the exchange records move to `model` as `PriorityAssessmentBatch` and `PriorityAssessmentBatchResult`. The rename drops the `Request`/`Response` suffixes, which the layout reserves for transport types.

## Considered and rejected: nested application modules

Spring Modulith 1.3 added nested application modules: annotate a sub-package with `@ApplicationModule` and its contents become reachable only from the parent module or from types sibling nested modules expose, while the nested module may read anything in its parent. That is the obvious candidate for `task`'s three sub-domains, and it would replace hand-written direction rules with framework verification.

It does not apply here. Declaring `task.application.definition` as a nested module makes verification fail with `Cycle detected: Slice task -> Slice task.application.definition -> Slice task`. The parent depends on the nested module because the adapters and `TaskMutationService` call `TaskDefinitionService`; the nested module depends on the parent because it reaches `task.model` 72 times and `task.persistence` 6 times. The documented allowance for a nested module to read its parent does not exempt the pair from cycle detection. Removing the nested module's `allowedDependencies` changes nothing: the cycle is structural.

Nesting assumes a nested module is a self-contained part the parent calls into one way. These sub-domains are not that: they sit between the parent's adapters and the parent's model, and they share one deliberately common vocabulary — the sealed `TaskResult` hierarchy and the `*Parameters` records exist precisely so that definition, occurrence and the adapters speak the same language. Making nesting work would mean each sub-domain owning its own entities, repositories and result types, with the adapters reassembling them. That is a different and much larger design, and it trades away the single resolution point for occurrence overrides.

The direction rules therefore stay in the architecture test, but they moved from matching import strings to ArchUnit, which is already on the test classpath through `spring-modulith-core`. Bytecode analysis sees actual references rather than text, so a star import or a package name mentioned in a comment no longer produces a wrong answer, and a rename cannot silently disable a rule.

## Risks / Trade-offs

- Synchronous listeners make a failing listener roll back the publisher's transaction. That is the previous behaviour of the direct calls, deliberately preserved; asynchronous delivery would have changed it.
- A veto expressed as an event is less obvious than a method call. The listener's name and Javadoc state the invariant and why it lives in the task module.
- Three named interfaces on `task` is more surface than one. Collapsing them would mean either putting repositories in a package called `model` or rewriting the notification sweep around a read model, which the user scoped out.

## Verification

`ApplicationModules.verify()` fails on a cycle, an undeclared dependency, or a reference to another module's internals. `BackendArchitectureTest` keeps only what module verification cannot reach: where a type belongs inside its own module, the task sub-domain dependency direction, and two Spring conventions.
