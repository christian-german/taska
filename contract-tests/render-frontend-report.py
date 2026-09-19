#!/usr/bin/env python3
"""Render deterministic Angular-to-Prism contract-test results as Markdown."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any


MARKER = "FRONTEND_CONTRACT_VIOLATION:"
REQUEST_LOG = re.compile(r"\[HTTP SERVER\]\s+(get|post|put|patch|delete)\s+(\S+)")
VIOLATION_LOG = re.compile(r"Violation:\s*(.+)$")


def extract_violation(message: str) -> dict[str, Any] | None:
    marker_index = message.find(MARKER)
    if marker_index < 0:
        return None
    payload = message[marker_index + len(MARKER) :]
    try:
        value, _ = json.JSONDecoder().raw_decode(payload)
    except json.JSONDecodeError:
        return None
    return value if isinstance(value, dict) else None


def load_results(path: Path) -> tuple[dict[str, int], list[dict[str, Any]], list[str]]:
    if not path.exists():
        return {"total": 0, "passed": 0, "failed": 0}, [], [
            "Vitest did not produce a JSON report."
        ]

    try:
        report = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        return {"total": 0, "passed": 0, "failed": 0}, [], [
            f"Could not read Vitest JSON: {error}"
        ]

    summary = {
        "total": int(report.get("numTotalTests", 0)),
        "passed": int(report.get("numPassedTests", 0)),
        "failed": int(report.get("numFailedTests", 0)),
    }
    violations: list[dict[str, Any]] = []
    failures: list[str] = []

    for test_result in report.get("testResults", []):
        for assertion in test_result.get("assertionResults", []):
            if assertion.get("status") != "failed":
                continue
            test_name = str(
                assertion.get("fullName") or assertion.get("title") or "unknown test"
            )
            found = False
            for message in assertion.get("failureMessages", []):
                violation = extract_violation(str(message))
                if violation is None:
                    continue
                violation["test"] = test_name
                violations.append(violation)
                found = True
                break
            if not found:
                failures.append(test_name)

    violations.sort(
        key=lambda item: (str(item.get("endpoint", "")), str(item.get("test", "")))
    )
    failures.sort()
    return summary, violations, failures


def prism_messages(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    messages: dict[str, str] = {}
    endpoint: str | None = None
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        request_match = REQUEST_LOG.search(line)
        if request_match:
            endpoint = f"{request_match.group(1).upper()} {request_match.group(2)}"
            continue
        violation_match = VIOLATION_LOG.search(line)
        if endpoint and violation_match:
            messages[endpoint] = violation_match.group(1).strip()
    return messages


def apply_prism_messages(
    violations: list[dict[str, Any]], messages: dict[str, str]
) -> None:
    for violation in violations:
        endpoint = str(violation.get("endpoint", ""))
        if endpoint in messages:
            violation["prismValidationMessage"] = messages[endpoint]


def fenced_json(value: Any) -> list[str]:
    return [
        "```json",
        json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True),
        "```",
    ]


def render(
    summary: dict[str, int],
    violations: list[dict[str, Any]],
    failures: list[str],
    vitest_exit_code: int,
) -> str:
    passed = (
        vitest_exit_code == 0
        and summary["failed"] == 0
        and not failures
        and not violations
    )
    lines = [
        "# Frontend OpenAPI contract report",
        "",
        f"- Status: **{'PASS' if passed else 'FAIL'}**",
        "- OpenAPI schema: `docs/openapi/taska.openapi.yaml`",
        "- Validator: `stoplight/prism:5` (mock mode with request errors enabled)",
        "",
        "## Summary",
        "",
        "| Executed tests | Passed tests | Failed tests | Contract violations |",
        "| ---: | ---: | ---: | ---: |",
        f"| {summary['total']} | {summary['passed']} | {summary['failed']} | {len(violations)} |",
        "",
        "## Contract violations",
        "",
    ]

    if not violations:
        lines.extend(["No contract violation was reported.", ""])
    else:
        for index, violation in enumerate(violations, start=1):
            endpoint = violation.get("endpoint", "unknown endpoint")
            lines.extend(
                [
                    f"### Violation {index}: {endpoint}",
                    "",
                    f"- Test: `{violation.get('test', 'unknown test')}`",
                    f"- Affected endpoint: `{endpoint}`",
                    "- Request:",
                    "",
                    *fenced_json(violation.get("request", {})),
                    "",
                    "- Prism validation message:",
                    "",
                    "```text",
                    str(
                        violation.get(
                            "prismValidationMessage",
                            "No validation message was returned.",
                        )
                    ),
                    "```",
                    "",
                    "- Recommended fix: "
                    + str(
                        violation.get(
                            "recommendedFix",
                            "Align the frontend request with the OpenAPI contract.",
                        )
                    ),
                    "",
                ]
            )

    if failures:
        lines.extend(["## Non-contract test failures", ""])
        lines.extend(f"- `{failure}`" for failure in failures)
        lines.append("")

    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--vitest", type=Path, required=True)
    parser.add_argument("--prism-log", type=Path, required=True)
    parser.add_argument("--markdown", type=Path, required=True)
    parser.add_argument("--vitest-exit-code", type=int, required=True)
    args = parser.parse_args()

    summary, violations, failures = load_results(args.vitest)
    apply_prism_messages(violations, prism_messages(args.prism_log))
    args.markdown.parent.mkdir(parents=True, exist_ok=True)
    args.markdown.write_text(
        render(summary, violations, failures, args.vitest_exit_code), encoding="utf-8"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
