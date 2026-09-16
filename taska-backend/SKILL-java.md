---
name: java-development
description: Apply reusable Java naming, design, API, and error-handling standards when writing, modifying, refactoring,
  or reviewing Java source and tests.
  Use alongside spring-development for Java work involving Spring; this skill contains no Spring-specific rules.
---

# Java development

Apply these standards to the Java code within the requested task.
Treat them as cross-project defaults: follow the user's current instructions and explicit repository or module conventions where they differ.
Existing code is a useful context, but an isolated legacy pattern is not automatically a convention.
Do not expand a focused change into repository-wide cleanup.

## Code formatting

After writing, modifying, or refactoring Java code, run the project's formatter before considering the task complete:

```bash
mvn fmt:format
```

This applies `google-java-format` via the `fmt-maven-plugin` already configured in the project's `pom.xml`.
Do not hand-format code to match Google style manually — always invoke the formatter, since it is the deterministic source of truth for style in this project.

If the formatter reports failures unrelated to your change (pre-existing non-compliant files outside the scope of the task),
do not fix them unless the task explicitly asks for a formatting cleanup — flag them instead.

## API naming conventions

When Java code models or consumes an HTTP API contract:
- Use camelCase for API-facing identifiers and properties.
- Path variables must use camelCase, for example, {taskId} or {projectId}.
- Query parameters must use camelCase, for example, projectId or isFavorite.
- JSON properties must use camelCase, for example, scheduledDate or projectId.

Do not introduce snake_case serialization aliases unless required by an external API contract.

Static URL path segments containing multiple words should use kebab-case, for example, /following-occurrences.

## Name values for their meaning

Prefer a natural, explicit name derived from the value's role or type. Use `LabelRequest labelRequest` instead of `LabelRequest req`.
Avoid generic abbreviations such as `req`, `res`, `resp`, `obj`, `svc`, `mgr`, and `tmp` when a meaningful name fits naturally.

```java
// Avoid: the reader has to decode these names.
Label createLabel(LabelRequest req) {
    Label res = labelMapper.toLabel(req);
    return labelRepository.save(res);
}

// Prefer: names carry the role through the operation.
Label createLabel(LabelRequest labelRequest) {
    Label label = labelMapper.toLabel(labelRequest);
    return labelRepository.save(label);
}
```

When several values share a type, name their distinct roles: `sourceLabel`, `targetLabel`, `existingLabel`, or `updatedLabel`.
A role such as `recipient` can be clearer than mechanically repeating `User`.
Use `labels` for a collection, and `labelsById` when the lookup key matters.

Do not rename serialized fields, public API elements, schema identifiers, or reflection-sensitive members merely to satisfy a local variable preference.
Evaluate compatibility when such a rename is part of the requested change.

## One mechanism per responsibility

A project settles a responsibility once: mapping, validation, error translation, configuration.
When such a mechanism exists, use it. Do not introduce a second idiom for the same responsibility because it is shorter to write in the class at hand.

Before writing a conversion, a validation, or an error translation by hand, look at how a sibling class in the same module solves the same problem, and follow it.
Two classes doing the same thing in two ways is not a style detail: it doubles the places a future change has to reach, and it hides which one is authoritative.

If the established mechanism is genuinely inadequate for the case at hand, change the mechanism for the whole module or raise it as a design decision. Do not work around it locally.

An abstraction that contributes nothing is worse than no abstraction.
A code-generation annotation on a type that is written entirely by hand, or a type that exists only to delegate to another, tells the reader something that is false.
Remove it, or use it for what it is.

## No compatibility overloads inside the application

Do not add an overloaded constructor, factory, or method whose only purpose is to keep existing call sites compiling after a type gained a component or a parameter.
In an application compiled as a whole, those callers do not exist: update the call sites instead.

```java
// Avoid: nothing calls this, and it hard-codes decisions the real constructor asks for.
public record TaskDto(UUID id, String content, Instant dueAt, TaskType type) {
  /** Compatibility constructor for callers compiled before task type was introduced. */
  public TaskDto(UUID id, String content) {
    this(id, content, null, TaskType.TODO);
  }
}
```

These overloads are untested paths that silently inject defaults, and they keep growing as the type evolves.
This applies to records in particular: adding a component to a record is a mechanical update of its construction sites, and the compiler lists them all.

The exception is a genuinely published artifact consumed by independently compiled clients. Record the reason next to the overload when that is the case.
