# Development workflow

This repository uses OpenSpec.

GitHub issues capture intent.
OpenSpec defines approved behavior.
Implementation must follow the approved OpenSpec change.

## General rules

- Read the current issue or pull request, including comments.
- Inspect relevant code, OpenSpec specs, and active changes before modifying anything.
- Keep changes scoped to the issue.
- Do not invent product behavior.
- Use engineering judgment only for internal details that do not change observable behavior.
- Do not merge pull requests.

## GitHub integration

The environment provides:

- `GH_TOKEN`
- `GH_REPO`
- GitHub CLI (`gh`)

Before publishing Git changes:

- ensure `origin` exists, using `https://github.com/${GH_REPO}.git` if necessary
- run `gh auth setup-git --force --hostname github.com`
- never expose or persist `GH_TOKEN`
- never push directly to the default branch
- never force-push unless explicitly requested

Use `gh` for GitHub operations.

## Workflow commands

### `prepare the spec for this issue`

Execute the specification workflow:

1. Read the complete issue and comments.
2. Inspect relevant code, existing specs, and active changes.
3. Create or update the appropriate OpenSpec change:
   - proposal
   - spec deltas
   - design when useful
   - implementation tasks
4. Make requirements explicit, observable, and testable.
5. Do not modify production code or future implementation tests.
6. Validate the OpenSpec change.
7. Commit and push specification-only changes.
8. Create or update the pull request.
9. Ensure the pull request contains `Closes #<issue-number>`.
10. Apply `spec-review`.

If product behavior is materially ambiguous, follow the blocking rules instead.

### `implement the change`

Execute the implementation workflow:

1. Locate and read the approved OpenSpec change.
2. Treat it as the implementation contract.
3. Implement all required tasks.
4. Preserve existing behavior unless explicitly changed by the spec.
5. Add or update automated tests.
6. Run relevant tests, static checks, and OpenSpec validation.
7. Mark tasks complete only after implementation and verification.
8. Commit and push to the existing pull-request branch.
9. Do not create another pull request.
10. Replace `spec-review` with `implementation-review`.

If the approved specification is materially incomplete or contradictory, follow the blocking rules instead.

### `continue`

Resume the previously blocked workflow using the latest issue or pull-request comments.

Remove `blocked` only when the blocking question is resolved.

## Blocking rules

Investigate before asking for clarification.

Use existing code, specs, active changes, and discussion to resolve questions when the answer is unambiguous.

If a product decision is still required:

- do not choose between plausible behaviors
- identify the exact unresolved decision
- explain its impact
- ask a concrete, decision-oriented question
- apply `blocked`
- do not apply a review label
- stop the affected work

During implementation, continue unrelated work only when it cannot prejudice the unresolved decision.

## Workflow labels

- `spec-review`: specification ready for human review
- `implementation-review`: implementation ready for human review
- `blocked`: human clarification required
