# OpenAPI contract test report

- Status: **FAILED**
- Generated at: `2026-09-19T14:03:19.127011+00:00`
- Schemathesis exit code: `1`
- OpenAPI schema: `docs/openapi/taska.openapi.yaml`
- API base URL: `http://taska-backend:8080`

## Summary

| Tests | Passed | Failures | Errors | Skipped | Duration |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 35 | 22 | 13 | 0 | 0 | 48.727s |

## Failures

### failure-1: POST /register-device

- Test case: `POST /register-device`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: yUjKpv

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/register-device","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"token": "\t\u00cc\u0000ax\u0014\ud8e7\udf23"}' http://taska-backend:8080/register-device
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"token": "\t\u00cc\u0000ax\u0014\ud8e7\udf23"}' http://taska-backend:8080/register-device
````

### failure-2: PUT /tasks/{taskId}

- Test case: `PUT /tasks/{taskId}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: R6gM60

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/tasks/ef1bbe9c-5f5e-3c56-b2b5-c545ff3ff8b7","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"dueAt": "2026-09-19T14:02:19.715740930Z", "mentionContext": "__main__", "description": null, "content": "0", "labels": ["", "\u0016k\udb69\udc76", "\u0014\ud826\udf52\ubd68\u00c2vA", "\u00c0w\u0012\u00f5\udb8e\udd82\u00ddo9\udb8f\udc3a\ud9e6\udd85\u000f3", "l\u00e8\u00edO\ud9cf\udf54", "\u0006\u00d9W\u00dd\udabd\udd5b\u00193\u0013\u00eb\u00c2\u00a8\u00cd\u00ae\u001a\u009c&\udade\udf15\ud9fe\udee8\ud96c\udd70", "\u00e9\u00ff\u0087\ud906\udcc2\u00cd", "", "\udb01\udec5\u00fd\u00a0\u00cc", "\ud81d\udd58\u00b7C\t\u00b7\udb25\udd81", "\u00fa\u0000", "\udade\udd3aQ\u00d0\"b\ud8c0\udeaf\ud963\udeb9\ud992\udcc5\u00da\u00e9\u000e", "", ".exe", "\u001bK\u0015\udb5a\udce8\u0016", "\uda8d\udc36", "\ud922\ude99\udbc3\udc5f\udaa8\udc99\u00e9", "\u00b3\u001b\u00f9Z`\u90db\u00ad"], "allDay": true, "recurrenceRule": "d\\Zb\uda09\ude2c\uda9f\ude59\u00e7\u001a\u00f2\u00ac\u0080g\u001e\u00f2\u00da\u00c4", "parentId": null, "isRecurring": false, "projectId": "769523d0-6409-2893-aaec-24551e705bab", "estimateMinutes": 1, "scheduledAt": "3787-12-17T01:35:43.90125+19:56", "type": "APPOINTMENT", "order": -5446, "priority": 1}' http://taska-backend:8080/tasks/ef1bbe9c-5f5e-3c56-b2b5-c545ff3ff8b7
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"dueAt": "2026-09-19T14:02:19.715740930Z", "mentionContext": "__main__", "description": null, "content": "0", "labels": ["", "\u0016k\udb69\udc76", "\u0014\ud826\udf52\ubd68\u00c2vA", "\u00c0w\u0012\u00f5\udb8e\udd82\u00ddo9\udb8f\udc3a\ud9e6\udd85\u000f3", "l\u00e8\u00edO\ud9cf\udf54", "\u0006\u00d9W\u00dd\udabd\udd5b\u00193\u0013\u00eb\u00c2\u00a8\u00cd\u00ae\u001a\u009c&\udade\udf15\ud9fe\udee8\ud96c\udd70", "\u00e9\u00ff\u0087\ud906\udcc2\u00cd", "", "\udb01\udec5\u00fd\u00a0\u00cc", "\ud81d\udd58\u00b7C\t\u00b7\udb25\udd81", "\u00fa\u0000", "\udade\udd3aQ\u00d0\"b\ud8c0\udeaf\ud963\udeb9\ud992\udcc5\u00da\u00e9\u000e", "", ".exe", "\u001bK\u0015\udb5a\udce8\u0016", "\uda8d\udc36", "\ud922\ude99\udbc3\udc5f\udaa8\udc99\u00e9", "\u00b3\u001b\u00f9Z`\u90db\u00ad"], "allDay": true, "recurrenceRule": "d\\Zb\uda09\ude2c\uda9f\ude59\u00e7\u001a\u00f2\u00ac\u0080g\u001e\u00f2\u00da\u00c4", "parentId": null, "isRecurring": false, "projectId": "769523d0-6409-2893-aaec-24551e705bab", "estimateMinutes": 1, "scheduledAt": "3787-12-17T01:35:43.90125+19:56", "type": "APPOINTMENT", "order": -5446, "priority": 1}' http://taska-backend:8080/tasks/ef1bbe9c-5f5e-3c56-b2b5-c545ff3ff8b7
````

### failure-3: PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}

- Test case: `PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: uC7ALd

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request parameter","instance":"/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/occurrences/value","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"dueAt": null, "priority": 1, "scheduledAt": "2000-01-01T00:00:00Z", "title": "0"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/occurrences/value
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"dueAt": null, "priority": 1, "scheduledAt": "2000-01-01T00:00:00Z", "title": "0"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/occurrences/value
````

### failure-4: PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following

- Test case: `PUT /tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: VgeWmd

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request parameter","instance":"/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/occurrences/value/following","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"allDay": false, "content": "0", "description": null, "dueAt": null, "estimateMinutes": 0, "isRecurring": true, "labels": [], "mentionContext": null, "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "priority": 1, "projectId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455", "recurrenceRule": "", "scheduledAt": "2000-01-01T00:00:00Z", "type": "TODO"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/occurrences/value/following
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"allDay": false, "content": "0", "description": null, "dueAt": null, "estimateMinutes": 0, "isRecurring": true, "labels": [], "mentionContext": null, "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "priority": 1, "projectId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455", "recurrenceRule": "", "scheduledAt": "2000-01-01T00:00:00Z", "type": "TODO"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/occurrences/value/following
````

### failure-5: PUT /planning-calendars/{planningCalendarId}

- Test case: `PUT /planning-calendars/{planningCalendarId}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: XYP6We

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Validation failed","instance":"/planning-calendars/5c39e3e6-9906-5bdf-b6fc-68a4477c7aaf","status":400,"title":"Bad Request","type":"about:blank","errors":{"rules":"availability rules must not overlap"}}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"rules": [{"endMinute": 780, "startMinute": 551, "dayOfWeek": 4}, {"endMinute": 780, "startMinute": 551, "dayOfWeek": 4}], "name": "\u00eet9.\uda11\ude43\u00e9\u00e7\u0091\u001a\ud8cc\udd7c\udb45\uded7\ud824\uddcd\u0095j\u00e2!ca"}' http://taska-backend:8080/planning-calendars/5c39e3e6-9906-5bdf-b6fc-68a4477c7aaf
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"rules": [{"endMinute": 780, "startMinute": 551, "dayOfWeek": 4}, {"endMinute": 780, "startMinute": 551, "dayOfWeek": 4}], "name": "\u00eet9.\uda11\ude43\u00e9\u00e7\u0091\u001a\ud8cc\udd7c\udb45\uded7\ud824\uddcd\u0095j\u00e2!ca"}' http://taska-backend:8080/planning-calendars/5c39e3e6-9906-5bdf-b6fc-68a4477c7aaf
````

### failure-6: POST /tasks

- Test case: `POST /tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 5j7k1o

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"allDay": null, "content": "0", "description": null, "dueAt": "2000-01-01T00:00:00Z", "estimateMinutes": 1, "isRecurring": null, "labels": [], "mentionContext": null, "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "priority": 1, "projectId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455", "recurrenceRule": null, "scheduledAt": null, "type": null}' http://taska-backend:8080/tasks

2. Test Case ID: bRAlOu

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Recurring series requires a recurrence rule","instance":"/tasks","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"allDay": null, "content": "0", "description": null, "dueAt": null, "estimateMinutes": 1, "isRecurring": true, "labels": [], "mentionContext": null, "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "priority": 1, "projectId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455", "recurrenceRule": null, "scheduledAt": "2000-01-01T00:00:00Z", "type": "APPOINTMENT"}' http://taska-backend:8080/tasks
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"allDay": null, "content": "0", "description": null, "dueAt": "2000-01-01T00:00:00Z", "estimateMinutes": 1, "isRecurring": null, "labels": [], "mentionContext": null, "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "priority": 1, "projectId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455", "recurrenceRule": null, "scheduledAt": null, "type": null}' http://taska-backend:8080/tasks
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"allDay": null, "content": "0", "description": null, "dueAt": null, "estimateMinutes": 1, "isRecurring": true, "labels": [], "mentionContext": null, "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "priority": 1, "projectId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455", "recurrenceRule": null, "scheduledAt": "2000-01-01T00:00:00Z", "type": "APPOINTMENT"}' http://taska-backend:8080/tasks
````

### failure-7: POST /tasks/{taskId}/reopen

- Test case: `POST /tasks/{taskId}/reopen`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: ydHtHf

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/reopen","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"occurrenceScheduledAt": "2067-08-17T06:47:31+21:20"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/reopen
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"occurrenceScheduledAt": "2067-08-17T06:47:31+21:20"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/reopen
````

### failure-8: POST /tasks/{taskId}/close

- Test case: `POST /tasks/{taskId}/close`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: FLyIQc

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"occurrenceScheduledAt": "2067-08-17T06:47:31+21:20"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"occurrenceScheduledAt": "2067-08-17T06:47:31+21:20"}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close
````

### failure-9: POST /planning-calendars

- Test case: `POST /planning-calendars`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: RwbipP

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/planning-calendars","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "+\ud8a4\udd42\u007f\u00aexS\t\u00cc\u0000ax\u0014\ud8e7\udf23"}' http://taska-backend:8080/planning-calendars
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "+\ud8a4\udd42\u007f\u00aexS\t\u00cc\u0000ax\u0014\ud8e7\udf23"}' http://taska-backend:8080/planning-calendars
````

### failure-10: POST /comments

- Test case: `POST /comments`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: hxDZb9

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/comments","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "00", "projectId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "taskId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455"}' http://taska-backend:8080/comments
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "00", "projectId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "taskId": "f728b4fa-4248-1e3a-8a5d-2f346baa9455"}' http://taska-backend:8080/comments
````

### failure-11: POST /projects

- Test case: `POST /projects`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: WHgkzj

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"color": null, "isFavorite": null, "name": "0", "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "planningCalendarId": null, "viewStyle": null}' http://taska-backend:8080/projects
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"color": null, "isFavorite": null, "name": "0", "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "planningCalendarId": null, "viewStyle": null}' http://taska-backend:8080/projects
````

### failure-12: PUT /projects/{projectId}

- Test case: `PUT /projects/{projectId}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: JGTJUT

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects/3a2c0646-15fa-4730-8b65-c1bb646edf12","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"color": "#808080", "isFavorite": false, "name": "0", "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "planningCalendarId": "00000000-0000-0000-0000-000000000001", "viewStyle": "CALENDAR"}' http://taska-backend:8080/projects/3a2c0646-15fa-4730-8b65-c1bb646edf12
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"color": "#808080", "isFavorite": false, "name": "0", "order": 0, "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "planningCalendarId": "00000000-0000-0000-0000-000000000001", "viewStyle": "CALENDAR"}' http://taska-backend:8080/projects/3a2c0646-15fa-4730-8b65-c1bb646edf12
````

### failure-13: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: UXuMDE

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects/56c919d4-21ca-45c4-9d5a-6413d25e25a2","status":500,"title":"Internal Server Error","type":"about:blank"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/comments?taskId=7ab3efac-ade6-1067-a1ec-b0338da3f215&projectId=56c919d4-21ca-45c4-9d5a-6413d25e25a2'
    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/56c919d4-21ca-45c4-9d5a-6413d25e25a2
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/comments?taskId=7ab3efac-ade6-1067-a1ec-b0338da3f215&projectId=56c919d4-21ca-45c4-9d5a-6413d25e25a2'
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/56c919d4-21ca-45c4-9d5a-6413d25e25a2
````

### failure-14: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: ksgeJx

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request parameter","instance":"/projects/None","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/comments
    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"planningCalendarId": "025af31c-3649-5140-af16-4b78383a5a96", "parentId": null, "viewStyle": "CALENDAR", "order": -401, "isFavorite": true, "color": "\ud849\udf39M\u00f6`\u00ae^\u00e6Ls\uda02\udca5\u0000\u00df\u00f8Z\ud85d\udde4", "name": "else"}' http://taska-backend:8080/projects/None
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/comments
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"planningCalendarId": "025af31c-3649-5140-af16-4b78383a5a96", "parentId": null, "viewStyle": "CALENDAR", "order": -401, "isFavorite": true, "color": "\ud849\udf39M\u00f6`\u00ae^\u00e6Ls\uda02\udca5\u0000\u00df\u00f8Z\ud85d\udde4", "name": "else"}' http://taska-backend:8080/projects/None
````

### failure-15: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: dc5ahn

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request parameter","instance":"/projects/None","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/comments
    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/None
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/comments
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/None
````

### failure-16: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: xRLdzB

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request parameter","instance":"/projects/None","status":400,"title":"Bad Request","type":"about:blank"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?showCompleted=false&label=%C3%A0%C2%8CJ%C2%B7&projectId=a2d6d225-c184-46ad-b24d-5b7589972a8a'
    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/comments
    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/None
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?showCompleted=false&label=%C3%A0%C2%8CJ%C2%B7&projectId=a2d6d225-c184-46ad-b24d-5b7589972a8a'
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/comments
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/None
````

## Raw artifacts

- JUnit: `reports/raw/junit.xml`
- NDJSON: `reports/raw/events.ndjson`
- Schemathesis log: `reports/raw/schemathesis.log`
