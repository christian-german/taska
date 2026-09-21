## Why

Backend packages were organised by feature and then by technical layer, with both axes appearing as siblings inside a feature. The task feature had outgrown it: `controller`, `mcp` and `service` sat next to the functional sub-domains `definition`, `occurrence` and `series`, `task/service` held both the orchestrator and the vocabulary every sub-domain exchanged, and `series/service` existed for a single 33-line class.

Feature boundaries were also unenforced. Three pairs of features depended on each other in both directions — task/notification, task/priority and task/project — so no feature could be reasoned about, tested or extracted on its own, and the hand-written architecture test could only check where a file sat, never who was allowed to reach it.

## What Changes

- Adopt Spring Modulith. Every feature becomes a verified application module declaring the modules it may use; `ApplicationModules.verify()` replaces path matching as the boundary check.
- Flatten `com.taska.domain.<feature>` to `com.taska.<feature>`: every package was a domain package, so the segment carried no information. Group the application's own base layer into a single `platform` module, so the application package lists the features and nothing else.
- Give every module one axis per level: the second level is the layer (`model`, `persistence`, `application`, `adapter`), the third is the functional sub-domain under `application` and the transport under `adapter`. A functional area is never a sibling of a layer.
- **BREAKING (internal)** Break the three dependency cycles with domain events and by relocating two endpoints to the module that owns what they return. HTTP paths, payloads and status codes are unchanged.
- Extract an outbound port for priority assessment so the evaluation service no longer depends on its own OpenAI adapter.
- Expose each module's public surface through `@NamedInterface` rather than by accident.

## Capabilities

### New Capabilities

- `backend-module-boundaries`: Verified module structure, declared inter-module dependencies, and the published surface of each module.

### Modified Capabilities

- `task-service-responsibility-separation`: Same ownership split, restated against the new package layout.

## Impact

Every backend Java package declaration and import. No HTTP or MCP contract changes: paths, payloads, status codes and MCP tool names are identical. No database change. Maintained clients are untouched.
