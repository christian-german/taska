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

For an issue that already has an open pull request:
- that pull request and its `headRefName` are authoritative
- never derive a new remote branch name from the local Codex branch or workflow phase
- never use a stacked pull request for specification → implementation transitions

If an expected existing branch or pull request cannot be found, stop with `blocked`.
Never recover by creating a replacement branch or pull request.

Use `gh` for GitHub operations.

## Workflow commands

### `prepare the spec for this issue`

Execute the specification workflow:

1. Read the complete issue and comments.
2. Inspect relevant code, existing specs, and active changes.
3. Create one dedicated issue branch if one does not already exist
   - use `feat/<short-name>` for features and improvements
   - use `fix/<short-name>` for bug fixes
4. Create or update the appropriate OpenSpec change:
   - proposal
   - spec deltas
   - design when useful
   - implementation tasks
5. Make requirements explicit, observable, and testable.
6. Do not modify production code or future implementation tests.
7. Validate the OpenSpec change.
8. Commit and push specification-only changes.
9. Create or update the pull request. Name the pull request the same as the branch name.
10. Ensure the pull request contains `Closes #<issue-number>`.
11. Apply `spec-review` to both the issue and pull request.
12. Remove `ready-for-spec` from both if present.

If product behavior is materially ambiguous, follow the blocking rules instead.

### `implement the change`

Execute the implementation workflow:

1. Locate and read the approved OpenSpec change.
2. Treat it as the implementation contract.
3. Locate the existing pull request associated with the issue.
   - reuse its exact `headRefName` as the target GitHub branch
   - never create another branch
   - never create another pull request
   - if the existing pull request cannot be identified unambiguously, apply `blocked` and stop
4. Implement all required tasks.
5. Preserve existing behavior unless explicitly changed by the spec.
6. Add or update automated tests.
7. Run relevant tests, static checks, and OpenSpec validation.
8. Mark tasks complete only after implementation and verification.
9. Commit the implementation and push it to the existing pull request's `headRefName`.
10. Replace `spec-review` with `implementation-review` on the pull request and issue.

Creating a pull request during implementation is forbidden.
The pull request is created during specification and reused for the entire issue lifecycle.

### `explore this issue`

Use OpenSpec explore mode to investigate the issue before specification.

1. Read the complete issue and comments.
2. Inspect relevant code, existing specs, and active changes.
3. Investigate the problem and its current behavior.
4. Identify constraints, affected areas, plausible approaches, and trade-offs.
5. Challenge assumptions when the code or existing specs contradict them.
6. Do not create an OpenSpec change, branch, pull request, or production code.
7. Post useful findings and concrete unresolved questions on the GitHub issue.

Exploration is conversational. Do not invent product decisions.

If exploration requires human input:
- post the concrete questions on the issue
- apply `input-needed`, do not apply `blocked`
- stop until new information is provided

When exploration is complete and no further human input is required:
- remove `input-needed` if present
- apply `ready-for-spec`
- state the main conclusions and agreed behavior on the issue

### `archive the change`

Finalize the implemented and human-approved OpenSpec change.

1. Locate the OpenSpec change associated with the current issue and pull request.
2. Ensure the implementation is complete and no OpenSpec task remains intentionally pending.
3. Synchronize the change's delta specs into `openspec/specs/`.
4. Archive the change with OpenSpec so its spec deltas are synchronized into the canonical specifications.
5. Validate the resulting OpenSpec state.
6. Commit only the archive and synchronized specification changes.
7. Push the commit to the existing pull-request branch.
8. Do not create another branch or pull request.
9. Do not merge the pull request.

Always perform the sync before archiving unless explicitly instructed not to.

If archiving reveals a conflict, incomplete requirement, or unexpected spec inconsistency:

- do not resolve product behavior implicitly
- apply `blocked`
- explain the exact problem on the issue or pull request
- stop before committing the archive

After a successful archive:

- remove `implementation-review`
- apply `ready-for-merge`
- keep the issue and pull request ready for final CI and merge

### `continue`

Resume the currently pending workflow using the latest issue or pull-request comments.

Determine the workflow from the current state:
- `input-needed` → resume exploration
- `blocked` during specification → resume specification
- `blocked` during implementation → resume implementation

Remove `blocked` or `input-needed` only when the corresponding question is resolved.

## Blocking rules

`blocked` is only for unexpected problems during specification or implementation.
Never use `blocked` during exploration; use `input-needed` instead.

Investigate before asking for clarification.

Use existing code, specs, active changes, and discussion to resolve questions when the answer is unambiguous.

If a product decision is still required:

- do not choose between plausible behaviors
- identify the exact unresolved decision
- explain its impact
- ask a concrete, decision-oriented question
- apply `blocked` on the pull request AND the issue
- do not apply a review label
- stop the affected work

During implementation, continue unrelated work only when it cannot prejudice the unresolved decision.

## Workflow labels

Workflow labels represent the state of the issue.

When a pull request exists, keep the same workflow label on both the issue and the pull request.

- `spec-review`: specification ready for human review
- `implementation-review`: implementation ready for human review
- `ready-for-merge`: implementation and spec are ready for merge
- `blocked`: unexpected issue prevents specification or implementation from proceeding
- `input-needed`: exploration requires human input before continuing
- `ready-for-spec`: exploration is complete and the issue is sufficiently defined to prepare a specification
