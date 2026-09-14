## ADDED Requirements

### Requirement: Backend features own their technical layers
Each maintained Spring feature that contains HTTP, application-service, or persistence responsibilities SHALL own corresponding `controller`, `service`, and `repository` subpackages. HTTP controllers, API DTOs, request records, controller advice, and HTTP boundary mappers SHALL reside in `controller`; application services and application input/result records SHALL reside in `service`; repository interfaces and persistence adapters SHALL reside in `repository`; entities, value objects, and domain exceptions SHALL remain in the feature root.

#### Scenario: Architecture verification inspects feature packages
- **WHEN** automated architecture checks inspect backend production classes
- **THEN** each class SHALL reside in the package matching its responsibility

### Requirement: Application services are transport-independent
Application services SHALL NOT depend on classes in controller packages or on MCP input/output types. REST and MCP adapters SHALL map their inputs to operation-specific application parameters before invoking a service, and services SHALL return entities or application result types rather than HTTP DTOs.

#### Scenario: REST and MCP create the same resource
- **WHEN** REST and MCP adapters perform equivalent creation operations
- **THEN** both adapters SHALL invoke the same application service behavior using the same application parameter type
- **AND** neither service implementation SHALL import a transport request or response type

### Requirement: Backend dependencies follow layer direction
Repositories SHALL NOT depend on controller or service packages, and services SHALL NOT depend on controller packages. Required Spring component dependencies SHALL use constructor injection and remain final where the class model permits.

#### Scenario: Architecture verification inspects dependencies
- **WHEN** automated architecture checks inspect repository and service dependencies
- **THEN** no forbidden inward dependency or mutable injected dependency SHALL be present

### Requirement: Shared business behavior has one authoritative implementation
Every business invariant, transition rule, and business side effect SHALL have one authoritative application or domain implementation reused by every applicable REST, MCP, scheduled, import, or batch mutation path. A transport adapter SHALL NOT bypass that behavior through generic property copying or direct repository mutation.

#### Scenario: Overlapping mutation paths change scheduling
- **WHEN** two supported transports change a resource property governed by scheduling validation or related side effects
- **THEN** both paths SHALL execute the same validation and side-effect implementation
