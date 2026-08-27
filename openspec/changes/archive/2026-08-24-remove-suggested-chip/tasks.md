## 1. Remove suggestion presentation

- [x] 1.1 Remove the Today-screen computation and task-list binding used only to identify suggested tasks.
- [x] 1.2 Remove the suggestion input propagation from the shared task list and task row.
- [x] 1.3 Remove the conditional "suggéré" chip while preserving every other task-row field, metadata marker, and interaction.

## 2. Verification

- [x] 2.1 Add or update Angular component tests to verify that task rows never render the suggestion marker and that unrelated metadata still renders.
- [x] 2.2 Run focused frontend tests, frontend static/build checks, and strict OpenSpec validation.
