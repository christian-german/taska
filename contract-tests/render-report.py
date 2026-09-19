#!/usr/bin/env python3
"""Turn Schemathesis JUnit and NDJSON output into agent-friendly reports."""

from __future__ import annotations

import argparse
import collections
import datetime as dt
import html
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any


HTTP_OPERATION = re.compile(
    r"\b(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS|TRACE)\s+(/[^\s\]]*)",
    re.IGNORECASE,
)
BEARER_TOKEN = re.compile(r"(?i)(authorization\s*:\s*bearer\s+)[^\s'\"]+")
JWT_TOKEN = re.compile(r"\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\b")
INVALID_XML_CHARACTER = re.compile(r"[\x00-\x08\x0b\x0c\x0e-\x1f\ufffe\uffff]")


def redact(value: str) -> str:
    return JWT_TOKEN.sub("[REDACTED]", BEARER_TOKEN.sub(r"\1[REDACTED]", value))


def sanitize_artifact(path: Path, *, xml: bool = False) -> str:
    """Remove credentials and XML-forbidden fuzz data from retained raw reports."""
    value = redact(path.read_text(encoding="utf-8", errors="replace"))
    if xml:
        value = INVALID_XML_CHARACTER.sub("�", value)
    path.write_text(value, encoding="utf-8")
    return value


def number(value: str | None, cast: type[int] | type[float]) -> int | float:
    try:
        return cast(value or 0)
    except (TypeError, ValueError):
        return cast(0)


def operation_from(name: str, details: str = "") -> dict[str, str | None]:
    match = HTTP_OPERATION.search(name) or HTTP_OPERATION.search(details)
    if not match:
        return {"method": None, "path": None}
    return {"method": match.group(1).upper(), "path": match.group(2)}


def reproduction_commands(details: str) -> list[str]:
    commands: list[str] = []
    lines = details.splitlines()
    index = 0
    while index < len(lines):
        if lines[index].lstrip().startswith("curl "):
            command = lines[index].strip()
            while command.endswith("\\") and index + 1 < len(lines):
                index += 1
                command += "\n" + lines[index].rstrip()
            commands.append(redact(command))
        index += 1
    return commands


def parse_junit(path: Path) -> tuple[dict[str, Any], list[dict[str, Any]], list[dict[str, Any]]]:
    root = ET.fromstring(sanitize_artifact(path, xml=True))
    testcases = list(root.iter("testcase"))
    failures: list[dict[str, Any]] = []
    cases: list[dict[str, Any]] = []

    for testcase in testcases:
        name = testcase.attrib.get("name", "unknown test")
        class_name = testcase.attrib.get("classname")
        duration = float(number(testcase.attrib.get("time"), float))
        problem_nodes = list(testcase.findall("failure")) + list(testcase.findall("error"))
        skipped = testcase.find("skipped") is not None
        outcome = "failed" if problem_nodes else "skipped" if skipped else "passed"
        operation = operation_from(name)
        cases.append(
            {
                "name": name,
                "class_name": class_name,
                "duration_seconds": duration,
                "outcome": outcome,
                "operation": operation,
            }
        )

        for position, problem in enumerate(problem_nodes, start=1):
            message = redact(problem.attrib.get("message", ""))
            body = redact((problem.text or "").strip())
            details = "\n\n".join(part for part in (message, body) if part)
            operation = operation_from(name, details)
            failures.append(
                {
                    "id": f"failure-{len(failures) + 1}",
                    "test_case": name,
                    "class_name": class_name,
                    "operation": operation,
                    "kind": problem.tag,
                    "failure_type": problem.attrib.get("type"),
                    "message": message or problem.attrib.get("type") or problem.tag,
                    "details": details,
                    "reproduction_commands": reproduction_commands(details),
                    "occurrence": position,
                }
            )

    suites = [root] if root.tag == "testsuite" else list(root.findall("testsuite"))
    declared = {
        "tests": sum(int(number(suite.attrib.get("tests"), int)) for suite in suites),
        "failures": sum(int(number(suite.attrib.get("failures"), int)) for suite in suites),
        "errors": sum(int(number(suite.attrib.get("errors"), int)) for suite in suites),
        "skipped": sum(
            int(number(suite.attrib.get("skipped") or suite.attrib.get("disabled"), int))
            for suite in suites
        ),
        "duration_seconds": sum(float(number(suite.attrib.get("time"), float)) for suite in suites),
    }
    if not declared["tests"]:
        declared["tests"] = len(testcases)
        declared["failures"] = sum(case["outcome"] == "failed" for case in cases)
        declared["skipped"] = sum(case["outcome"] == "skipped" for case in cases)

    declared["passed"] = max(
        declared["tests"] - declared["failures"] - declared["errors"] - declared["skipped"],
        0,
    )
    return declared, failures, cases


def parse_ndjson(path: Path) -> tuple[dict[str, int], list[dict[str, Any]], list[str]]:
    event_counts: collections.Counter[str] = collections.Counter()
    interesting_events: list[dict[str, Any]] = []
    parse_errors: list[str] = []

    sanitized = sanitize_artifact(path)
    for line_number, line in enumerate(sanitized.splitlines(), start=1):
        if not line.strip():
            continue
        try:
            event = json.loads(line)
        except json.JSONDecodeError as error:
            parse_errors.append(f"NDJSON line {line_number}: {error}")
            continue

        if len(event) == 1:
            event_type = str(next(iter(event)))
            payload = event[event_type]
        else:
            event_type = str(
                event.get("type")
                or event.get("event_type")
                or event.get("event")
                or "unknown"
            )
            payload = event
        event_counts[event_type] += 1
        payload_status = payload.get("status") if isinstance(payload, dict) else None
        is_notable = payload_status in {"failure", "error", "interrupted"} or any(
            marker in event_type.lower() for marker in ("fail", "error", "warn", "interrupt")
        )
        if is_notable:
            serialized = json.dumps(event, ensure_ascii=False, sort_keys=True)
            interesting_events.append(
                {
                    "line": line_number,
                    "type": event_type,
                    "status": payload_status,
                    "event": event if len(serialized) <= 20000 else serialized[:20000],
                }
            )

    return dict(sorted(event_counts.items())), interesting_events, parse_errors


def tail(path: Path, line_count: int = 120) -> str:
    if not path.exists():
        return ""
    return redact("\n".join(path.read_text(encoding="utf-8", errors="replace").splitlines()[-line_count:]))


def markdown_report(report: dict[str, Any]) -> str:
    summary = report["summary"]
    lines = [
        "# OpenAPI contract test report",
        "",
        f"- Status: **{report['status'].upper()}**",
        f"- Generated at: `{report['generated_at']}`",
        f"- Schemathesis exit code: `{report['schemathesis_exit_code']}`",
        f"- OpenAPI schema: `{report['inputs']['schema']}`",
        f"- API base URL: `{report['inputs']['base_url']}`",
        "",
        "## Summary",
        "",
        "| Tests | Passed | Failures | Errors | Skipped | Duration |",
        "| ---: | ---: | ---: | ---: | ---: | ---: |",
        (
            f"| {summary['tests']} | {summary['passed']} | {summary['failures']} | "
            f"{summary['errors']} | {summary['skipped']} | {summary['duration_seconds']:.3f}s |"
        ),
        "",
    ]

    if report["failures"]:
        lines.extend(["## Failures", ""])
        for failure in report["failures"]:
            operation = failure["operation"]
            operation_label = (
                f"{operation['method']} {operation['path']}"
                if operation["method"] and operation["path"]
                else failure["test_case"]
            )
            lines.extend(
                [
                    f"### {failure['id']}: {operation_label}",
                    "",
                    f"- Test case: `{failure['test_case']}`",
                    f"- Kind: `{failure['kind']}`",
                    f"- Message: {failure['message']}",
                    "",
                    "````text",
                    failure["details"] or "No failure details were emitted.",
                    "````",
                    "",
                ]
            )
            if failure["reproduction_commands"]:
                lines.extend(["Reproduction:", "", "````shell"])
                lines.extend(failure["reproduction_commands"])
                lines.extend(["````", ""])
    else:
        lines.extend(["## Failures", "", "No contract failure was reported.", ""])

    if report["diagnostics"]:
        lines.extend(["## Infrastructure diagnostics", ""])
        for diagnostic in report["diagnostics"]:
            lines.extend([f"- {diagnostic}", ""])

    lines.extend(
        [
            "## Raw artifacts",
            "",
            f"- JUnit: `{report['artifacts']['junit']}`",
            f"- NDJSON: `{report['artifacts']['ndjson']}`",
            f"- Schemathesis log: `{report['artifacts']['log']}`",
            "",
        ]
    )
    return "\n".join(lines)


def html_report(report: dict[str, Any]) -> str:
    summary = report["summary"]
    failure_sections = []
    for failure in report["failures"]:
        operation = failure["operation"]
        title = (
            f"{operation['method']} {operation['path']}"
            if operation["method"] and operation["path"]
            else failure["test_case"]
        )
        reproduction = "\n\n".join(failure["reproduction_commands"])
        failure_sections.append(
            "<details open><summary>"
            + html.escape(f"{failure['id']}: {title}")
            + "</summary><h3>Message</h3><p>"
            + html.escape(failure["message"])
            + "</p><h3>Details</h3><pre>"
            + html.escape(failure["details"] or "No failure details were emitted.")
            + "</pre>"
            + ("<h3>Reproduction</h3><pre>" + html.escape(reproduction) + "</pre>" if reproduction else "")
            + "</details>"
        )
    failures_html = "".join(failure_sections) or "<p>No contract failure was reported.</p>"
    status_class = "passed" if report["status"] == "passed" else "failed"

    return f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Taska OpenAPI contract report</title>
  <style>
    :root {{ color-scheme: light dark; font-family: system-ui, sans-serif; }}
    body {{ max-width: 1100px; margin: 2rem auto; padding: 0 1rem; line-height: 1.5; }}
    .status {{ display: inline-block; padding: .25rem .65rem; border-radius: 999px; font-weight: 700; }}
    .passed {{ background: #176b3a; color: white; }} .failed {{ background: #a32323; color: white; }}
    table {{ border-collapse: collapse; width: 100%; margin: 1rem 0 2rem; }}
    th, td {{ border: 1px solid #8888; padding: .5rem; text-align: right; }} th:first-child, td:first-child {{ text-align: left; }}
    details {{ border: 1px solid #8888; border-radius: .5rem; padding: .75rem; margin: 1rem 0; }}
    summary {{ cursor: pointer; font-weight: 700; }} pre {{ overflow: auto; padding: 1rem; background: #8882; }}
    code {{ overflow-wrap: anywhere; }}
  </style>
</head>
<body>
  <h1>Taska OpenAPI contract report</h1>
  <p><span class="status {status_class}">{html.escape(report['status'].upper())}</span></p>
  <p>Generated at <code>{html.escape(report['generated_at'])}</code></p>
  <table>
    <thead><tr><th>Status</th><th>Tests</th><th>Passed</th><th>Failures</th><th>Errors</th><th>Skipped</th><th>Duration</th></tr></thead>
    <tbody><tr><td>{html.escape(report['status'])}</td><td>{summary['tests']}</td><td>{summary['passed']}</td><td>{summary['failures']}</td><td>{summary['errors']}</td><td>{summary['skipped']}</td><td>{summary['duration_seconds']:.3f}s</td></tr></tbody>
  </table>
  <h2>Failures</h2>
  {failures_html}
</body>
</html>
"""


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--junit", type=Path, required=True)
    parser.add_argument("--ndjson", type=Path, required=True)
    parser.add_argument("--log", type=Path, required=True)
    parser.add_argument("--markdown", type=Path, required=True)
    parser.add_argument("--json", dest="json_path", type=Path, required=True)
    parser.add_argument("--html", type=Path, required=True)
    parser.add_argument("--schemathesis-exit-code", type=int, required=True)
    parser.add_argument("--schema", required=True)
    parser.add_argument("--base-url", required=True)
    return parser.parse_args()


def main() -> int:
    args = parse_arguments()
    diagnostics: list[str] = []
    summary: dict[str, Any] = {
        "tests": 0,
        "passed": 0,
        "failures": 0,
        "errors": 0,
        "skipped": 0,
        "duration_seconds": 0.0,
    }
    failures: list[dict[str, Any]] = []
    cases: list[dict[str, Any]] = []
    event_counts: dict[str, int] = {}
    interesting_events: list[dict[str, Any]] = []

    if args.junit.exists() and args.junit.stat().st_size:
        try:
            summary, failures, cases = parse_junit(args.junit)
        except (ET.ParseError, OSError, ValueError) as error:
            diagnostics.append(f"Unable to parse JUnit report: {error}")
    else:
        diagnostics.append("Schemathesis did not produce a JUnit report.")

    if args.ndjson.exists() and args.ndjson.stat().st_size:
        try:
            event_counts, interesting_events, parse_errors = parse_ndjson(args.ndjson)
            diagnostics.extend(parse_errors)
        except OSError as error:
            diagnostics.append(f"Unable to parse NDJSON report: {error}")
    else:
        diagnostics.append("Schemathesis did not produce an NDJSON report.")

    if args.schemathesis_exit_code > 1:
        log_tail = tail(args.log)
        diagnostics.append(
            "Schemathesis aborted because of an infrastructure, configuration, or schema error."
            + (f" Last log lines:\n{log_tail}" if log_tail else "")
        )

    status = "passed"
    if args.schemathesis_exit_code > 1 or any("did not produce" in item for item in diagnostics):
        status = "error"
    elif args.schemathesis_exit_code == 1 or summary["failures"] or summary["errors"]:
        status = "failed"

    operations = {
        f"{case['operation']['method']} {case['operation']['path']}"
        for case in cases
        if case["operation"]["method"] and case["operation"]["path"]
    }
    failed_operations = {
        f"{failure['operation']['method']} {failure['operation']['path']}"
        for failure in failures
        if failure["operation"]["method"] and failure["operation"]["path"]
    }
    summary["operations_observed"] = len(operations)
    summary["operations_failed"] = len(failed_operations)

    report = {
        "report_version": 1,
        "generated_at": dt.datetime.now(dt.timezone.utc).isoformat(),
        "status": status,
        "schemathesis_exit_code": args.schemathesis_exit_code,
        "inputs": {
            "schema": args.schema,
            "base_url": args.base_url,
            "generation_mode": "positive",
            "authentication": "OAuth2 development user (credentials redacted)",
        },
        "summary": summary,
        "failures": failures,
        "test_cases": cases,
        "ndjson_event_counts": event_counts,
        "notable_ndjson_events": interesting_events,
        "diagnostics": diagnostics,
        "artifacts": {
            "html": "reports/contract-report.html",
            "markdown": "reports/contract-report.md",
            "json": "reports/contract-report.json",
            "junit": "reports/raw/junit.xml",
            "ndjson": "reports/raw/events.ndjson",
            "schemathesis_html": "reports/raw/schema-coverage.html",
            "log": "reports/raw/schemathesis.log",
            "startup_log": "reports/raw/startup.log (when startup fails)",
        },
        "agent_guidance": [
            "Start with failures[].operation and failures[].details to locate the affected endpoint and mismatch.",
            "Use failures[].reproduction_commands when present; authorization values are redacted.",
            "Compare the response and request described in the failure with docs/openapi and the backend controller/DTO.",
            "After a correction, rerun ./contract-tests/verify-contract.sh and require status=passed.",
        ],
    }

    for output_path in (args.markdown, args.json_path, args.html):
        output_path.parent.mkdir(parents=True, exist_ok=True)
    args.json_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    args.markdown.write_text(markdown_report(report), encoding="utf-8")
    args.html.write_text(html_report(report), encoding="utf-8")

    label = {"passed": "PASSED", "failed": "FAILED", "error": "ERROR"}[status]
    print(
        f"Contract tests: {label} — {summary['tests']} tests, "
        f"{summary['failures']} failures, {summary['errors']} errors, "
        f"{summary['skipped']} skipped."
    )
    print("Reports: contract-tests/reports/contract-report.{html,md,json}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:  # Keep the shell contract to 0/1 and explain report failures.
        print(f"Unable to render contract-test reports: {error}", file=sys.stderr)
        raise SystemExit(1)
