## MODIFIED Requirements

### Requirement: Backend features own their technical layers
Each maintained Spring feature that contains HTTP, application-service, or persistence responsibilities SHALL own corresponding `controller`, `service`, and `repository` subpackages. A feature MAY define cohesive nested subfeatures, and each nested subfeature SHALL own its corresponding technical subpackages. HTTP controllers, API DTOs, request records, controller advice, and HTTP boundary mappers SHALL reside in `controller`; application services and application input/result records SHALL reside in `service`; repository interfaces, persistence adapters, and JPA entities SHALL reside in `repository`; non-persistence value objects and domain exceptions SHALL remain in the owning feature or subfeature root.

#### Scenario: Architecture verification inspects feature packages
- **WHEN** automated architecture checks inspect backend production classes, including classes in nested subfeatures
- **THEN** each class SHALL reside in the package matching its responsibility
- **AND** each JPA entity SHALL reside in its owning `repository` package
