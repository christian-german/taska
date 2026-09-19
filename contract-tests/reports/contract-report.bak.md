# OpenAPI contract test report

- Status: **FAILED**
- Generated at: `2026-09-04T22:33:47.029859+00:00`
- Schemathesis exit code: `1`
- OpenAPI schema: `docs/openapi/taska.openapi.yaml`
- API base URL: `http://taska-backend:8080`

## Summary

| Tests | Passed | Failures | Errors | Skipped | Duration |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 52 | 8 | 44 | 0 | 0 | 103.276s |

## Failures

### failure-1: POST /labels

- Test case: `POST /labels`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: Yb278s

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/labels",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/labels","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "order": 2147483646, "isFavorite": false}' http://taska-backend:8080/labels
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "order": 2147483646, "isFavorite": false}' http://taska-backend:8080/labels
````

### failure-2: POST /register-device

- Test case: `POST /register-device`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: XcSpg8

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/register-device",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/register-device","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"token": "\u00e6w\u00bf\ud85e\udfd3\u00d7\u00b6\u00b6\u00ce\u00b1\u009fZ\u0000Wq\u00e7D\u009e\u00c5"}' http://taska-backend:8080/register-device
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"token": "\u00e6w\u00bf\ud85e\udfd3\u00d7\u00b6\u00b6\u00ce\u00b1\u009fZ\u0000Wq\u00e7D\u009e\u00c5"}' http://taska-backend:8080/register-device
````

### failure-3: GET /filters

- Test case: `GET /filters`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: pd2AVB

- Response violates schema (2 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /FilterDto/properties/hasDate:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /FilterDto/properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `[{"id":"bf3d06d6-e4d3-47a5-b9e4-0963ce15ea7c","name":"0","color":"","order":-2147483648,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false},{"id":"7544c2bd-53e5-4da5-8a0b-0d1922f69c2f","name":"0","color":"","order":-2147483647,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false},{"id":"ad591682-63d1-407e-8825-a1f25979f04d","name":"0","color":"","order":0,"isFavorite":false,"projectId":"f4bfd780-a52e-4d04-b8f9-2fa67dc8aeaf","hasDate":false},{"id":"ad67a59e-2e3c-434e-b74a-76062dd1bb37","name":"0","color":"","order":0,"isFavorite":true,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false},{"id":"54dab01a-d63f-41ca-8e2a-fff63a81b60b","name":"0","color":"","order":0,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false},{"id":"38432da2-7871-4fcb-9b71-b2ffdb400c28","name":"00","color":"","order":0,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false},{"id":"c915a5c3-cbf7-4465-9c3e-701c4eb8e82a","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":null},{"id":"12f8f891-68ad-42e0-82b5-5fb788523d07","name":"0","color":"","order":0,"isFavorite":false,"projectId":null,"hasDate":false},{"id":"266e5221-a572-48db-a2d7-0e39ff388f49","name":"0","color":"","order":0,"isFavorite":false,"projectId":null,"hasDate":false},{"id":"fd4686df-d8b7-40e1-bf3f-45e986dd8617","name":"0","color":"","order":0,"isFavorite":false,"projectId":null,"hasDate":false},{"id":"5347676a-0c10-4f8e-a71f-295bc18f363a","name":"0","color":"","order":0,"isFavorite":false,"projectId":null,"hasDate":null},{"id":"ee4e0e1e-c4aa-44f3-8787-c29ad8f21542","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":null},{"id":"3cc6180f-020b-4bcf-ad62-1694d567162d","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":null},{"id":"9737c983-9642-4620-8623-5f253fc7d7c6","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":null},{"id":"ebdb552c-2f50-4633-bfaa-22b8061e4398","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":false},{"id":"9be742c7-0958-4c91-98a6-ead11876af02","name":"0","color":"","order":0,"isFavorite":false,"projectId":null,"hasDate":null},{"id":"fe75c5f6-4f1e-42de-ad73-e600ef43740a","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":null},{"id":"e23ec58f-b61e-43aa-9ec0-e1754b495cf7","name":"0","color":"","order":0,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false},{"id":"c1e3cfe8-72c4-474f-948e-fb9f8bb9d523","name":"0","color":"","order":2147483646,"isFavorite":false,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","hasDate":false}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters
````

### failure-4: GET /projects

- Test case: `GET /projects`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: iUSpWo

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /ProjectDto/properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `[{"id":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","name":"Inbox","color":"#808080","parentId":null,"order":0,"isFavorite":false,"viewStyle":"LIST","isInboxProject":true,"planningCalendarId":"00000000-0000-0000-0000-000000000001","createdAt":"2026-09-04T22:31:35.240040Z","updatedAt":"2026-09-04T22:31:35.240040Z"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects
````

### failure-5: PATCH /projects/reorder

- Test case: `PATCH /projects/reorder`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: jlq78O

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 400.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Invalid request body",
            "instance": "/projects/reorder",
            "status": 400,
            "title": "Bad Request"
        }

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/projects/reorder","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X PATCH -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '[{}]' http://taska-backend:8080/projects/reorder

2. Test Case ID: s45AIp

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects/reorder","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PATCH -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '[{"order": 0}]' http://taska-backend:8080/projects/reorder
````

Reproduction:

````shell
curl -X PATCH -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '[{}]' http://taska-backend:8080/projects/reorder
curl -X PATCH -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '[{"order": 0}]' http://taska-backend:8080/projects/reorder
````

### failure-6: PUT /planning-calendars/{id}

- Test case: `PUT /planning-calendars/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: GHgOWr

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Planning calendar not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Planning calendar not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "rules": [{"dayOfWeek": 0, "startMinute": 0, "endMinute": 2147483646}]}' http://taska-backend:8080/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd

2. Test Case ID: 0k2ufv

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "rules": [{}]}' http://taska-backend:8080/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "rules": [{"dayOfWeek": 0, "startMinute": 0, "endMinute": 2147483646}]}' http://taska-backend:8080/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "rules": [{}]}' http://taska-backend:8080/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-7: POST /planning-calendars

- Test case: `POST /planning-calendars`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: R2Rb2g

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 400.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Invalid availability rule",
            "instance": "/planning-calendars",
            "status": 400,
            "title": "Bad Request"
        }

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Invalid availability rule","instance":"/planning-calendars","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "rules": [{"dayOfWeek": 0, "startMinute": 0, "endMinute": 2147483646}]}' http://taska-backend:8080/planning-calendars
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "rules": [{"dayOfWeek": 0, "startMinute": 0, "endMinute": 2147483646}]}' http://taska-backend:8080/planning-calendars
````

### failure-8: POST /comments

- Test case: `POST /comments`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: gRDNVq

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/comments",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/comments","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"taskId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e", "content": "00"}' http://taska-backend:8080/comments

2. Test Case ID: rqA0ul

- Response violates schema (2 violations)

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/taskId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[201] Created:

    `{"id":"1ed06bee-f6fc-47ad-bdf8-465e8dd78934","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:31:50.026129530Z"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0"}' http://taska-backend:8080/comments
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"taskId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e", "content": "00"}' http://taska-backend:8080/comments
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0"}' http://taska-backend:8080/comments
````

### failure-9: POST /filters

- Test case: `POST /filters`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: dUzqGM

- Response violates schema (2 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 201.

    Schema at /properties/hasDate:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[201] Created:

    `{"id":"c915a5c3-cbf7-4465-9c3e-701c4eb8e82a","name":"0","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":null}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0"}' http://taska-backend:8080/filters
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0"}' http://taska-backend:8080/filters
````

### failure-10: POST /filters

- Test case: `POST /filters`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: HMcMya

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/filters",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/filters","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0084": null, "isFavorite": true, "hasDate": true, "order": -108, "clearProject": false, "name": "\u00e7^\u00e9\u9b46\u00c2\u0095\u00e5\u00d5\u009dQ\u009c\u00b3\u00d2\u00ba", "projectId": "00ca289a-efe9-396d-9d3c-7aca12c25001", "color": "rk\u00b3\u00f8\u00fcx\u00c8\ud9f7\udc5dd\u00ef\u00e6)"}' http://taska-backend:8080/filters
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0084": null, "isFavorite": true, "hasDate": true, "order": -108, "clearProject": false, "name": "\u00e7^\u00e9\u9b46\u00c2\u0095\u00e5\u00d5\u009dQ\u009c\u00b3\u00d2\u00ba", "projectId": "00ca289a-efe9-396d-9d3c-7aca12c25001", "color": "rk\u00b3\u00f8\u00fcx\u00c8\ud9f7\udc5dd\u00ef\u00e6)"}' http://taska-backend:8080/filters
````

### failure-11: POST /projects

- Test case: `POST /projects`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: UZs24j

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/projects",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "clearParent": false, "order": 0, "isFavorite": false, "viewStyle": "CALENDAR", "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects

2. Test Case ID: cekWlb

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[201] Created:

    `{"id":"c087d448-2b81-4243-897b-ec856f818843","name":"0","color":"#808080","parentId":null,"order":0,"isFavorite":false,"viewStyle":"LIST","isInboxProject":false,"planningCalendarId":"00000000-0000-0000-0000-000000000001","createdAt":"2026-09-04T22:31:50.352085303Z","updatedAt":"2026-09-04T22:31:50.352085683Z"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0"}' http://taska-backend:8080/projects
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "clearParent": false, "order": 0, "isFavorite": false, "viewStyle": "CALENDAR", "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0"}' http://taska-backend:8080/projects
````

### failure-12: POST /projects

- Test case: `POST /projects`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: Xjvgl4

- Undocumented HTTP status code

    Received: 404
    Documented: 201, 400, 401, 403, 409, 422, 500

[404] Not Found:

    `{"detail":"Planning calendar not found: 00163c54-f2b3-18f6-b0df-42d6eb192ed9","instance":"/projects","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"isFavorite": false, "parentId": "97df9ab1-b0b2-41f4-8a7f-c69af4910f28", "planningCalendarId": "00163c54-f2b3-18f6-b0df-42d6eb192ed9", "name": "\u0085\u0002\u00ec", "color": "charcoal", "clearParent": true}' http://taska-backend:8080/projects
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"isFavorite": false, "parentId": "97df9ab1-b0b2-41f4-8a7f-c69af4910f28", "planningCalendarId": "00163c54-f2b3-18f6-b0df-42d6eb192ed9", "name": "\u0085\u0002\u00ec", "color": "charcoal", "clearParent": true}' http://taska-backend:8080/projects
````

### failure-13: POST /sections

- Test case: `POST /sections`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: UyegvM

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/sections",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/sections","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"__main__": {}, "\ud83d\udc4d\ud83c\udffb": {"\udac4\ude7a\u00c8@+\u0006\u00feQ\u00ef\ud828\ude71\u00bc\ud847\uded6\ud907\udefa\ud825\udd18>\t\uda6a\udf0c\uc85a\u00d5&\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": "\u0006\udaff\udc09", "": 8957}, "!": [[-19649250, -141], [false, true], true], "\u009e\u0006\r\u00bdE\u00aeL:\udbf5\udf78\u00bf\u001a\udb80\ude05": {"q*\u0091\u0003;": -2095, "\u00ab": null, "\u0083w\u00f1\u008c\udb22\udca0\u00d6\u0086\u0001g": false}, "": true, "name": "\u00f8\ud92f\udff8", "projectId": "f10cc4f3-ae39-289a-81cf-e9d7627bffe1"}' http://taska-backend:8080/sections
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"__main__": {}, "\ud83d\udc4d\ud83c\udffb": {"\udac4\ude7a\u00c8@+\u0006\u00feQ\u00ef\ud828\ude71\u00bc\ud847\uded6\ud907\udefa\ud825\udd18>\t\uda6a\udf0c\uc85a\u00d5&\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": "\u0006\udaff\udc09", "": 8957}, "!": [[-19649250, -141], [false, true], true], "\u009e\u0006\r\u00bdE\u00aeL:\udbf5\udf78\u00bf\u001a\udb80\ude05": {"q*\u0091\u0003;": -2095, "\u00ab": null, "\u0083w\u00f1\u008c\udb22\udca0\u00d6\u0086\u0001g": false}, "": true, "name": "\u00f8\ud92f\udff8", "projectId": "f10cc4f3-ae39-289a-81cf-e9d7627bffe1"}' http://taska-backend:8080/sections
````

### failure-14: POST /tasks

- Test case: `POST /tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: LIqUBB

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 400.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Validation failed",
            "errors": {
                "estimateMinutes": "must be greater than 0"
            },
            "instance": "/tasks",
            "status": 400,
            "title": "Bad Request"
        }

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Validation failed","instance":"/tasks","status":400,"title":"Bad Request","errors":{"estimateMinutes":"must be greater than 0"}}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "description": "", "projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1", "sectionId": "6082d6f5-584d-4de0-83c0-b80f9d6fffc0", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 0, "mentionContext": "", "recurrenceRule": "", "scope": "THIS_ONLY", "occurrenceScheduledAt": "2000-01-01T00:00:00Z", "type": "APPOINTMENT"}' http://taska-backend:8080/tasks

2. Test Case ID: cX3fxW

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "description": "", "projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1", "sectionId": "6082d6f5-584d-4de0-83c0-b80f9d6fffc0", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 2147483646, "mentionContext": "", "recurrenceRule": "", "scope": "THIS_ONLY", "occurrenceScheduledAt": "2000-01-01T00:00:00Z", "type": "TODO"}' http://taska-backend:8080/tasks

3. Test Case ID: Iq8Y0d

- Response violates schema (14 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 201.

    Schema at /properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 201.

    Schema at /properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 201.

    Schema at /properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[201] Created:

    `{"id":"0f3762eb-4c6d-463a-aefa-a713eca01994","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.722864405Z","updatedAt":"2026-09-04T22:31:50.722864805Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0"}' http://taska-backend:8080/tasks
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "description": "", "projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1", "sectionId": "6082d6f5-584d-4de0-83c0-b80f9d6fffc0", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 0, "mentionContext": "", "recurrenceRule": "", "scope": "THIS_ONLY", "occurrenceScheduledAt": "2000-01-01T00:00:00Z", "type": "APPOINTMENT"}' http://taska-backend:8080/tasks
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "description": "", "projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1", "sectionId": "6082d6f5-584d-4de0-83c0-b80f9d6fffc0", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 2147483646, "mentionContext": "", "recurrenceRule": "", "scope": "THIS_ONLY", "occurrenceScheduledAt": "2000-01-01T00:00:00Z", "type": "TODO"}' http://taska-backend:8080/tasks
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0"}' http://taska-backend:8080/tasks
````

### failure-15: POST /tasks

- Test case: `POST /tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: pHlGx2

- Undocumented HTTP status code

    Received: 404
    Documented: 201, 400, 401, 403, 409, 422, 500

[404] Not Found:

    `{"detail":"Project not found: 41580073-0cf4-5fad-8710-77d5bfb20d19","instance":"/tasks","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u00e4": {}, "\u00aa6\ud865\udfc0\u0085": {"F\udb7c\udfec\u00ed\u00d6z": 10140201941347, "\u00b8\u009b\ud9bc\udf40o\ud8fb\udea7~iI6": {}, "$v\u0098": []}, "\u0012\u00e2\udb20\udd04\u0087\udbb3\udd0cz\u0007\u001f": -5.336723905777826e+205, "\u0017/": {"": [], "O\u0095}\u00844\ud869\udf79\uda5b\udee2\u0095\u00bb": {"\ud9b7\udfef\u009d\u0006\ud898\udf80\u00f7\udb8c\udebeo_\u0006": 100000000000000001}, "\u00e2\udb44\uddfa\u00fe\u00da": []}, "scheduledAt": "2026-09-04T22:32:04.958244484Z", "estimateMinutes": 1130864751, "parentId": "61a4806b-71df-5d9b-a1ee-19cb184383b3", "content": "\u8d09", "mentionContext": "i", "projectId": "41580073-0cf4-5fad-8710-77d5bfb20d19"}' http://taska-backend:8080/tasks
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u00e4": {}, "\u00aa6\ud865\udfc0\u0085": {"F\udb7c\udfec\u00ed\u00d6z": 10140201941347, "\u00b8\u009b\ud9bc\udf40o\ud8fb\udea7~iI6": {}, "$v\u0098": []}, "\u0012\u00e2\udb20\udd04\u0087\udbb3\udd0cz\u0007\u001f": -5.336723905777826e+205, "\u0017/": {"": [], "O\u0095}\u00844\ud869\udf79\uda5b\udee2\u0095\u00bb": {"\ud9b7\udfef\u009d\u0006\ud898\udf80\u00f7\udb8c\udebeo_\u0006": 100000000000000001}, "\u00e2\udb44\uddfa\u00fe\u00da": []}, "scheduledAt": "2026-09-04T22:32:04.958244484Z", "estimateMinutes": 1130864751, "parentId": "61a4806b-71df-5d9b-a1ee-19cb184383b3", "content": "\u8d09", "mentionContext": "i", "projectId": "41580073-0cf4-5fad-8710-77d5bfb20d19"}' http://taska-backend:8080/tasks
````

### failure-16: POST /tasks/{taskId}/close

- Test case: `POST /tasks/{taskId}/close`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: RfJjjW

- Response violates schema (13 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"0f3762eb-4c6d-463a-aefa-a713eca01994","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":true,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.722864Z","updatedAt":"2026-09-04T22:31:51.069719710Z","completedAt":"2026-09-04T22:31:51.067092309Z","instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/0f3762eb-4c6d-463a-aefa-a713eca01994/close
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/0f3762eb-4c6d-463a-aefa-a713eca01994/close
````

### failure-17: POST /tasks/{taskId}/close

- Test case: `POST /tasks/{taskId}/close`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: GXZNaV

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Task not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Task not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close

2. Test Case ID: 2vPC51

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"occurrenceScheduledAt is required to complete a recurring occurrence","instance":"/tasks/addd3b11-f3d8-465d-af8a-eb255ca4a0ae/close","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/addd3b11-f3d8-465d-af8a-eb255ca4a0ae/close
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/close
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/addd3b11-f3d8-465d-af8a-eb255ca4a0ae/close
````

### failure-18: POST /tasks/{taskId}/reopen

- Test case: `POST /tasks/{taskId}/reopen`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: z93aXy

- Response violates schema (12 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"577c4cef-b325-4b4b-90ca-b6134f5b7382","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":"2000-01-01T00:00:00Z","allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.842848Z","updatedAt":"2026-09-04T22:31:50.842849Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/577c4cef-b325-4b4b-90ca-b6134f5b7382/reopen
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/577c4cef-b325-4b4b-90ca-b6134f5b7382/reopen
````

### failure-19: POST /tasks/{taskId}/reopen

- Test case: `POST /tasks/{taskId}/reopen`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: l7m2Qq

- Response violates schema (2 violations)

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

[200] OK:

    `{"id":"2d9e305c-fadc-4694-a700-0a4bdbf466ec","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.313497Z","updatedAt":"2026-09-04T22:32:07.923418Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/2d9e305c-fadc-4694-a700-0a4bdbf466ec/reopen

2. Test Case ID: o6NabS

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Task not found: 7b2929fe-c890-585e-bf62-1a6cd173cd97",
            "instance": "/tasks/7b2929fe-c890-585e-bf62-1a6cd173cd97/reopen",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Task not found: 7b2929fe-c890-585e-bf62-1a6cd173cd97","instance":"/tasks/7b2929fe-c890-585e-bf62-1a6cd173cd97/reopen","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"wQ\ud907\udefa\ud825\udd18>\t\uda6a\udf0c\uc85a\u00d5&\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": {}, "occurrenceScheduledAt": "2026-09-04T22:32:09.409072623Z"}' http://taska-backend:8080/tasks/7b2929fe-c890-585e-bf62-1a6cd173cd97/reopen
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/2d9e305c-fadc-4694-a700-0a4bdbf466ec/reopen
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"wQ\ud907\udefa\ud825\udd18>\t\uda6a\udf0c\uc85a\u00d5&\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": {}, "occurrenceScheduledAt": "2026-09-04T22:32:09.409072623Z"}' http://taska-backend:8080/tasks/7b2929fe-c890-585e-bf62-1a6cd173cd97/reopen
````

### failure-20: POST /time-entries

- Test case: `POST /time-entries`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 1CXBFG

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/time-entries",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/time-entries","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/time-entries
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/time-entries
````

### failure-21: POST /time-entries

- Test case: `POST /time-entries`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: gBkQim

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 201.

    Schema at /properties/notes:

        {
            "type": "string"
        }

    Value:

        null

[201] Created:

    `{"id":"274c844f-08a8-497a-8d15-9855f2ad8024","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-03T05:14:14.708Z","projectId":"cde9395c-cbb1-4fa8-8c47-26ed41f1d24a","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.427576525Z","updatedAt":"2026-09-04T22:32:11.427576675Z"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud98b\ude92\ud836\udeae\u0010\u00db\ud868\uddca": [], "w\ud850\ude6b}+\u0006\u00feQ\u00ef\ud828\ude71\u00bc\ud847\uded6\ud907\udefa\ud825\udd18>\t\uda6a\udf0c\uc85a\u00d5&\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": {}, "projectId": "cde9395c-cbb1-4fa8-8c47-26ed41f1d24a", "startAt": "2026-09-04T22:32:05.343872Z", "description": "", "endAt": "7268-04-02T17:01:14.708-12:13"}' http://taska-backend:8080/time-entries
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud98b\ude92\ud836\udeae\u0010\u00db\ud868\uddca": [], "w\ud850\ude6b}+\u0006\u00feQ\u00ef\ud828\ude71\u00bc\ud847\uded6\ud907\udefa\ud825\udd18>\t\uda6a\udf0c\uc85a\u00d5&\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": {}, "projectId": "cde9395c-cbb1-4fa8-8c47-26ed41f1d24a", "startAt": "2026-09-04T22:32:05.343872Z", "description": "", "endAt": "7268-04-02T17:01:14.708-12:13"}' http://taska-backend:8080/time-entries
````

### failure-22: PUT /comments/{id}

- Test case: `PUT /comments/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: phAQGk

- Response violates schema (2 violations)

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/taskId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"1ed06bee-f6fc-47ad-bdf8-465e8dd78934","taskId":null,"projectId":null,"content":"00","createdAt":"2026-09-04T22:31:50.026130Z"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"taskId": "2b26c2a9-2382-4f4c-b420-b67328261686", "projectId": "252b32c3-ea08-4d9f-b550-ecc4309f7f35", "content": "00"}' http://taska-backend:8080/comments/1ed06bee-f6fc-47ad-bdf8-465e8dd78934
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"taskId": "2b26c2a9-2382-4f4c-b420-b67328261686", "projectId": "252b32c3-ea08-4d9f-b550-ecc4309f7f35", "content": "00"}' http://taska-backend:8080/comments/1ed06bee-f6fc-47ad-bdf8-465e8dd78934
````

### failure-23: PUT /comments/{id}

- Test case: `PUT /comments/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: sUpEon

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Comment not found: 6347afc2-8444-5e8e-8f6d-a0d8ed54c5d8",
            "instance": "/comments/6347afc2-8444-5e8e-8f6d-a0d8ed54c5d8",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Comment not found: 6347afc2-8444-5e8e-8f6d-a0d8ed54c5d8","instance":"/comments/6347afc2-8444-5e8e-8f6d-a0d8ed54c5d8","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"e\u0096\u00f8\u00ef\u0003\ud83f\udf3a'"'"'\udae7\udc70\ud856\uddbc": {}, "\uda90\udea8\udab6\udf86/*\u00ef\udb2c\udf3d\uda39\udffc": {"\u0001\u00aa": "", "": -8.024371483008267e+63, "\u00a8\u008d": true}, "content": "00"}' http://taska-backend:8080/comments/6347afc2-8444-5e8e-8f6d-a0d8ed54c5d8
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"e\u0096\u00f8\u00ef\u0003\ud83f\udf3a'"'"'\udae7\udc70\ud856\uddbc": {}, "\uda90\udea8\udab6\udf86/*\u00ef\udb2c\udf3d\uda39\udffc": {"\u0001\u00aa": "", "": -8.024371483008267e+63, "\u00a8\u008d": true}, "content": "00"}' http://taska-backend:8080/comments/6347afc2-8444-5e8e-8f6d-a0d8ed54c5d8
````

### failure-24: PUT /filters/{id}

- Test case: `PUT /filters/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: fxHN6B

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"ad591682-63d1-407e-8825-a1f25979f04d","name":"0","color":"","order":0,"isFavorite":false,"projectId":null,"hasDate":false}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "order": 0, "isFavorite": false, "projectId": "f4bfd780-a52e-4d04-b8f9-2fa67dc8aeaf", "clearProject": true, "hasDate": false}' http://taska-backend:8080/filters/ad591682-63d1-407e-8825-a1f25979f04d
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "order": 0, "isFavorite": false, "projectId": "f4bfd780-a52e-4d04-b8f9-2fa67dc8aeaf", "clearProject": true, "hasDate": false}' http://taska-backend:8080/filters/ad591682-63d1-407e-8825-a1f25979f04d
````

### failure-25: PUT /filters/{id}

- Test case: `PUT /filters/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: ulJ9Zc

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/filters/a3d5fa7b-93c0-4b95-9c98-ccd5ae21ad36",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/filters/a3d5fa7b-93c0-4b95-9c98-ccd5ae21ad36","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0080@\u00bd": {}, "\ud9bf\udf8f\u00fe|D\u009e\u00c5": [], "clearProject": false, "order": 1620417, "hasDate": false, "name": "<", "color": "", "isFavorite": true, "projectId": "d95bbcc6-f8c5-5f7e-b3c0-cf2f0023430d"}' http://taska-backend:8080/filters/a3d5fa7b-93c0-4b95-9c98-ccd5ae21ad36

2. Test Case ID: NkH71C

- Response violates schema

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /properties/hasDate:

        {
            "type": "boolean"
        }

    Value:

        null

[200] OK:

    `{"id":"6afc1021-e345-45b5-8dcc-b259c2d13cf5","name":"f©ÿ\r§©","color":"charcoal","order":0,"isFavorite":false,"projectId":"d84a0fe6-c520-426e-a9f2-a948a801711f","hasDate":null}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": {"i\u00c0\u00ec\u0006\u00b1\u000f\u00c0": {";}\uda6e\udc0eOe\udbfe\ude32%\u00das\u00c5": -5536567417596487, "": []}, "": [], "I0\u0085\u000e": true}, "\u00e9": [[[-5.50945865043582e-214, null]], 24013793, {}], "\ud814\uddf6\u00f32": null, "__proto__": false, "\u0005\u00ce\u00f4\udbb5\udf02\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": {}, "isFavorite": false, "name": "f\u00a9\u00ff\r\u00a7\u00a9\uedb8"}' http://taska-backend:8080/filters/6afc1021-e345-45b5-8dcc-b259c2d13cf5
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0080@\u00bd": {}, "\ud9bf\udf8f\u00fe|D\u009e\u00c5": [], "clearProject": false, "order": 1620417, "hasDate": false, "name": "<", "color": "", "isFavorite": true, "projectId": "d95bbcc6-f8c5-5f7e-b3c0-cf2f0023430d"}' http://taska-backend:8080/filters/a3d5fa7b-93c0-4b95-9c98-ccd5ae21ad36
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": {"i\u00c0\u00ec\u0006\u00b1\u000f\u00c0": {";}\uda6e\udc0eOe\udbfe\ude32%\u00das\u00c5": -5536567417596487, "": []}, "": [], "I0\u0085\u000e": true}, "\u00e9": [[[-5.50945865043582e-214, null]], 24013793, {}], "\ud814\uddf6\u00f32": null, "__proto__": false, "\u0005\u00ce\u00f4\udbb5\udf02\u009b\u478bI\uda96\udcc3(\u00c0\u001d\udb8a\udef7": {}, "isFavorite": false, "name": "f\u00a9\u00ff\r\u00a7\u00a9\uedb8"}' http://taska-backend:8080/filters/6afc1021-e345-45b5-8dcc-b259c2d13cf5
````

### failure-26: PUT /labels/{id}

- Test case: `PUT /labels/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: GtwZv5

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/labels/a2caba97-6b93-47d5-8efa-662c50bbce4b",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/labels/a2caba97-6b93-47d5-8efa-662c50bbce4b","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "00", "color": "", "order": 0, "isFavorite": false}' http://taska-backend:8080/labels/a2caba97-6b93-47d5-8efa-662c50bbce4b
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "00", "color": "", "order": 0, "isFavorite": false}' http://taska-backend:8080/labels/a2caba97-6b93-47d5-8efa-662c50bbce4b
````

### failure-27: PUT /projects/{id}

- Test case: `PUT /projects/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: ZCYCqM

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/projects/d84a0fe6-c520-426e-a9f2-a948a801711f",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects/d84a0fe6-c520-426e-a9f2-a948a801711f","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "clearParent": false, "order": 0, "isFavorite": false, "viewStyle": "CALENDAR", "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects/d84a0fe6-c520-426e-a9f2-a948a801711f

2. Test Case ID: KfADuF

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"d84a0fe6-c520-426e-a9f2-a948a801711f","name":"0","color":"","parentId":null,"order":0,"isFavorite":false,"viewStyle":"LIST","isInboxProject":false,"planningCalendarId":"00000000-0000-0000-0000-000000000001","createdAt":"2026-09-04T22:31:50.429882Z","updatedAt":"2026-09-04T22:31:51.516496149Z"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "clearParent": true, "order": 0, "isFavorite": false, "viewStyle": "LIST", "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects/d84a0fe6-c520-426e-a9f2-a948a801711f
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "clearParent": false, "order": 0, "isFavorite": false, "viewStyle": "CALENDAR", "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects/d84a0fe6-c520-426e-a9f2-a948a801711f
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "0", "color": "", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "clearParent": true, "order": 0, "isFavorite": false, "viewStyle": "LIST", "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects/d84a0fe6-c520-426e-a9f2-a948a801711f
````

### failure-28: PUT /sections/{id}

- Test case: `PUT /sections/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: dQyivK

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Section not found: bebb047d-444d-4366-bd22-79192b55851c",
            "instance": "/sections/bebb047d-444d-4366-bd22-79192b55851c",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Section not found: bebb047d-444d-4366-bd22-79192b55851c","instance":"/sections/bebb047d-444d-4366-bd22-79192b55851c","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u00b9T\ub342\ud803\udd2a\u00fb\ud811\udee7\u00f3\u0014\ud929\udefc\u0000s\u00e4": [null, false, -2.577003073398037e+59], "\u00c6\u00af": [], "": {"": "v\u00ebdq\u001cv\ud930\ude68", "\u00df\u00e0\uda04\udd89\u00066\u00fe\u0018\uda5d\ude4ed\u00a1p\u00d0\u00f6\u00b4\u00b1x\u0093\udb64\udc35\u00e5\u00f5\u0012": [true, 5.703471885263189e+16, null]}, "?\u009e\u0017": {"\ud831\udc62\ud968\udda1\u00a2\u00c0": [], "\u00d5\u00fb\u00d9": "XC\u007f\u0087C\u000f\u008d\u00ec\u00c0\u00f9\u0007\u00ff\u001fm\u00f5l\udbd8\udde9", "\u00f8\u00d6ze": {}}, "\u00e9\uda55\uded8": [-0.99999], "projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1", "name": "0"}' http://taska-backend:8080/sections/bebb047d-444d-4366-bd22-79192b55851c
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u00b9T\ub342\ud803\udd2a\u00fb\ud811\udee7\u00f3\u0014\ud929\udefc\u0000s\u00e4": [null, false, -2.577003073398037e+59], "\u00c6\u00af": [], "": {"": "v\u00ebdq\u001cv\ud930\ude68", "\u00df\u00e0\uda04\udd89\u00066\u00fe\u0018\uda5d\ude4ed\u00a1p\u00d0\u00f6\u00b4\u00b1x\u0093\udb64\udc35\u00e5\u00f5\u0012": [true, 5.703471885263189e+16, null]}, "?\u009e\u0017": {"\ud831\udc62\ud968\udda1\u00a2\u00c0": [], "\u00d5\u00fb\u00d9": "XC\u007f\u0087C\u000f\u008d\u00ec\u00c0\u00f9\u0007\u00ff\u001fm\u00f5l\udbd8\udde9", "\u00f8\u00d6ze": {}}, "\u00e9\uda55\uded8": [-0.99999], "projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1", "name": "0"}' http://taska-backend:8080/sections/bebb047d-444d-4366-bd22-79192b55851c
````

### failure-29: PUT /tasks/{id}

- Test case: `PUT /tasks/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: cNAMnS

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "type": "TODO", "description": "", "projectId": "cde9395c-cbb1-4fa8-8c47-26ed41f1d24a", "sectionId": "debe2835-04d8-468a-b1c4-8ad67e74226b", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 2147483646, "mentionContext": "", "recurrenceRule": ""}' http://taska-backend:8080/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "type": "TODO", "description": "", "projectId": "cde9395c-cbb1-4fa8-8c47-26ed41f1d24a", "sectionId": "debe2835-04d8-468a-b1c4-8ad67e74226b", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 2147483646, "mentionContext": "", "recurrenceRule": ""}' http://taska-backend:8080/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde
````

### failure-30: PUT /tasks/{id}

- Test case: `PUT /tasks/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: Xudomn

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx
    Hint: The request body contains 5 additional properties not defined in the schema (``, `
    `, `¢À` and 2 more). The server likely rejects unexpected fields. Add `additionalProperties: false` to your schema to prevent this.

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/tasks/8fbfc000-16ce-3b5b-9cec-7edcd8d66a17","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\n": 3240, "": [{"\u0003\u00a6\u009f\uda47\udf937": 7662648, "\u00a0\udb33\udfbedq\u001cv\ud930\ude68": "\u00ca\u810d\u00a9\u00066\u00fe\u0018\uda5d\ude4ed\u00a1p\u00d0\u00f6\u00b4\u00b1x\u0093\udb64\udc35\u00e5\u00f5\u0012", "\u0087Q\\\ud928\ude54\u00a3,y?\u009e\u0017": "c"}], "\u00a2\u00c0": {}, "\u00d5\u00fb\u00d9": "XC\u007f\u0087C\u000f\u008d\u00ec\u00c0\u00f9\u0007\u00ff\u001fm\u00f5l\udbd8\udde9", "\u00f8\u00d6ze": {}, "mentionContext": "\u00b1\u00af", "scheduledAt": "2026-09-04T22:32:12.395158628Z", "allDay": true, "dueAt": "0293-12-07T14:51:11+22:17", "description": "", "type": "APPOINTMENT", "content": "Y\u00c0\ud93c\udc79,\u00cb'"'"'\u008a\u001d", "recurrenceRule": "}\u00e3,\ud863\uddcb.G", "priority": 1, "order": -59434397, "estimateMinutes": 2795, "sectionId": "7ffa6a77-561c-1805-a86c-522c412d9a82", "projectId": "b4b15a00-957c-3b96-abd2-c21a65b7a33c", "labels": ["\u0099\u0094$\u001ea\u0003B\u00c2"], "isRecurring": false, "parentId": "f889b1c2-34d6-5b15-853d-75515125aaed"}' http://taska-backend:8080/tasks/8fbfc000-16ce-3b5b-9cec-7edcd8d66a17
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\n": 3240, "": [{"\u0003\u00a6\u009f\uda47\udf937": 7662648, "\u00a0\udb33\udfbedq\u001cv\ud930\ude68": "\u00ca\u810d\u00a9\u00066\u00fe\u0018\uda5d\ude4ed\u00a1p\u00d0\u00f6\u00b4\u00b1x\u0093\udb64\udc35\u00e5\u00f5\u0012", "\u0087Q\\\ud928\ude54\u00a3,y?\u009e\u0017": "c"}], "\u00a2\u00c0": {}, "\u00d5\u00fb\u00d9": "XC\u007f\u0087C\u000f\u008d\u00ec\u00c0\u00f9\u0007\u00ff\u001fm\u00f5l\udbd8\udde9", "\u00f8\u00d6ze": {}, "mentionContext": "\u00b1\u00af", "scheduledAt": "2026-09-04T22:32:12.395158628Z", "allDay": true, "dueAt": "0293-12-07T14:51:11+22:17", "description": "", "type": "APPOINTMENT", "content": "Y\u00c0\ud93c\udc79,\u00cb'"'"'\u008a\u001d", "recurrenceRule": "}\u00e3,\ud863\uddcb.G", "priority": 1, "order": -59434397, "estimateMinutes": 2795, "sectionId": "7ffa6a77-561c-1805-a86c-522c412d9a82", "projectId": "b4b15a00-957c-3b96-abd2-c21a65b7a33c", "labels": ["\u0099\u0094$\u001ea\u0003B\u00c2"], "isRecurring": false, "parentId": "f889b1c2-34d6-5b15-853d-75515125aaed"}' http://taska-backend:8080/tasks/8fbfc000-16ce-3b5b-9cec-7edcd8d66a17
````

### failure-31: PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}

- Test case: `PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: PWWBa8

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 400.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Occurrence replacement requires a recurring task",
            "instance": "/tasks/8ebf088b-3bf1-4f7e-a1d2-4efa4591e71f/occurrences/2000-01-01T00%3A00%3A00Z",
            "status": 400,
            "title": "Bad Request"
        }

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Occurrence replacement requires a recurring task","instance":"/tasks/8ebf088b-3bf1-4f7e-a1d2-4efa4591e71f/occurrences/2000-01-01T00%3A00%3A00Z","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"title": "0", "priority": 3, "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z"}' http://taska-backend:8080/tasks/8ebf088b-3bf1-4f7e-a1d2-4efa4591e71f/occurrences/2000-01-01T00%3A00%3A00Z
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"title": "0", "priority": 3, "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z"}' http://taska-backend:8080/tasks/8ebf088b-3bf1-4f7e-a1d2-4efa4591e71f/occurrences/2000-01-01T00%3A00%3A00Z
````

### failure-32: PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}/following

- Test case: `PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}/following`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: yNdPzB

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 400.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Following-series replacement requires a recurring task",
            "instance": "/tasks/ef96bb41-9c58-421e-8004-4c161a45b6c4/occurrences/2000-01-01T00%3A00%3A00Z/following",
            "status": 400,
            "title": "Bad Request"
        }

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Following-series replacement requires a recurring task","instance":"/tasks/ef96bb41-9c58-421e-8004-4c161a45b6c4/occurrences/2000-01-01T00%3A00%3A00Z/following","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "type": "TODO", "description": "", "projectId": "64739609-cdb9-4f86-bb86-18dc4f1e9654", "sectionId": "dab0e39d-e03c-440b-a7a3-2c3fecc9c408", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 2147483646, "mentionContext": "", "recurrenceRule": ""}' http://taska-backend:8080/tasks/ef96bb41-9c58-421e-8004-4c161a45b6c4/occurrences/2000-01-01T00%3A00%3A00Z/following
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"content": "0", "type": "TODO", "description": "", "projectId": "64739609-cdb9-4f86-bb86-18dc4f1e9654", "sectionId": "dab0e39d-e03c-440b-a7a3-2c3fecc9c408", "parentId": "e3e70682-c209-1cac-a29f-6fbed82c07cd", "order": 0, "priority": 1, "labels": [], "scheduledAt": "2000-01-01T00:00:00Z", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "isRecurring": false, "estimateMinutes": 2147483646, "mentionContext": "", "recurrenceRule": ""}' http://taska-backend:8080/tasks/ef96bb41-9c58-421e-8004-4c161a45b6c4/occurrences/2000-01-01T00%3A00%3A00Z/following
````

### failure-33: PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}/following

- Test case: `PUT /tasks/{id}/occurrences/{occurrenceScheduledAt}/following`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: GMnVtV

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks/577c4cef-b325-4b4b-90ca-b6134f5b7382/occurrences/0074-08-24T05%3A47%3A05.5-23%3A55/following","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"p\u00c2\u00c6\u00ae\u00b3x\udb22\uddd8:\u008e'"'"'": [], "order": 0, "estimateMinutes": 19134, "parentId": "31bdf4b6-d7ad-59d0-9eeb-790cfc0bb292", "sectionId": "c215fd3c-189d-4ccd-8b6c-dc6f05f6f9fe", "recurrenceRule": "}\u00e3,\ud863\uddcb.G", "projectId": "68810589-9201-1b45-a3e3-d304300f8919", "scheduledAt": "2026-09-04T22:32:03.867844Z", "type": "TODO", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "description": "", "labels": [], "content": "\u0094\u0013;\udb6a\udc48a", "isRecurring": false, "priority": 1, "mentionContext": ""}' http://taska-backend:8080/tasks/577c4cef-b325-4b4b-90ca-b6134f5b7382/occurrences/0074-08-24T05%3A47%3A05.5-23%3A55/following
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"p\u00c2\u00c6\u00ae\u00b3x\udb22\uddd8:\u008e'"'"'": [], "order": 0, "estimateMinutes": 19134, "parentId": "31bdf4b6-d7ad-59d0-9eeb-790cfc0bb292", "sectionId": "c215fd3c-189d-4ccd-8b6c-dc6f05f6f9fe", "recurrenceRule": "}\u00e3,\ud863\uddcb.G", "projectId": "68810589-9201-1b45-a3e3-d304300f8919", "scheduledAt": "2026-09-04T22:32:03.867844Z", "type": "TODO", "dueAt": "2000-01-01T00:00:00Z", "allDay": false, "description": "", "labels": [], "content": "\u0094\u0013;\udb6a\udc48a", "isRecurring": false, "priority": 1, "mentionContext": ""}' http://taska-backend:8080/tasks/577c4cef-b325-4b4b-90ca-b6134f5b7382/occurrences/0074-08-24T05%3A47%3A05.5-23%3A55/following
````

### failure-34: PUT /time-entries/{id}

- Test case: `PUT /time-entries/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 0wLiTm

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "TimeEntry not found: 1ed06bee-f6fc-47ad-bdf8-465e8dd78934",
            "instance": "/time-entries/1ed06bee-f6fc-47ad-bdf8-465e8dd78934",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"TimeEntry not found: 1ed06bee-f6fc-47ad-bdf8-465e8dd78934","instance":"/time-entries/1ed06bee-f6fc-47ad-bdf8-465e8dd78934","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/time-entries/1ed06bee-f6fc-47ad-bdf8-465e8dd78934
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/time-entries/1ed06bee-f6fc-47ad-bdf8-465e8dd78934
````

### failure-35: PUT /time-entries/{id}

- Test case: `PUT /time-entries/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: IN1fJm

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/time-entries/cb89ff0f-2a63-457b-8257-9b64441e725e","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"projectId": "68810589-9201-1b45-a3e3-d304300f8919"}' http://taska-backend:8080/time-entries/cb89ff0f-2a63-457b-8257-9b64441e725e

2. Test Case ID: gHNp8m

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/notes:

        {
            "type": "string"
        }

    Value:

        null

[200] OK:

    `{"id":"64fb8b64-508e-4b78-890d-29c4a80ab898","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.548926Z","updatedAt":"2026-09-04T22:32:24.595720013Z"}`

Reproduce with:

    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1"}' http://taska-backend:8080/time-entries/64fb8b64-508e-4b78-890d-29c4a80ab898
````

Reproduction:

````shell
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"projectId": "68810589-9201-1b45-a3e3-d304300f8919"}' http://taska-backend:8080/time-entries/cb89ff0f-2a63-457b-8257-9b64441e725e
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"projectId": "50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1"}' http://taska-backend:8080/time-entries/64fb8b64-508e-4b78-890d-29c4a80ab898
````

### failure-36: GET /comments

- Test case: `GET /comments`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: vJnr3c

- Response violates schema (2 violations)

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /CommentDto/properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /CommentDto/properties/taskId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `[{"id":"d2b0c332-18b7-47a1-846a-835d01ab3322","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:31:59.677237Z"},{"id":"705b672a-322a-4bad-b59c-0fc852c727da","taskId":null,"projectId":null,"content":"񮃬§÷񡻔§","createdAt":"2026-09-04T22:31:59.689523Z"},{"id":"7ea8e822-20c0-4d87-8ef1-20dd18bbf9d8","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:31:59.698727Z"},{"id":"1da1f4d3-e7f9-4727-acf6-a68020fda693","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:31:59.713328Z"},{"id":"6814695f-55aa-4ddf-9385-6f6598d0604a","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:31:59.939074Z"},{"id":"85e3920c-b0b8-438f-b7ad-cd00f71f7acb","taskId":null,"projectId":null,"content":"00","createdAt":"2026-09-04T22:31:59.961349Z"},{"id":"11f5fb4d-059c-483f-95e0-f7763e80e4b4","taskId":null,"projectId":null,"content":"00","createdAt":"2026-09-04T22:31:59.970982Z"},{"id":"05d01f7a-db3b-45eb-b40c-567a75e88295","taskId":null,"projectId":null,"content":"󛾑°r򺷿𝩮𚜛Ûfo\"","createdAt":"2026-09-04T22:32:00.113230Z"},{"id":"aaf91799-7d8f-43c5-905c-ad026f6ce25d","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.123738Z"},{"id":"75828296-78e3-45ef-a2cd-549b525672bf","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.132653Z"},{"id":"be8685da-ea58-4f50-9d5a-a4e6329801ae","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.336448Z"},{"id":"67e549dc-5333-483a-96a7-f126e3ba0135","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:32:00.585167Z"},{"id":"e99b1221-feaf-4e3f-a9c1-7355c94c5047","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.724784Z"},{"id":"b2ddc608-42d6-406f-8a00-b2ca9938f8a1","taskId":null,"projectId":null,"content":"00","createdAt":"2026-09-04T22:32:00.765526Z"},{"id":"5bfdd2ee-7452-4e5b-9f82-c00d88666983","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:32:00.778609Z"},{"id":"edc6c4b8-b6ef-48e1-b79a-48139aea00eb","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:32:00.919263Z"},{"id":"f9827630-a390-45c9-b3e2-1ecfb4cccbd5","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:32:00.936898Z"},{"id":"1eda5351-d72d-434d-8b15-544facb34581","taskId":null,"projectId":null,"content":"00","createdAt":"2026-09-04T22:32:00.967330Z"},{"id":"508a77e8-dfef-4ef0-9357-7bf321e942a1","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:32:00.980731Z"},{"id":"f9f5ad5e-9f2c-4efe-b148-a1d9e1ff2a0f","taskId":null,"projectId":null,"content":"øï\u0003🼺'󉱰𥦼","createdAt":"2026-09-04T22:32:01.096350Z"},{"id":"b6c067e5-5422-45b5-81d4-793eaa677001","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:01.121992Z"},{"id":"2c4e223f-be7f-4e4a-a881-baef95a5abd7","taskId":null,"projectId":null,"content":"򀉢­'[","createdAt":"2026-09-04T22:31:59.798434Z"},{"id":"c83a7f0c-ba8c-4c27-b696-fc3462199173","taskId":null,"projectId":null,"content":"󛾑°r򺷿𝩮𚜛Ûfo\"","createdAt":"2026-09-04T22:32:00.602598Z"},{"id":"2a3adc01-98e1-4869-b76c-6138055dc8d1","taskId":null,"projectId":null,"content":"","createdAt":"2026-09-04T22:32:00.371859Z"},{"id":"25388d33-3176-496a-978b-ab12b9f79c9e","taskId":null,"projectId":null,"content":"¿[񖥲𡊣\u0016Ç\u0015󩚿򽕨","createdAt":"2026-09-04T22:32:00.739405Z"},{"id":"2d0d3456-90a0-44e1-81cb-1698a9a3feaa","taskId":null,"projectId":null,"content":"񮃬§÷񡻔§","createdAt":"2026-09-04T22:32:00.751298Z"},{"id":"09231926-5535-49a7-bd39-2609db07f24e","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.345484Z"},{"id":"4fe0410d-986c-4c70-89b0-2643027987bb","taskId":null,"projectId":null,"content":"5{G𧏊9,Xoê·1","createdAt":"2026-09-04T22:32:00.907005Z"},{"id":"77a62105-95dd-4591-9d79-07327faabf50","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.613889Z"},{"id":"abf8648a-77e8-4327-a5f0-7988fc7fada4","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.359539Z"},{"id":"80691abf-ada6-4e0d-9878-61013fb85eac","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.316656Z"},{"id":"d57fad08-0203-4f7e-ac12-473e9bcbf99e","taskId":null,"projectId":null,"content":"wñ󘢠Ö\u0001g","createdAt":"2026-09-04T22:31:59.950407Z"},{"id":"8f7594e9-3041-467b-9d69-b9bc73a43338","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:01.084963Z"},{"id":"6deb39b7-820e-4103-a3b6-b5deb40f5a09","taskId":null,"projectId":null,"content":"é򥛘","createdAt":"2026-09-04T22:31:59.637063Z"},{"id":"0ca57b07-6898-4133-bf68-6e436ab58d9a","taskId":null,"projectId":null,"content":"é򥛘","createdAt":"2026-09-04T22:32:00.503706Z"},{"id":"ee0dc713-927b-45be-9c98-f72a79a14d96","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:01.132923Z"},{"id":"224b1815-9060-4c1e-b2de-3a0dae4965e0","taskId":null,"projectId":null,"content":"󛾑°r򺷿𝩮𚜛Ûfo\"","createdAt":"2026-09-04T22:32:00.512107Z"},{"id":"0a5de72d-4fd2-4380-9135-4a99906e8f43","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:32:00.950435Z"},{"id":"1ed06bee-f6fc-47ad-bdf8-465e8dd78934","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:31:50.026130Z"},{"id":"1e479477-ba68-4bac-af52-42c101557ba8","taskId":null,"projectId":null,"content":"0","createdAt":"2026-09-04T22:31:59.626964Z"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/comments
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/comments
````

### failure-37: GET /filters/{id}

- Test case: `GET /filters/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: m7wGle

- Response violates schema

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /properties/hasDate:

        {
            "type": "boolean"
        }

    Value:

        null

[200] OK:

    `{"id":"6afc1021-e345-45b5-8dcc-b259c2d13cf5","name":"f©ÿ\r§©","color":"charcoal","order":0,"isFavorite":false,"projectId":"d84a0fe6-c520-426e-a9f2-a948a801711f","hasDate":null}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/6afc1021-e345-45b5-8dcc-b259c2d13cf5

2. Test Case ID: 0kvHqN

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/projectId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"4353ec02-141d-4c95-ae2a-ae7605858412","name":"00","color":"charcoal","order":0,"isFavorite":false,"projectId":null,"hasDate":null}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/4353ec02-141d-4c95-ae2a-ae7605858412

3. Test Case ID: hcPEkK

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Filter not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Filter not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/6afc1021-e345-45b5-8dcc-b259c2d13cf5
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/4353ec02-141d-4c95-ae2a-ae7605858412
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-38: GET /filters/{id}/tasks

- Test case: `GET /filters/{id}/tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: cy7T1J

- Response violates schema (14 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `[{"id":"577c4cef-b325-4b4b-90ca-b6134f5b7382","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":"2000-01-01T00:00:00Z","allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.842848Z","updatedAt":"2026-09-04T22:31:50.842849Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2b26c2a9-2382-4f4c-b420-b67328261686","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.857208Z","updatedAt":"2026-09-04T22:31:50.857209Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0183094f-bf17-4fa3-9ab0-6bdacc0abdde","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.870198Z","updatedAt":"2026-09-04T22:31:50.870198Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"40ae7825-cbe0-45b2-8995-40b08867434b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":"6082d6f5-584d-4de0-83c0-b80f9d6fffc0","parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.882114Z","updatedAt":"2026-09-04T22:31:50.882114Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"3e398297-1e8e-477a-863e-734bc05fe31e","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.894558Z","updatedAt":"2026-09-04T22:31:50.894558Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2f881655-6f96-46f2-8885-898fd788d93c","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"","createdAt":"2026-09-04T22:31:50.918935Z","updatedAt":"2026-09-04T22:31:50.918935Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"8c244817-48a1-4396-83c8-b77ecb1611d4","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":1,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.941041Z","updatedAt":"2026-09-04T22:31:50.941042Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"8ebf088b-3bf1-4f7e-a1d2-4efa4591e71f","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.959145Z","updatedAt":"2026-09-04T22:31:50.959146Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"ef96bb41-9c58-421e-8004-4c161a45b6c4","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.971136Z","updatedAt":"2026-09-04T22:31:50.971137Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"e6bdf1c2-4c02-4223-bb1c-3bd7cba79047","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":"","recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.982621Z","updatedAt":"2026-09-04T22:31:50.982621Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"051c2e8f-56e4-43cd-a1c9-3e0f79ab76ad","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.993027Z","updatedAt":"2026-09-04T22:31:50.993027Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"c872461c-fe75-4a00-963d-c874a7d61701","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.005262Z","updatedAt":"2026-09-04T22:31:51.005263Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"e0a046e7-e03e-4b2e-9ca5-fd6f15638da7","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":"2000-01-01T00:00:00Z","allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.021538Z","updatedAt":"2026-09-04T22:31:51.021538Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"5b3bf23f-3556-4626-918d-449699e8dea6","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.032874Z","updatedAt":"2026-09-04T22:31:51.032874Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0b4df037-1bc1-4459-8fec-0dbdfea54c97","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.043455Z","updatedAt":"2026-09-04T22:31:51.043456Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/c1e3cfe8-72c4-474f-948e-fb9f8bb9d523/tasks
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/c1e3cfe8-72c4-474f-948e-fb9f8bb9d523/tasks
````

### failure-39: GET /filters/{id}/tasks

- Test case: `GET /filters/{id}/tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 1XmZm3

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Filter not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd/tasks",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Filter not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd/tasks","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd/tasks
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/e3e70682-c209-1cac-a29f-6fbed82c07cd/tasks
````

### failure-40: GET /labels/{id}

- Test case: `GET /labels/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: Kazdy5

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Label not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Label not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-41: GET /projects/{id}

- Test case: `GET /projects/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: ql5pTH

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","name":"0","color":"","parentId":null,"order":0,"isFavorite":false,"viewStyle":"LIST","isInboxProject":false,"planningCalendarId":"00000000-0000-0000-0000-000000000001","createdAt":"2026-09-04T22:31:50.395554Z","updatedAt":"2026-09-04T22:31:50.395554Z"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1
````

### failure-42: GET /projects/{id}

- Test case: `GET /projects/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: eoQtmk

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Project not found: 0345b73a-5840-5e47-8dcf-0b3bd6759ed2",
            "instance": "/projects/0345b73a-5840-5e47-8dcf-0b3bd6759ed2",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Project not found: 0345b73a-5840-5e47-8dcf-0b3bd6759ed2","instance":"/projects/0345b73a-5840-5e47-8dcf-0b3bd6759ed2","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/0345b73a-5840-5e47-8dcf-0b3bd6759ed2
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/0345b73a-5840-5e47-8dcf-0b3bd6759ed2
````

### failure-43: GET /projects/{id}/sections

- Test case: `GET /projects/{id}/sections`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: YIMHko

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Project not found: ec0e0eda-eb85-45f5-afea-14317e38a0aa",
            "instance": "/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/sections",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Project not found: ec0e0eda-eb85-45f5-afea-14317e38a0aa","instance":"/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/sections","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/sections
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/sections
````

### failure-44: GET /projects/{id}/tasks

- Test case: `GET /projects/{id}/tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 6WDYwj

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Project not found: ec0e0eda-eb85-45f5-afea-14317e38a0aa",
            "instance": "/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/tasks",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Project not found: ec0e0eda-eb85-45f5-afea-14317e38a0aa","instance":"/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/tasks","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/tasks
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa/tasks
````

### failure-45: GET /sections/{id}

- Test case: `GET /sections/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: VgCb4a

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Section not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/sections/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Section not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/sections/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-46: GET /tasks

- Test case: `GET /tasks`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: be1c0h

- Response violates schema (13 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `[{"id":"769d11b5-f482-44b0-a3a5-640a93ecb42b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":"2000-01-01T00:00:00Z","dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.907389Z","updatedAt":"2026-09-04T22:31:50.907389Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?project_id=d84a0fe6-c520-426e-a9f2-a948a801711f&section_id=8ee8a9a2-178e-4442-a1f6-1542503fcecc&label=&filter=&show_completed=true&singleDate=2000-01-01&from=2000-01-01'

2. Test Case ID: uLAgsg

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

[200] OK:

    `[{"id":"577c4cef-b325-4b4b-90ca-b6134f5b7382","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":"2000-01-01T00:00:00Z","allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.842848Z","updatedAt":"2026-09-04T22:31:50.842849Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2b26c2a9-2382-4f4c-b420-b67328261686","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.857208Z","updatedAt":"2026-09-04T22:31:50.857209Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0183094f-bf17-4fa3-9ab0-6bdacc0abdde","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.870198Z","updatedAt":"2026-09-04T22:31:50.870198Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"40ae7825-cbe0-45b2-8995-40b08867434b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":"6082d6f5-584d-4de0-83c0-b80f9d6fffc0","parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.882114Z","updatedAt":"2026-09-04T22:31:50.882114Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"3e398297-1e8e-477a-863e-734bc05fe31e","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.894558Z","updatedAt":"2026-09-04T22:31:50.894558Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"769d11b5-f482-44b0-a3a5-640a93ecb42b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":"2000-01-01T00:00:00Z","dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.907389Z","updatedAt":"2026-09-04T22:31:50.907389Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2f881655-6f96-46f2-8885-898fd788d93c","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"","createdAt":"2026-09-04T22:31:50.918935Z","updatedAt":"2026-09-04T22:31:50.918935Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0bb1f892-1869-40f4-8fc1-bca6795d2248","content":"0","description":null,"projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.929177Z","updatedAt":"2026-09-04T22:31:50.929177Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"8c244817-48a1-4396-83c8-b77ecb1611d4","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":1,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.941041Z","updatedAt":"2026-09-04T22:31:50.941042Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"8ebf088b-3bf1-4f7e-a1d2-4efa4591e71f","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.959145Z","updatedAt":"2026-09-04T22:31:50.959146Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"ef96bb41-9c58-421e-8004-4c161a45b6c4","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.971136Z","updatedAt":"2026-09-04T22:31:50.971137Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"e6bdf1c2-4c02-4223-bb1c-3bd7cba79047","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":"","recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.982621Z","updatedAt":"2026-09-04T22:31:50.982621Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"051c2e8f-56e4-43cd-a1c9-3e0f79ab76ad","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.993027Z","updatedAt":"2026-09-04T22:31:50.993027Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"c872461c-fe75-4a00-963d-c874a7d61701","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.005262Z","updatedAt":"2026-09-04T22:31:51.005263Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"e0a046e7-e03e-4b2e-9ca5-fd6f15638da7","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":"2000-01-01T00:00:00Z","allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.021538Z","updatedAt":"2026-09-04T22:31:51.021538Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"5b3bf23f-3556-4626-918d-449699e8dea6","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.032874Z","updatedAt":"2026-09-04T22:31:51.032874Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0b4df037-1bc1-4459-8fec-0dbdfea54c97","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.043455Z","updatedAt":"2026-09-04T22:31:51.043456Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0f3762eb-4c6d-463a-aefa-a713eca01994","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":true,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.722864Z","updatedAt":"2026-09-04T22:31:51.099054Z","completedAt":"2026-09-04T22:31:51.097985Z","instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?project_id=d84a0fe6-c520-426e-a9f2-a948a801711f&section_id=8ee8a9a2-178e-4442-a1f6-1542503fcecc&label=&filter=&show_completed=true&from=2000-01-01'
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?project_id=d84a0fe6-c520-426e-a9f2-a948a801711f&section_id=8ee8a9a2-178e-4442-a1f6-1542503fcecc&label=&filter=&show_completed=true&singleDate=2000-01-01&from=2000-01-01'
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?project_id=d84a0fe6-c520-426e-a9f2-a948a801711f&section_id=8ee8a9a2-178e-4442-a1f6-1542503fcecc&label=&filter=&show_completed=true&from=2000-01-01'
````

### failure-47: GET /tasks/{id}

- Test case: `GET /tasks/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: KSmJp6

- Response violates schema (13 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `{"id":"769d11b5-f482-44b0-a3a5-640a93ecb42b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":"2000-01-01T00:00:00Z","dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.907389Z","updatedAt":"2026-09-04T22:31:50.907389Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/769d11b5-f482-44b0-a3a5-640a93ecb42b
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/769d11b5-f482-44b0-a3a5-640a93ecb42b
````

### failure-48: GET /tasks/{id}

- Test case: `GET /tasks/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: M5anCW

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

[200] OK:

    `{"id":"d0b0c503-11a5-485a-983d-8290d38c5bfc","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.991410Z","updatedAt":"2026-09-04T22:32:10.498273Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc

2. Test Case ID: 7FbHv1

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Task not found: cc8d04c7-d851-441f-b174-a6b2d91db1b3",
            "instance": "/tasks/cc8d04c7-d851-441f-b174-a6b2d91db1b3",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Task not found: cc8d04c7-d851-441f-b174-a6b2d91db1b3","instance":"/tasks/cc8d04c7-d851-441f-b174-a6b2d91db1b3","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/cc8d04c7-d851-441f-b174-a6b2d91db1b3
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/cc8d04c7-d851-441f-b174-a6b2d91db1b3
````

### failure-49: GET /time-entries

- Test case: `GET /time-entries`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 6WihgH

- Server error

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 500.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Internal error",
            "instance": "/time-entries",
            "status": 500,
            "title": "Internal Server Error"
        }

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/time-entries","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/time-entries?project_id=d84a0fe6-c520-426e-a9f2-a948a801711f&end='
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/time-entries?project_id=d84a0fe6-c520-426e-a9f2-a948a801711f&end='
````

### failure-50: GET /time-entries

- Test case: `GET /time-entries`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: gPRA21

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TimeEntryDto/properties/notes:

        {
            "type": "string"
        }

    Value:

        null

[200] OK:

    `[{"id":"cb89ff0f-2a63-457b-8257-9b64441e725e","startAt":"0037-07-27T00:02:58Z","endAt":"2026-09-04T22:32:08.564456Z","projectId":"cf3d8893-1b42-4c8b-9b7e-186c4e3af5c7","description":"","notes":"","createdAt":"2026-09-04T22:32:11.322026Z","updatedAt":"2026-09-04T22:32:11.322027Z"},{"id":"4ab6b27b-a090-4ff5-88c0-6f7307c3bc44","startAt":"0037-07-27T00:02:58Z","endAt":"2026-09-04T22:32:08.564456Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":"","createdAt":"2026-09-04T22:32:11.340504Z","updatedAt":"2026-09-04T22:32:11.340504Z"},{"id":"1249f508-037c-4347-8996-074bf5c592f7","startAt":"0037-07-27T00:02:58Z","endAt":"2026-09-04T22:32:08.564456Z","projectId":"cf3d8893-1b42-4c8b-9b7e-186c4e3af5c7","description":"","notes":"","createdAt":"2026-09-04T22:32:11.357619Z","updatedAt":"2026-09-04T22:32:11.357619Z"},{"id":"bfe07209-2ff6-4d3d-8701-16db5bb90b9a","startAt":"0037-07-27T00:02:58Z","endAt":"2026-09-04T22:32:08.564456Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":"","createdAt":"2026-09-04T22:32:11.372547Z","updatedAt":"2026-09-04T22:32:11.372547Z"},{"id":"870bca5b-d113-4684-b3f7-138c5a1a57ab","startAt":"0037-07-27T00:02:58Z","endAt":"2026-09-04T22:32:08.564456Z","projectId":"cf3d8893-1b42-4c8b-9b7e-186c4e3af5c7","description":"","notes":"","createdAt":"2026-09-04T22:32:11.389169Z","updatedAt":"2026-09-04T22:32:11.389169Z"},{"id":"8a1be304-f10a-4b35-bd5d-f39fc49a139c","startAt":"2026-09-04T22:32:08.948497Z","endAt":"2026-09-04T22:32:08.948497Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":"","createdAt":"2026-09-04T22:32:11.402405Z","updatedAt":"2026-09-04T22:32:11.402405Z"},{"id":"1beb9e3c-26a4-46c5-b358-0ceb5d4f168e","startAt":"2026-09-04T22:32:08.948497Z","endAt":"2026-09-04T22:32:08.948497Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":"","createdAt":"2026-09-04T22:32:11.414727Z","updatedAt":"2026-09-04T22:32:11.414727Z"},{"id":"274c844f-08a8-497a-8d15-9855f2ad8024","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-03T05:14:14.708Z","projectId":"cde9395c-cbb1-4fa8-8c47-26ed41f1d24a","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.427577Z","updatedAt":"2026-09-04T22:32:11.427577Z"},{"id":"1fc1c4e2-e480-48bf-808a-cf0e70486edb","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-03T05:14:14.591100Z","projectId":"290892f6-448c-4ea1-9e73-62733eab23b3","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.441157Z","updatedAt":"2026-09-04T22:32:11.441157Z"},{"id":"4ec5f904-4bda-49ed-a8e0-696a98152f2b","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-03T05:24:14.591100Z","projectId":"cde9395c-cbb1-4fa8-8c47-26ed41f1d24a","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.456226Z","updatedAt":"2026-09-04T22:32:11.456226Z"},{"id":"dd864ffb-f615-437d-afa2-f27d1313ecf0","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-02T23:25:46.591100Z","projectId":"290892f6-448c-4ea1-9e73-62733eab23b3","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.476255Z","updatedAt":"2026-09-04T22:32:11.476255Z"},{"id":"9460c0cf-4e2d-437c-9e52-b384e52d3156","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-02T23:25:46.591100Z","projectId":"cde9395c-cbb1-4fa8-8c47-26ed41f1d24a","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.488037Z","updatedAt":"2026-09-04T22:32:11.488037Z"},{"id":"34037726-f1aa-4cb8-9d7a-f841dee08520","startAt":"2026-09-04T22:32:05.343872Z","endAt":"7268-04-02T23:25:46.591100Z","projectId":"290892f6-448c-4ea1-9e73-62733eab23b3","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.500766Z","updatedAt":"2026-09-04T22:32:11.500766Z"},{"id":"80c8bbaf-3348-4329-af36-59e837480f2b","startAt":"2026-09-04T22:32:08.403737Z","endAt":"2026-09-04T22:32:09.253023Z","projectId":"0bfd7bb0-567e-427d-86d0-e79be246a1b1","description":"","notes":"","createdAt":"2026-09-04T22:32:11.599408Z","updatedAt":"2026-09-04T22:32:11.599408Z"},{"id":"17153013-882e-4156-94c9-ea4bf4a54333","startAt":"2026-09-04T22:32:08.403737Z","endAt":"2026-09-04T22:32:09.253023Z","projectId":"bb775061-e2b0-49aa-9dc2-420472bfba46","description":"","notes":"","createdAt":"2026-09-04T22:32:11.614120Z","updatedAt":"2026-09-04T22:32:11.614120Z"},{"id":"de05c328-235a-41f7-8e79-e9ab2da50c44","startAt":"2026-09-04T22:32:08.403737Z","endAt":"2026-09-04T22:32:09.253023Z","projectId":"0bfd7bb0-567e-427d-86d0-e79be246a1b1","description":"","notes":"","createdAt":"2026-09-04T22:32:11.628963Z","updatedAt":"2026-09-04T22:32:11.628963Z"},{"id":"c27b42c0-94ca-45cb-9a44-47c945e5f7a7","startAt":"2026-09-04T22:32:08.403737Z","endAt":"2026-09-04T22:32:09.253023Z","projectId":"bb775061-e2b0-49aa-9dc2-420472bfba46","description":"","notes":"","createdAt":"2026-09-04T22:32:11.643551Z","updatedAt":"2026-09-04T22:32:11.643551Z"},{"id":"7d48e61a-b3dc-42b0-ab4e-6988471dfba7","startAt":"2026-09-04T22:32:08.403737Z","endAt":"2026-09-04T22:32:09.253023Z","projectId":"0bfd7bb0-567e-427d-86d0-e79be246a1b1","description":"","notes":"","createdAt":"2026-09-04T22:32:11.657436Z","updatedAt":"2026-09-04T22:32:11.657437Z"},{"id":"ae01880a-2cc2-40cc-881b-0238fb2b1be6","startAt":"1374-01-07T23:50:43.607980Z","endAt":"2026-09-04T22:32:09.013012Z","projectId":"7075a160-7937-4b0d-a5fc-d1111023dcb4","description":"","notes":"","createdAt":"2026-09-04T22:32:11.764095Z","updatedAt":"2026-09-04T22:32:11.764095Z"},{"id":"8dbdd2ab-e31a-4ad5-b6da-0478d1c1eab5","startAt":"1374-01-07T23:50:43Z","endAt":"2026-09-04T22:32:09.013012Z","projectId":"bf28f493-16dc-4805-b55c-af6662720451","description":"","notes":"","createdAt":"2026-09-04T22:32:11.786107Z","updatedAt":"2026-09-04T22:32:11.786107Z"},{"id":"e34e5faf-d237-4ef0-b3e0-8185e2b7c795","startAt":"2026-09-04T22:32:08.948191Z","endAt":"2026-09-04T22:32:08.969784Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":"","createdAt":"2026-09-04T22:32:11.801100Z","updatedAt":"2026-09-04T22:32:11.801100Z"},{"id":"091ed3d6-ceff-4cf3-b45a-0d2fb714cf7c","startAt":"2026-09-04T22:32:08.948191Z","endAt":"2026-09-04T22:32:08.969784Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":"","createdAt":"2026-09-04T22:32:11.815576Z","updatedAt":"2026-09-04T22:32:11.815576Z"},{"id":"455743e1-886a-4a1c-9762-be90213709fc","startAt":"2026-09-04T22:32:08.948191Z","endAt":"2026-09-04T22:32:08.969784Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":"","createdAt":"2026-09-04T22:32:11.828276Z","updatedAt":"2026-09-04T22:32:11.828277Z"},{"id":"bd1c974f-a3ea-4928-b198-d0cbdfdbf2fc","startAt":"2026-09-04T22:32:08.948191Z","endAt":"2026-09-04T22:32:08.969784Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":"","createdAt":"2026-09-04T22:32:11.842143Z","updatedAt":"2026-09-04T22:32:11.842144Z"},{"id":"b40e5bba-c98f-4a59-93d8-6da8f122d54f","startAt":"2026-09-04T22:32:08.948191Z","endAt":"2026-09-04T22:32:08.969784Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":"","createdAt":"2026-09-04T22:32:11.855922Z","updatedAt":"2026-09-04T22:32:11.855922Z"},{"id":"cdd5e77f-b60d-4ddd-b00e-459ff04da7b1","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T09:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.865859Z","updatedAt":"2026-09-04T22:32:11.865859Z"},{"id":"7f6f52fe-1b01-478d-b522-37442091b3e4","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T09:07:01Z","projectId":"cbbc1b0b-62ee-44ac-8c46-5dd7f5f87797","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.877258Z","updatedAt":"2026-09-04T22:32:11.877259Z"},{"id":"388cb78b-1375-442f-a002-ae517c3b3fd4","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.888526Z","updatedAt":"2026-09-04T22:32:11.888526Z"},{"id":"befb012e-14d2-48f6-ab7e-43b69ae079c2","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T07:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.909200Z","updatedAt":"2026-09-04T22:32:11.909200Z"},{"id":"ebbcddb7-317d-4487-967c-35553ae2b1fb","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T17:07:02Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.928914Z","updatedAt":"2026-09-04T22:32:11.928914Z"},{"id":"e66ec2a3-fe26-47d9-9d8d-c71eebff17ed","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.941270Z","updatedAt":"2026-09-04T22:32:11.941270Z"},{"id":"88c2bcac-373a-44d4-b240-dd04dceacec4","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.955001Z","updatedAt":"2026-09-04T22:32:11.955001Z"},{"id":"84ef4586-bcb2-4fd7-91e4-1a754ab79c36","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.965883Z","updatedAt":"2026-09-04T22:32:11.965883Z"},{"id":"f63b9cba-ccb2-475b-8443-b79079b36a8d","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.976516Z","updatedAt":"2026-09-04T22:32:11.976516Z"},{"id":"f904b0f6-5b38-4b5a-b17a-54f46ac23dba","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.987633Z","updatedAt":"2026-09-04T22:32:11.987633Z"},{"id":"f5332027-1e10-4ff9-816e-f4e233e1cbea","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:11.997289Z","updatedAt":"2026-09-04T22:32:11.997289Z"},{"id":"79b15690-755d-4b75-90f0-0628dd167fe1","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.019394Z","updatedAt":"2026-09-04T22:32:12.019394Z"},{"id":"c55454c1-d138-48ed-862b-c3db3e1938af","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.030083Z","updatedAt":"2026-09-04T22:32:12.030083Z"},{"id":"793db072-d5da-4b4e-810e-2a41fcc1a26a","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.042096Z","updatedAt":"2026-09-04T22:32:12.042096Z"},{"id":"63d6b602-82c3-47de-b380-c44c879df5e9","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.053078Z","updatedAt":"2026-09-04T22:32:12.053078Z"},{"id":"80389e0c-fc93-475f-b3a1-2074f0fb3ad6","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.065364Z","updatedAt":"2026-09-04T22:32:12.065364Z"},{"id":"472f89bb-5ef3-4aad-b6ae-8e638da5f4aa","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.076921Z","updatedAt":"2026-09-04T22:32:12.076921Z"},{"id":"5d6f54aa-d5c3-4001-8856-4f7123e35bad","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.103973Z","updatedAt":"2026-09-04T22:32:12.103973Z"},{"id":"3af0329b-23a0-4d12-ac15-2c534cc56c1e","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.116582Z","updatedAt":"2026-09-04T22:32:12.116582Z"},{"id":"12a6a640-d9b7-43e4-9669-21bbac08d023","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.144212Z","updatedAt":"2026-09-04T22:32:12.144212Z"},{"id":"afc33802-2f29-4f97-9cc9-b167ed761560","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.158790Z","updatedAt":"2026-09-04T22:32:12.158790Z"},{"id":"aa94ab96-1ae0-4202-9f6e-9eb53efeb90c","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.266064Z","updatedAt":"2026-09-04T22:32:12.266064Z"},{"id":"6edc93c0-9448-4749-ac9d-8a616e9f4bd5","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.278271Z","updatedAt":"2026-09-04T22:32:12.278271Z"},{"id":"aa956ddf-c7a1-486b-8229-fa527cd91de2","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"0532c133-d60b-42e5-902a-3dce31352233","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.288592Z","updatedAt":"2026-09-04T22:32:12.288592Z"},{"id":"b7c0a4b4-b2c2-4c79-b404-ebf107efbfe4","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.298951Z","updatedAt":"2026-09-04T22:32:12.298951Z"},{"id":"ecac908c-9205-42a6-9ca4-5cda4454e433","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"9836d5d4-c89a-4b05-9bef-fcda09b78e23","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.310946Z","updatedAt":"2026-09-04T22:32:12.310946Z"},{"id":"368744ed-ad59-4c60-911f-f9ad9f64f918","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.325196Z","updatedAt":"2026-09-04T22:32:12.325196Z"},{"id":"a0bad399-0a08-464f-8c2d-91c96ea508b7","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.335778Z","updatedAt":"2026-09-04T22:32:12.335779Z"},{"id":"38b8ad86-be1f-478d-8729-5f07da07313f","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.346669Z","updatedAt":"2026-09-04T22:32:12.346669Z"},{"id":"60fd6268-c49a-4eb0-a0c4-f901ea0c517c","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"1c4d41fb-565a-4178-ace8-32585c709916","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.359277Z","updatedAt":"2026-09-04T22:32:12.359277Z"},{"id":"d1bc355c-2b7f-447e-a511-0bc7c9c0d681","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"708809cc-5666-4433-8242-310c83302610","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.373296Z","updatedAt":"2026-09-04T22:32:12.373296Z"},{"id":"30d77346-dbb7-434c-a24b-dc3c2449107a","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"d10a76ce-623d-4a4f-b9f9-0e826599a0c2","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.384280Z","updatedAt":"2026-09-04T22:32:12.384281Z"},{"id":"8955fd77-d3be-4679-ba72-dab3ac8ed68f","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"708809cc-5666-4433-8242-310c83302610","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.395159Z","updatedAt":"2026-09-04T22:32:12.395159Z"},{"id":"e8f74183-be58-4712-a0e7-177bf37d9d32","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.405331Z","updatedAt":"2026-09-04T22:32:12.405331Z"},{"id":"b3c49cc8-144f-413b-8814-b79e3d9d44ae","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"d10a76ce-623d-4a4f-b9f9-0e826599a0c2","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.417122Z","updatedAt":"2026-09-04T22:32:12.417122Z"},{"id":"06bc42d0-a533-4e75-9e79-9c937eee8af7","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"9836d5d4-c89a-4b05-9bef-fcda09b78e23","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.428220Z","updatedAt":"2026-09-04T22:32:12.428220Z"},{"id":"0ff7c8ef-dc49-4255-a634-37db973eb72e","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"708809cc-5666-4433-8242-310c83302610","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.438532Z","updatedAt":"2026-09-04T22:32:12.438533Z"},{"id":"3031eee0-f002-4a28-a8bb-163b79043546","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"f1d34720-b305-43e7-bbae-8a827a46445d","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.451610Z","updatedAt":"2026-09-04T22:32:12.451610Z"},{"id":"368f247e-9c8a-4e9c-9829-7e88bbe473c2","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"9836d5d4-c89a-4b05-9bef-fcda09b78e23","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.463951Z","updatedAt":"2026-09-04T22:32:12.463951Z"},{"id":"fcabf8d1-3d2a-4769-803f-25750cd268b8","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"d10a76ce-623d-4a4f-b9f9-0e826599a0c2","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.476182Z","updatedAt":"2026-09-04T22:32:12.476182Z"},{"id":"2e56a4f8-13c5-44e2-b381-669cbfe4d411","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"cf3d8893-1b42-4c8b-9b7e-186c4e3af5c7","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.486472Z","updatedAt":"2026-09-04T22:32:12.486472Z"},{"id":"1a766ade-cf18-42b7-9b81-f68a96c67332","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.496157Z","updatedAt":"2026-09-04T22:32:12.496158Z"},{"id":"80e94ecf-46ab-43d9-9bca-402e40109b3b","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"f0e268a4-df68-4211-9598-6a43e60dde9e","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.506499Z","updatedAt":"2026-09-04T22:32:12.506499Z"},{"id":"e6ec2491-967d-4b97-b624-c69382245fe4","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"e0d65792-64c7-4258-bf57-370c12bbaf6a","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.517008Z","updatedAt":"2026-09-04T22:32:12.517008Z"},{"id":"c2696455-f275-41b2-8860-34f3dacd86fb","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"d10a76ce-623d-4a4f-b9f9-0e826599a0c2","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.527893Z","updatedAt":"2026-09-04T22:32:12.527893Z"},{"id":"8b465057-0ec4-4c78-925a-af30708258f2","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"e0d65792-64c7-4258-bf57-370c12bbaf6a","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.539186Z","updatedAt":"2026-09-04T22:32:12.539186Z"},{"id":"ff790ea9-18f7-46d3-bd5d-b09421447c14","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"708809cc-5666-4433-8242-310c83302610","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.560359Z","updatedAt":"2026-09-04T22:32:12.560359Z"},{"id":"64fb8b64-508e-4b78-890d-29c4a80ab898","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.548926Z","updatedAt":"2026-09-04T22:32:24.595720Z"},{"id":"cd55232e-02d3-4388-a451-4f4b12bb3d9b","startAt":"1026-12-27T10:55:41.209Z","endAt":"4261-11-30T01:07:01Z","projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.007439Z","updatedAt":"2026-09-04T22:32:24.804628Z"},{"id":"1d0c5fa1-2545-4df8-8ca9-93eec5246e53","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.171852Z","updatedAt":"2026-09-04T22:32:24.902423Z"},{"id":"69deedb3-508a-4ef2-b72f-d8bf505702c1","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.131597Z","updatedAt":"2026-09-04T22:32:24.933648Z"},{"id":"a3b0a138-7755-4998-95f0-c0ede4696b90","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.090067Z","updatedAt":"2026-09-04T22:32:25.001996Z"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/time-entries?project_id=50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1'
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/time-entries?project_id=50c7d3c9-4a29-4a90-9efb-99f5d5fd43f1'
````

### failure-51: GET /time-entries/{id}

- Test case: `GET /time-entries/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: kDrlkl

- Response violates schema

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/notes:

        {
            "type": "string"
        }

    Value:

        null

[200] OK:

    `{"id":"63d6b602-82c3-47de-b380-c44c879df5e9","startAt":"2026-09-04T22:32:09.047091Z","endAt":"4261-11-30T01:07:01Z","projectId":"c087d448-2b81-4243-897b-ec856f818843","description":"","notes":null,"createdAt":"2026-09-04T22:32:12.053078Z","updatedAt":"2026-09-04T22:32:12.053078Z"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/time-entries/63d6b602-82c3-47de-b380-c44c879df5e9
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/time-entries/63d6b602-82c3-47de-b380-c44c879df5e9
````

### failure-52: DELETE /comments/{id}

- Test case: `DELETE /comments/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 1znP8h

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Comment not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/comments/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Comment not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/comments/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/comments/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/comments/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-53: DELETE /labels/{id}

- Test case: `DELETE /labels/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: LYPt7E

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Label not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Label not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/labels/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-54: DELETE /projects/{id}

- Test case: `DELETE /projects/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: PdUM5s

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Project not found: ec0e0eda-eb85-45f5-afea-14317e38a0aa",
            "instance": "/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Project not found: ec0e0eda-eb85-45f5-afea-14317e38a0aa","instance":"/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa
````

Reproduction:

````shell
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/ec0e0eda-eb85-45f5-afea-14317e38a0aa
````

### failure-55: DELETE /tasks/{taskId}

- Test case: `DELETE /tasks/{taskId}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: N9gRTF

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Task not found: 0183094f-bf17-4fa3-9ab0-6bdacc0abdde",
            "instance": "/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Task not found: 0183094f-bf17-4fa3-9ab0-6bdacc0abdde","instance":"/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X DELETE -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde
````

Reproduction:

````shell
curl -X DELETE -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/0183094f-bf17-4fa3-9ab0-6bdacc0abdde
````

### failure-56: DELETE /time-entries/{id}

- Test case: `DELETE /time-entries/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: ZeVERK

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "TimeEntry not found: cd55232e-02d3-4388-a451-4f4b12bb3d9b",
            "instance": "/time-entries/cd55232e-02d3-4388-a451-4f4b12bb3d9b",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"TimeEntry not found: cd55232e-02d3-4388-a451-4f4b12bb3d9b","instance":"/time-entries/cd55232e-02d3-4388-a451-4f4b12bb3d9b","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/time-entries/cd55232e-02d3-4388-a451-4f4b12bb3d9b
````

Reproduction:

````shell
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/time-entries/cd55232e-02d3-4388-a451-4f4b12bb3d9b
````

### failure-57: GET /planning-calendars/{id}

- Test case: `GET /planning-calendars/{id}`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: eHjjuP

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Planning calendar not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Planning calendar not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/planning-calendars/e3e70682-c209-1cac-a29f-6fbed82c07cd
````

### failure-58: GET /tasks/{id}/priority-evaluation

- Test case: `GET /tasks/{id}/priority-evaluation`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: Tl9MHl

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Task not found: e3e70682-c209-1cac-a29f-6fbed82c07cd",
            "instance": "/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/priority-evaluation",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Task not found: e3e70682-c209-1cac-a29f-6fbed82c07cd","instance":"/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/priority-evaluation","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/priority-evaluation
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/tasks/e3e70682-c209-1cac-a29f-6fbed82c07cd/priority-evaluation
````

### failure-59: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: h3PvyP

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/comments?project_id=f4bfd780-a52e-4d04-b8f9-2fa67dc8aeaf'
    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/comments?project_id=f4bfd780-a52e-4d04-b8f9-2fa67dc8aeaf'
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
````

### failure-60: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: W1XR2E

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Section not found: 764a7d4e-a6cd-4fa2-a65e-eca11b2d3abe",
            "instance": "/sections/764a7d4e-a6cd-4fa2-a65e-eca11b2d3abe",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Section not found: 764a7d4e-a6cd-4fa2-a65e-eca11b2d3abe","instance":"/sections/764a7d4e-a6cd-4fa2-a65e-eca11b2d3abe","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/sections?project_id=e0d65792-64c7-4258-bf57-370c12bbaf6a'
    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/764a7d4e-a6cd-4fa2-a65e-eca11b2d3abe
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/sections?project_id=e0d65792-64c7-4258-bf57-370c12bbaf6a'
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/764a7d4e-a6cd-4fa2-a65e-eca11b2d3abe
````

### failure-61: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: KRLxTy

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/projects/None","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters
    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/None
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/None
````

### failure-62: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: owUBU8

- Response violates schema (10 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

- API accepted schema-violating request

    Invalid data should have been rejected
    Expected: 400, 401, 403, 404, 405, 406, 409, 422, 428, 5xx
    Invalid component: positive test case

[200] OK:

    `{"id":"7e545aec-beb9-47ec-a419-ac4ed186776e","content":"\\","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":"af6a1291-a71f-4fd8-98cd-ea95186abe44","parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":true,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":123,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.233318Z","updatedAt":"2026-09-04T22:32:56.784023154Z","completedAt":"2026-09-04T22:32:56.744503Z","instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/7e545aec-beb9-47ec-a419-ac4ed186776e/close
    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"G\ud86f\udfd4m\u00c5\u00a7\udb9f\udd1a\u00ce": ["\u00eb\u00d8", [3.341594940473778e+166, null, null]], "\u001e\udba8\udf47\u00e0": {"\ub443\u00ecq\u009c\ud85c\udf34\u0090\u0081/S\u00e9[\uda57\udeb6": {}}, "": {}, "\udb97\udd96\u0011": [{"M\u00b7\udabf\udcef": []}, [null], {}], "i\ud914\udc53\u00fdlEm\udacf\uddf7": [-0.5, "", 27], "description": "", "type": "TODO", "labels": [], "dueAt": null, "isRecurring": false, "mentionContext": null, "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e", "scheduledAt": null, "sectionId": "af6a1291-a71f-4fd8-98cd-ea95186abe44", "priority": null, "recurrenceRule": null, "content": "\\\u008b\u0097", "allDay": false, "parentId": null, "order": 0, "estimateMinutes": 123}' http://taska-backend:8080/tasks/7e545aec-beb9-47ec-a419-ac4ed186776e
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{}' http://taska-backend:8080/tasks/7e545aec-beb9-47ec-a419-ac4ed186776e/close
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"G\ud86f\udfd4m\u00c5\u00a7\udb9f\udd1a\u00ce": ["\u00eb\u00d8", [3.341594940473778e+166, null, null]], "\u001e\udba8\udf47\u00e0": {"\ub443\u00ecq\u009c\ud85c\udf34\u0090\u0081/S\u00e9[\uda57\udeb6": {}}, "": {}, "\udb97\udd96\u0011": [{"M\u00b7\udabf\udcef": []}, [null], {}], "i\ud914\udc53\u00fdlEm\udacf\uddf7": [-0.5, "", 27], "description": "", "type": "TODO", "labels": [], "dueAt": null, "isRecurring": false, "mentionContext": null, "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e", "scheduledAt": null, "sectionId": "af6a1291-a71f-4fd8-98cd-ea95186abe44", "priority": null, "recurrenceRule": null, "content": "\\\u008b\u0097", "allDay": false, "parentId": null, "order": 0, "estimateMinutes": 123}' http://taska-backend:8080/tasks/7e545aec-beb9-47ec-a419-ac4ed186776e
````

### failure-63: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: idjyp3

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 404.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Filter not found: 1d569e14-b1e0-4f89-a906-f467b32f1ae1",
            "instance": "/filters/1d569e14-b1e0-4f89-a906-f467b32f1ae1",
            "status": 404,
            "title": "Not Found"
        }

[404] Not Found:

    `{"detail":"Filter not found: 1d569e14-b1e0-4f89-a906-f467b32f1ae1","instance":"/filters/1d569e14-b1e0-4f89-a906-f467b32f1ae1","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0086\u00b6\u00cc\u008bI@\u0081\u0089Q\u001f": {"\ud8f5\udfee\ud8a6\udc20": 1538, "": -1031, "\u00d5\u1de4\u0090{\uda45\ude73\udb0d\udc11\u008f\u00eei\u00c8\u00fa": "f1\u0083\u00b6\u247a5pD\ud8ed\udf23p\udaf9\udf6b\u0090\ua32f\u00c8\ud84f\ude04\u00b8\u001a"}, "\u00e4Zx\u00fb": {}, "hasDate": true, "isFavorite": true, "color": "\udb66\udeef\u00a7*\u0091\u0003;", "clearProject": false, "name": "\ud802\udd52", "projectId": "105d0df3-22b7-452f-ad94-f5d1b2937433"}' http://taska-backend:8080/filters
    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/1d569e14-b1e0-4f89-a906-f467b32f1ae1
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0086\u00b6\u00cc\u008bI@\u0081\u0089Q\u001f": {"\ud8f5\udfee\ud8a6\udc20": 1538, "": -1031, "\u00d5\u1de4\u0090{\uda45\ude73\udb0d\udc11\u008f\u00eei\u00c8\u00fa": "f1\u0083\u00b6\u247a5pD\ud8ed\udf23p\udaf9\udf6b\u0090\ua32f\u00c8\ud84f\ude04\u00b8\u001a"}, "\u00e4Zx\u00fb": {}, "hasDate": true, "isFavorite": true, "color": "\udb66\udeef\u00a7*\u0091\u0003;", "clearProject": false, "name": "\ud802\udd52", "projectId": "105d0df3-22b7-452f-ad94-f5d1b2937433"}' http://taska-backend:8080/filters
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/1d569e14-b1e0-4f89-a906-f467b32f1ae1
````

### failure-64: POST /sections`

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: q2Y6hC

- Resource is not available after creation

    The API returned `404 Not Found` for a resource that was just created.

    Created with      : `POST /sections`
    Not available with: `GET /sections/{id}`

[404] Not Found:

    `{"detail":"Section not found: 503c2b91-388d-471f-81a4-4859e344fd9c","instance":"/sections/503c2b91-388d-471f-81a4-4859e344fd9c","status":404,"title":"Not Found"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "\ud802\udd52", "projectId": "2606185e-080e-434c-af4e-ebe2b33b94dd"}' http://taska-backend:8080/sections
    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/503c2b91-388d-471f-81a4-4859e344fd9c
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "\ud802\udd52", "projectId": "2606185e-080e-434c-af4e-ebe2b33b94dd"}' http://taska-backend:8080/sections
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/503c2b91-388d-471f-81a4-4859e344fd9c
````

### failure-65: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: pW8p22

- API accepted schema-violating request

    Invalid data should have been rejected
    Expected: 400, 401, 403, 404, 405, 406, 409, 422, 428, 5xx
    Invalid component: positive test case

[201] Created:

    `{"id":"f6cf064b-ca12-45d1-87ad-a5dac9abc05f","name":"¢ÓvîõR×ãâ","color":"#808080","parentId":null,"order":0,"isFavorite":false,"viewStyle":"LIST","isInboxProject":false,"planningCalendarId":"00000000-0000-0000-0000-000000000001","createdAt":"2026-09-04T22:33:02.190211065Z","updatedAt":"2026-09-04T22:33:02.190211135Z"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/comments?task_id=89494e77-4a6f-435b-890e-bb5c214a6098&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e'
    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "\u00a2\u008c\u0080\u00d3v\u00ee\u00f5R\u00d7\u00e3\u00e2", "parentId": null, "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/comments?task_id=89494e77-4a6f-435b-890e-bb5c214a6098&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e'
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"name": "\u00a2\u008c\u0080\u00d3v\u00ee\u00f5R\u00d7\u00e3\u00e2", "parentId": null, "planningCalendarId": "00000000-0000-0000-0000-000000000001"}' http://taska-backend:8080/projects
````

### failure-66: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: DIy2Ce

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks/6907660d-8f29-4f7f-ad63-5ded68f1d828/close","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": -148, "occurrenceScheduledAt": "2026-09-04T22:33:00.168686912Z"}' http://taska-backend:8080/tasks/6907660d-8f29-4f7f-ad63-5ded68f1d828/close
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": -148, "occurrenceScheduledAt": "2026-09-04T22:33:00.168686912Z"}' http://taska-backend:8080/tasks/6907660d-8f29-4f7f-ad63-5ded68f1d828/close
````

### failure-67: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: IRWvHe

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks/6907660d-8f29-4f7f-ad63-5ded68f1d828","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/tasks?show_completed=false&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e&section_id=6920995e-b2c9-4397-91d0-21a09535d5df'
    curl -X DELETE -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\t\u00fc": [[]], "\u00ee": {"": [9.257966334952828e-125, null]}, "\ud9ec\uddeb\u0085*\u00ba?": [], "\ud963\ude64\u00a0?": {}, "scope": "THIS_ONLY", "occurrenceScheduledAt": "2026-09-04T22:33:01.692666178Z"}' http://taska-backend:8080/tasks/6907660d-8f29-4f7f-ad63-5ded68f1d828
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/tasks?show_completed=false&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e&section_id=6920995e-b2c9-4397-91d0-21a09535d5df'
curl -X DELETE -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\t\u00fc": [[]], "\u00ee": {"": [9.257966334952828e-125, null]}, "\ud9ec\uddeb\u0085*\u00ba?": [], "\ud963\ude64\u00a0?": {}, "scope": "THIS_ONLY", "occurrenceScheduledAt": "2026-09-04T22:33:01.692666178Z"}' http://taska-backend:8080/tasks/6907660d-8f29-4f7f-ad63-5ded68f1d828
````

### failure-68: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: Wau2ED

- Response violates schema (14 violations)

    null is not of type "boolean"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/isVirtual:

        {
            "type": "boolean"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/priority:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/description:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/dueAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/instanceId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/mentionContext:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/occurrenceScheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/parentId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/recurrenceRule:

        {
            "type": "string"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/rruleEndsAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/scheduledAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /TaskDto/properties/sectionId:

        {
            "type": "string",
            "format": "uuid"
        }

    Value:

        null

[200] OK:

    `[{"id":"e6bdf1c2-4c02-4223-bb1c-3bd7cba79047","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":"","recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.982621Z","updatedAt":"2026-09-04T22:31:50.982621Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"5b3bf23f-3556-4626-918d-449699e8dea6","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.032874Z","updatedAt":"2026-09-04T22:31:51.032874Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0b4df037-1bc1-4459-8fec-0dbdfea54c97","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:51.043455Z","updatedAt":"2026-09-04T22:31:51.043456Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"40ae7825-cbe0-45b2-8995-40b08867434b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:31:50.882114Z","updatedAt":"2026-09-04T22:31:50.882114Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"801662c1-7ed8-472f-ae38-9efa9b3ee440","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.209474Z","updatedAt":"2026-09-04T22:32:05.209474Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"686ff088-58e7-4195-b173-7a6ab8f319df","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.270831Z","updatedAt":"2026-09-04T22:32:05.270832Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"addd3b11-f3d8-465d-af8a-eb255ca4a0ae","content":"񮃬§÷񡻔§","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":true,"estimateMinutes":null,"mentionContext":"","recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.363804Z","updatedAt":"2026-09-04T22:32:05.363804Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"e7b8508d-b8ac-487f-ae20-a3ba88dd0bb2","content":"0","description":"","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.380405Z","updatedAt":"2026-09-04T22:32:05.380405Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"59eb1fa5-37ac-46df-aa28-56502bb7000b","content":"","description":"}ã,𨷋.G","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":"2026-09-04T22:32:03.867844Z","dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"}ã,𨷋.G","createdAt":"2026-09-04T22:32:05.437193Z","updatedAt":"2026-09-04T22:32:05.437193Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"620573ad-5185-41e5-9479-ba1440b4c329","content":"","description":"}ã,𨷋.G","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":"2026-09-04T22:32:03.867844Z","dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"}ã,𨷋.G","createdAt":"2026-09-04T22:32:05.462598Z","updatedAt":"2026-09-04T22:32:05.462599Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"18aeb4b8-a4d0-4bfe-8f97-011d24261fa3","content":"0","description":"}ã,𨷋.G","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":"2026-09-04T22:32:03.867844Z","dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"}ã,𨷋.G","createdAt":"2026-09-04T22:32:05.483290Z","updatedAt":"2026-09-04T22:32:05.483290Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"fa8d2da2-fcb2-48c3-9fc7-33bc6257df26","content":"󛾑°r򺷿𝩮𚜛Ûfo\"","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.673281Z","updatedAt":"2026-09-04T22:32:05.673281Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"8c3a4a54-554b-4547-8411-ae162846ccb3","content":"󛾑°r򺷿𝩮𚜛Ûfo\"","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.697220Z","updatedAt":"2026-09-04T22:32:05.697220Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"6907660d-8f29-4f7f-ad63-5ded68f1d828","content":"wñ󘢠Ö\u0001g","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":"2026-09-04T22:33:02.809648Z","allDay":false,"isRecurring":true,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:33:03.815370Z","updatedAt":"2026-09-04T22:33:03.815370Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"e7b77afc-7535-45ef-9693-2a2621d42a11","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.343872Z","updatedAt":"2026-09-04T22:32:10.340320Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"5f953de1-2930-4555-8e39-78b258cf72e3","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:05.733821Z","updatedAt":"2026-09-04T22:32:05.733821Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"a32aed98-fe49-44b9-bf0d-2055a79af968","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"","createdAt":"2026-09-04T22:32:05.917816Z","updatedAt":"2026-09-04T22:32:05.917816Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"08d96d4c-8a40-4c31-ac4b-5e76d62d7bc6","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"","createdAt":"2026-09-04T22:32:05.953939Z","updatedAt":"2026-09-04T22:32:05.953939Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"50dfe2d8-df49-45dc-8ee9-d1b255017a32","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":"","createdAt":"2026-09-04T22:32:05.974982Z","updatedAt":"2026-09-04T22:32:05.974982Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"db46b2b3-ed33-456b-b59e-7b1b4926f5c7","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.047964Z","updatedAt":"2026-09-04T22:32:06.047964Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"5fba5311-d1d1-4993-a783-76d389190e6c","content":"é򥛘","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.129258Z","updatedAt":"2026-09-04T22:32:06.129258Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"76d2bb32-c8b4-4a87-baca-6cefd9758072","content":"é򥛘","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.150765Z","updatedAt":"2026-09-04T22:32:06.150765Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"743d8062-d872-4dfd-859a-11731c5ccba5","content":"é򥛘","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.224731Z","updatedAt":"2026-09-04T22:32:06.224731Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"795cd679-85f5-4aad-bc19-ba9ec59c9214","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.267920Z","updatedAt":"2026-09-04T22:32:06.267920Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"b04dda7d-2547-442a-b93d-9d082cf23d9f","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.297884Z","updatedAt":"2026-09-04T22:32:06.297885Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"db9db936-c8f1-43f6-a942-346d3c6fd175","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.329205Z","updatedAt":"2026-09-04T22:32:06.329205Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"cfc7b7bf-7d80-43a2-86de-a1cb38a5f649","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.385436Z","updatedAt":"2026-09-04T22:32:06.385436Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"fb929259-0d4e-4b7f-92f4-3157335b4dfb","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.413976Z","updatedAt":"2026-09-04T22:32:06.413976Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"246f203d-b544-4c6d-9585-ce107a6b55e1","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.461288Z","updatedAt":"2026-09-04T22:32:06.461288Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"6667a91f-d22d-4e89-a1d3-00e85b140b73","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.491597Z","updatedAt":"2026-09-04T22:32:06.491597Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"0912b338-3aeb-4a19-b132-2cdbdec8c0b4","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.531842Z","updatedAt":"2026-09-04T22:32:06.531842Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"9f53c82a-f748-4b24-8bbc-0f6a503f2ce3","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.554827Z","updatedAt":"2026-09-04T22:32:06.554827Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"309e344f-a15d-453b-9663-ffa43b84dfb9","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.573720Z","updatedAt":"2026-09-04T22:32:06.573721Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"c75e10c7-74d2-4053-9756-be95bcfd46c6","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.604658Z","updatedAt":"2026-09-04T22:32:06.604658Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2f0e63cc-0f75-4cd4-bc35-a1d34e4fb556","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.634899Z","updatedAt":"2026-09-04T22:32:06.634899Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"930955c7-3c27-41a7-92a1-da0c66f3038e","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.670059Z","updatedAt":"2026-09-04T22:32:06.670059Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"ab44f32b-4096-4b2b-b3ef-df9ade045c01","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.686703Z","updatedAt":"2026-09-04T22:32:06.686703Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"5d25287d-77cb-48f0-a5f2-d5458a66a209","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.701777Z","updatedAt":"2026-09-04T22:32:06.701777Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"ecc9fd61-f545-4631-b20d-dddc84242459","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.718011Z","updatedAt":"2026-09-04T22:32:06.718011Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2fcf0a1f-2812-4bc6-970a-e66eed36bb20","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.736402Z","updatedAt":"2026-09-04T22:32:06.736402Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"4dae249c-3255-4971-9255-756561e9ab2c","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.781011Z","updatedAt":"2026-09-04T22:32:06.781011Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"28183cf1-1377-4a17-b020-7f5ad2e0e706","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.847959Z","updatedAt":"2026-09-04T22:32:06.847959Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"c02cf739-ab59-43dc-be02-1d7a63320fd6","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.947221Z","updatedAt":"2026-09-04T22:32:06.947221Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"70012fe7-7258-4489-890b-d05759d08958","content":"øï\u0003🼺'󉱰𥦼","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:07.055543Z","updatedAt":"2026-09-04T22:32:07.055543Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"2d9e305c-fadc-4694-a700-0a4bdbf466ec","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.313497Z","updatedAt":"2026-09-04T22:32:09.468785Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"adb165b3-9122-4468-b213-7fce396a1d79","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.766122Z","updatedAt":"2026-09-04T22:32:09.492110Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"b8bc2620-4685-4d15-b9d7-c6370337487b","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.362216Z","updatedAt":"2026-09-04T22:32:09.513408Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"7966034a-8a4a-4b40-9444-b341d19d8903","content":"é򥛘","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.248635Z","updatedAt":"2026-09-04T22:32:09.560442Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"88ab37a5-88bd-4f6a-9129-cfd0c6e8ea1f","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.620028Z","updatedAt":"2026-09-04T22:32:09.645481Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"a835f014-c296-4cc3-acbe-d5dddab40b3c","content":"񮃬§÷񡻔§","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:07.073442Z","updatedAt":"2026-09-04T22:32:09.717830Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"059c656c-bebb-46c6-8704-14906aea7015","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.930481Z","updatedAt":"2026-09-04T22:32:09.746169Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"ec7432db-f4f7-4b70-8d27-709398e881fe","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.897600Z","updatedAt":"2026-09-04T22:32:09.775227Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"1e9bb946-fe3d-4763-a26d-dfeb61fbe230","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:07.007466Z","updatedAt":"2026-09-04T22:32:10.013506Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"b75d83e1-92b3-4d8e-ba52-1b75ab4bcae2","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.829945Z","updatedAt":"2026-09-04T22:32:10.286649Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"d0b0c503-11a5-485a-983d-8290d38c5bfc","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.991410Z","updatedAt":"2026-09-04T22:32:10.498273Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"cd1d1fec-ad16-4559-80df-9b6c4a5c450e","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.024150Z","updatedAt":"2026-09-04T22:32:06.024150Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"a561d9d7-95cc-4c56-a3bc-2a6540ca4468","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.281952Z","updatedAt":"2026-09-04T22:32:06.281952Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"},{"id":"51a9ed28-8cdb-4ce4-9a1a-034d417ad824","content":"0","description":null,"projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":0,"priority":null,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.811831Z","updatedAt":"2026-09-04T22:32:09.596819Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}]`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects
    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e/tasks
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e/tasks
````

### failure-69: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: n0tPgp

- API accepted schema-violating request

    Invalid data should have been rejected
    Expected: 400, 401, 403, 404, 405, 406, 409, 422, 428, 5xx
    Invalid component: positive test case

[200] OK:

    `{"id":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","name":"Inbox","color":"򬅆N󒣎Q","parentId":null,"order":-358,"isFavorite":false,"viewStyle":"LIST","isInboxProject":true,"planningCalendarId":"00000000-0000-0000-0000-000000000001","createdAt":"2026-09-04T22:31:35.240040Z","updatedAt":"2026-09-04T22:33:07.188063451Z"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"6\u007f\ud9a6\udee46\u001c\udb6f\ude3f\u00a4": {}, "\u0087": [{"": 6.696347102564501e+244, "\ud972\ude4cr": 8.576310046334606e+250, "\u00bfu7": false}, 648455969269509414453248], "": [], "\uda05\udf14\udbc1\udce8z\uda15\udf19": [null, null], "\u00c3\u0084": {"": 1.6656504295666104e+16, "\u00cewsq5\u00f1\b\u00d0uD": [], "\u00d7": [null]}, "name": "\u00ee\udbb9\udd12\u0004%!", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e"}' http://taska-backend:8080/filters
    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u00ae\u00c4": false, "name": "Inbox", "planningCalendarId": "00000000-0000-0000-0000-000000000001", "isFavorite": false, "parentId": null}' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"6\u007f\ud9a6\udee46\u001c\udb6f\ude3f\u00a4": {}, "\u0087": [{"": 6.696347102564501e+244, "\ud972\ude4cr": 8.576310046334606e+250, "\u00bfu7": false}, 648455969269509414453248], "": [], "\uda05\udf14\udbc1\udce8z\uda15\udf19": [null, null], "\u00c3\u0084": {"": 1.6656504295666104e+16, "\u00cewsq5\u00f1\b\u00d0uD": [], "\u00d7": [null]}, "name": "\u00ee\udbb9\udd12\u0004%!", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e"}' http://taska-backend:8080/filters
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u00ae\u00c4": false, "name": "Inbox", "planningCalendarId": "00000000-0000-0000-0000-000000000001", "isFavorite": false, "parentId": null}' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
````

### failure-70: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: QnLPeC

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/sections/None","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"6\u007f\ud9a6\udee46\u001c\udb6f\ude3f\u00a4": {}, "\u0087": [{"": 6.696347102564501e+244, "\ud972\ude4cr": 8.576310046334606e+250, "\u00bfu7": false}, 648455969269509414453248], "": [], "\uda05\udf14\udbc1\udce8z\uda15\udf19": [null, null], "\u00c3\u0084": {"": 1.6656504295666104e+16, "\u00cewsq5\u00f1\b\u00d0uD": [], "\u00d7": [null]}, "name": "\u00ee\udbb9\udd12\u0004%!", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e"}' http://taska-backend:8080/filters
    curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/07d591b1-a9b6-41de-9b78-3d2e6811fbe0/tasks
    curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/None
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"6\u007f\ud9a6\udee46\u001c\udb6f\ude3f\u00a4": {}, "\u0087": [{"": 6.696347102564501e+244, "\ud972\ude4cr": 8.576310046334606e+250, "\u00bfu7": false}, 648455969269509414453248], "": [], "\uda05\udf14\udbc1\udce8z\uda15\udf19": [null, null], "\u00c3\u0084": {"": 1.6656504295666104e+16, "\u00cewsq5\u00f1\b\u00d0uD": [], "\u00d7": [null]}, "name": "\u00ee\udbb9\udd12\u0004%!", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e"}' http://taska-backend:8080/filters
curl -X GET -H 'Authorization: [Filtered]' http://taska-backend:8080/filters/07d591b1-a9b6-41de-9b78-3d2e6811fbe0/tasks
curl -X DELETE -H 'Authorization: [Filtered]' http://taska-backend:8080/sections/None
````

### failure-71: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: kTDeU1

- API accepted schema-violating request

    Invalid data should have been rejected
    Expected: 400, 401, 403, 404, 405, 406, 409, 422, 428, 5xx
    Invalid component: positive test case

[201] Created:

    `{"id":"4016e19b-c829-4239-b60c-8d5e834351c4","content":"Åé#dÄ𚧻\u0014Rïì ","description":",򡐗ÎÜ񅚹\u0010","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":null,"parentId":null,"order":-524289,"priority":3,"labels":["kR;É<򇓗","󫏼򓃷H¼ïÞX򬸰","󛓛񥀣°","î½󠊭Ù\u0005Ns","eñ󐧩"],"isCompleted":false,"scheduledAt":null,"dueAt":"1164-06-08T13:23:13Z","allDay":false,"isRecurring":true,"estimateMinutes":774,"mentionContext":"","recurrenceRule":"}ã,𨷋.G","createdAt":"2026-09-04T22:33:13.199386799Z","updatedAt":"2026-09-04T22:33:13.199387139Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"APPOINTMENT"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud85f\ude88\u0097J]\u00de\u0085\u00ae\u00fb\u0083\u00b5\udaf1\udf1c\u00a7\udb26\udf95": [], "": "\u00c6", "\b\u00b1": [7874708], "occurrenceScheduledAt": "2026-09-04T22:33:08.580312Z"}' http://taska-backend:8080/tasks/76d2bb32-c8b4-4a87-baca-6cefd9758072/reopen
    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\udb13\udec1y\u00ab\u0099": [], "Infinity": [-3.7914509559399637e-264], "\u00ca": [36698587], "$\u00cf\ud8da\ude6d\u0092\u0081,3U\ud820\udc78\ub092\u001es'"'"'\u0094": {"\u00e2\u0096\r\u0093\ud813\udd1a\u0099\u00dc\u0000\u008d\u00f6\u001e\u00b2O\u001a'"'"'\u00cf\ud90f\udec1": {"": [true, false]}, "\ud9cf\udf8eV\uda97\udc03\u00f3": []}, "": -13501358}' http://taska-backend:8080/tasks/76d2bb32-c8b4-4a87-baca-6cefd9758072/close
    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud9ca\uddfd\ud8df\uddb1\u00c0\u00d5iX\u00ff+\u00a6\u00d3\u00f1>\u00f8\u00d6>\u00fe\u001f\u00d0\udaab\udc32 ": [[], {"\u00edn": false, "": null, "__main__": 6.106615915461877e+16}, []], "\u0014\u00dc\u00e2\u00ba\ud9c5\ude9c\udbbb\udd82;v\ud93a\udd06\u00d1\u00bd": {"\ud848\udd83\u00d9\u00db\u00a4\rd\u0012E\udbaf\udc29\u00cb\u00d6": 1622, "\u0089\u00ba\u00f0\u00deT": true, "\u00f9\u0095f\u00bdU": false}, "&\ud9bd\udf16\u009a\u00ed\u00fa": [], "1\udb9a\udc86[": 2.1000690041566527e-265, "\u00fbnB": [[-33133284, false, "\udb6e\udd31\u0007\ud914\udfb3\u0097\u008ab\u00db\u00e1"]], "scope": "FROM_THIS", "labels": ["kR;\u00c9<\ud9dd\udcd7", "\udb6c\udffc\uda0c\udcf7H\u00bc\u00ef\u00deX\uda73\ude30", "\udb2d\udcdb\ud954\udc23\u00b0", "\u00ee\u00bd\udb40\udead\u00d9\u009c\u0005Ns", "e\u00f1\udb02\udde9"], "mentionContext": "", "estimateMinutes": 774, "content": "\u00c5\u00e9#d\u00c4\ud82a\uddfb\u0014R\u00ef\u00ec ", "parentId": null, "type": "APPOINTMENT", "description": ",\uda45\udc17\u00ce\u00dc\ud8d5\udeb9\u0010", "order": -524289, "isRecurring": true, "priority": 3, "dueAt": "1164-06-08T00:40:13-12:43", "recurrenceRule": "}\u00e3,\ud863\uddcb.G"}' http://taska-backend:8080/tasks
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud85f\ude88\u0097J]\u00de\u0085\u00ae\u00fb\u0083\u00b5\udaf1\udf1c\u00a7\udb26\udf95": [], "": "\u00c6", "\b\u00b1": [7874708], "occurrenceScheduledAt": "2026-09-04T22:33:08.580312Z"}' http://taska-backend:8080/tasks/76d2bb32-c8b4-4a87-baca-6cefd9758072/reopen
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\udb13\udec1y\u00ab\u0099": [], "Infinity": [-3.7914509559399637e-264], "\u00ca": [36698587], "$\u00cf\ud8da\ude6d\u0092\u0081,3U\ud820\udc78\ub092\u001es'"'"'\u0094": {"\u00e2\u0096\r\u0093\ud813\udd1a\u0099\u00dc\u0000\u008d\u00f6\u001e\u00b2O\u001a'"'"'\u00cf\ud90f\udec1": {"": [true, false]}, "\ud9cf\udf8eV\uda97\udc03\u00f3": []}, "": -13501358}' http://taska-backend:8080/tasks/76d2bb32-c8b4-4a87-baca-6cefd9758072/close
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud9ca\uddfd\ud8df\uddb1\u00c0\u00d5iX\u00ff+\u00a6\u00d3\u00f1>\u00f8\u00d6>\u00fe\u001f\u00d0\udaab\udc32 ": [[], {"\u00edn": false, "": null, "__main__": 6.106615915461877e+16}, []], "\u0014\u00dc\u00e2\u00ba\ud9c5\ude9c\udbbb\udd82;v\ud93a\udd06\u00d1\u00bd": {"\ud848\udd83\u00d9\u00db\u00a4\rd\u0012E\udbaf\udc29\u00cb\u00d6": 1622, "\u0089\u00ba\u00f0\u00deT": true, "\u00f9\u0095f\u00bdU": false}, "&\ud9bd\udf16\u009a\u00ed\u00fa": [], "1\udb9a\udc86[": 2.1000690041566527e-265, "\u00fbnB": [[-33133284, false, "\udb6e\udd31\u0007\ud914\udfb3\u0097\u008ab\u00db\u00e1"]], "scope": "FROM_THIS", "labels": ["kR;\u00c9<\ud9dd\udcd7", "\udb6c\udffc\uda0c\udcf7H\u00bc\u00ef\u00deX\uda73\ude30", "\udb2d\udcdb\ud954\udc23\u00b0", "\u00ee\u00bd\udb40\udead\u00d9\u009c\u0005Ns", "e\u00f1\udb02\udde9"], "mentionContext": "", "estimateMinutes": 774, "content": "\u00c5\u00e9#d\u00c4\ud82a\uddfb\u0014R\u00ef\u00ec ", "parentId": null, "type": "APPOINTMENT", "description": ",\uda45\udc17\u00ce\u00dc\ud8d5\udeb9\u0010", "order": -524289, "isRecurring": true, "priority": 3, "dueAt": "1164-06-08T00:40:13-12:43", "recurrenceRule": "}\u00e3,\ud863\uddcb.G"}' http://taska-backend:8080/tasks
````

### failure-72: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: rdLUbF

- Server error

[500] Internal Server Error:

    `{"detail":"Internal error","instance":"/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc/occurrences/0165-05-27T13%3A19%3A33-19%3A36","status":500,"title":"Internal Server Error"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{".Y\u00d5\u0099\u00e2\u00be": [{"\u0003Rl\u00f5\udbdb\ude8a\u009d": {"}d\ud91f\udd93\u0088\u00d9\u009e\ud895\udc96\ud813\udd46\uda00\ude91": null}, "o": {}, "": 2063}], "\u0098\u008a": [-6458], "\u009e\u00c8\u001d\u00d5": [-4.185228754984408e+16, -1.7976931348623157e+308, []], "\u00ca\u008e\u00d8j\udbf2\udfab\u001f\u00e7": [[-9764], 5.832841007385643e+16, {}], "\u00d19k(\ud979\udeae\ud8d4\udedc": [["\u001d\u00fa\u00b2\u00e8N", null], {}], "occurrenceScheduledAt": "1715-04-06T14:57:02.307832Z"}' http://taska-backend:8080/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc/close
    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud990\udc76\u00b1E\u00bau\udae0\udcfa$\ud9ad\udf9e[ R": [false, {"h\u00f7\uda62\ude40\u0085\ud982\udf8f\u001f\u00e6\u009c\u00ba": -2.4427477539307998e+239, "": false, "%\u001d*\u00ae\u00aek^": 1.7447922595451596e+16}, null], "\u00bb\u00a2\f\udb87\ude55\u00fa": null, "\u00fd(\u0007": {"\u00d0": {"": {"\u008f\udb31\ude15V\u009b\u00ce*\ud848\udca5\u00d0=\u00f0": -140, "U": null, "\u009c\u0015\u00ef\u0091\u27a5\ud8b8\udeae\u00b9\u00b6": -322}}}, "title": "\r\u00a2\u00e9\"\u00d4\u0007t\u00a1"}' http://taska-backend:8080/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc/occurrences/0165-05-27T13%3A19%3A33-19%3A36
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{".Y\u00d5\u0099\u00e2\u00be": [{"\u0003Rl\u00f5\udbdb\ude8a\u009d": {"}d\ud91f\udd93\u0088\u00d9\u009e\ud895\udc96\ud813\udd46\uda00\ude91": null}, "o": {}, "": 2063}], "\u0098\u008a": [-6458], "\u009e\u00c8\u001d\u00d5": [-4.185228754984408e+16, -1.7976931348623157e+308, []], "\u00ca\u008e\u00d8j\udbf2\udfab\u001f\u00e7": [[-9764], 5.832841007385643e+16, {}], "\u00d19k(\ud979\udeae\ud8d4\udedc": [["\u001d\u00fa\u00b2\u00e8N", null], {}], "occurrenceScheduledAt": "1715-04-06T14:57:02.307832Z"}' http://taska-backend:8080/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc/close
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\ud990\udc76\u00b1E\u00bau\udae0\udcfa$\ud9ad\udf9e[ R": [false, {"h\u00f7\uda62\ude40\u0085\ud982\udf8f\u001f\u00e6\u009c\u00ba": -2.4427477539307998e+239, "": false, "%\u001d*\u00ae\u00aek^": 1.7447922595451596e+16}, null], "\u00bb\u00a2\f\udb87\ude55\u00fa": null, "\u00fd(\u0007": {"\u00d0": {"": {"\u008f\udb31\ude15V\u009b\u00ce*\ud848\udca5\u00d0=\u00f0": -140, "U": null, "\u009c\u0015\u00ef\u0091\u27a5\ud8b8\udeae\u00b9\u00b6": -322}}}, "title": "\r\u00a2\u00e9\"\u00d4\u0007t\u00a1"}' http://taska-backend:8080/tasks/d0b0c503-11a5-485a-983d-8290d38c5bfc/occurrences/0165-05-27T13%3A19%3A33-19%3A36
````

### failure-73: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: obcJH9

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx
    Hint: The request body contains 5 additional properties not defined in the schema (``, `j`, ` º9󷉺𻅴0`` and 2 more). The server likely rejects unexpected fields. Add `additionalProperties: false` to your schema to prevent this.

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/tasks/23fc2032-30ef-19dd-a9fc-93c0d443f641/reopen","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"occurrenceScheduledAt": "2026-09-04T22:33:22.869451119Z"}' http://taska-backend:8080/tasks/a835f014-c296-4cc3-acbe-d5dddab40b3c/close
    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/comments?task_id=a835f014-c296-4cc3-acbe-d5dddab40b3c&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e'
    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": [{}], "j": {}, "\u00fc\u0088)\ud8b9\udd2b\ud997\udd39": [false, -801, null], "\u00c5\u0085\u0085": false, "\u00a0\u00ba9\udb9c\ude7a\ud8ac\udd740`\u0081": {}, "occurrenceScheduledAt": "0658-01-06T20:44:47.8-19:59"}' http://taska-backend:8080/tasks/23fc2032-30ef-19dd-a9fc-93c0d443f641/reopen
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"occurrenceScheduledAt": "2026-09-04T22:33:22.869451119Z"}' http://taska-backend:8080/tasks/a835f014-c296-4cc3-acbe-d5dddab40b3c/close
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/comments?task_id=a835f014-c296-4cc3-acbe-d5dddab40b3c&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e'
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": [{}], "j": {}, "\u00fc\u0088)\ud8b9\udd2b\ud997\udd39": [false, -801, null], "\u00c5\u0085\u0085": false, "\u00a0\u00ba9\udb9c\ude7a\ud8ac\udd740`\u0081": {}, "occurrenceScheduledAt": "0658-01-06T20:44:47.8-19:59"}' http://taska-backend:8080/tasks/23fc2032-30ef-19dd-a9fc-93c0d443f641/reopen
````

### failure-74: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: p0ymPv

- Response violates schema (2 violations)

    null is not of type "integer"

    Validated against the response schema for status code 200.

    Schema at /properties/estimateMinutes:

        {
            "type": "integer",
            "format": "int32"
        }

    Value:

        null

    null is not of type "string"

    Validated against the response schema for status code 200.

    Schema at /properties/completedAt:

        {
            "type": "string",
            "format": "date-time"
        }

    Value:

        null

[200] OK:

    `{"id":"b8bc2620-4685-4d15-b9d7-c6370337487b","content":"__main__","description":"__main__","projectId":"62d0f6c0-9b92-4435-8ded-70c9c98f7c5e","sectionId":"49312bdb-663e-452c-94c6-9b656894b4b5","parentId":null,"order":0,"priority":1,"labels":[],"isCompleted":false,"scheduledAt":null,"dueAt":null,"allDay":false,"isRecurring":false,"estimateMinutes":null,"mentionContext":null,"recurrenceRule":null,"createdAt":"2026-09-04T22:32:06.362216Z","updatedAt":"2026-09-04T22:33:27.148336550Z","completedAt":null,"instanceId":null,"occurrenceScheduledAt":null,"isVirtual":null,"rruleEndsAt":null,"type":"TODO"}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\udbc9\udcda": -261, "": ["", 5.717391565616968e+16, "\udb51\udd4eT\uda58\udd7a\ud96f\ude1f\u00eb\u00a6\u0011\udab9\udd3a"], "\u0086Q\ud957\udc1e\u008b\u00017\u00b1tj\u0096": []}' http://taska-backend:8080/tasks/b8bc2620-4685-4d15-b9d7-c6370337487b/reopen
    curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"~f\u00b5`x": 5.9261875338336296e+16, "B": {"\u00ae\"\u00a7": [false, true, -46477], "\u023e": {"\ud9c5\ude6a\u00d9": -467785}, "\u00b2": {"__main__": true}}, "\ud9cf\udf1d": null, "\ud9a4\udc93": {"\u0016\\1\uda2a\udc20\u00fc\udbf8\udd21\u00e1\u00cb\uda1b\udfe8\u00d4\ud977\ude7d\u0099\u00a6\u00f4\u00aer\u00c0e\u00ee\ud8b7\udf0b\ud9f0\ude6e$q\f\u0094\ud980\ude8e": []}, " ": [[true, 6.505897048629285e+16, 7.382641927727514e-62], {}], "recurrenceRule": null, "type": "TODO", "order": 0, "scheduledAt": null, "content": "__main__", "isRecurring": false, "parentId": null, "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e", "priority": 1, "mentionContext": null, "estimateMinutes": null, "description": "__main__", "dueAt": null, "sectionId": "49312bdb-663e-452c-94c6-9b656894b4b5", "allDay": false, "labels": []}' http://taska-backend:8080/tasks/b8bc2620-4685-4d15-b9d7-c6370337487b
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\udbc9\udcda": -261, "": ["", 5.717391565616968e+16, "\udb51\udd4eT\uda58\udd7a\ud96f\ude1f\u00eb\u00a6\u0011\udab9\udd3a"], "\u0086Q\ud957\udc1e\u008b\u00017\u00b1tj\u0096": []}' http://taska-backend:8080/tasks/b8bc2620-4685-4d15-b9d7-c6370337487b/reopen
curl -X PUT -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"~f\u00b5`x": 5.9261875338336296e+16, "B": {"\u00ae\"\u00a7": [false, true, -46477], "\u023e": {"\ud9c5\ude6a\u00d9": -467785}, "\u00b2": {"__main__": true}}, "\ud9cf\udf1d": null, "\ud9a4\udc93": {"\u0016\\1\uda2a\udc20\u00fc\udbf8\udd21\u00e1\u00cb\uda1b\udfe8\u00d4\ud977\ude7d\u0099\u00a6\u00f4\u00aer\u00c0e\u00ee\ud8b7\udf0b\ud9f0\ude6e$q\f\u0094\ud980\ude8e": []}, " ": [[true, 6.505897048629285e+16, 7.382641927727514e-62], {}], "recurrenceRule": null, "type": "TODO", "order": 0, "scheduledAt": null, "content": "__main__", "isRecurring": false, "parentId": null, "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e", "priority": 1, "mentionContext": null, "estimateMinutes": null, "description": "__main__", "dueAt": null, "sectionId": "49312bdb-663e-452c-94c6-9b656894b4b5", "allDay": false, "labels": []}' http://taska-backend:8080/tasks/b8bc2620-4685-4d15-b9d7-c6370337487b
````

### failure-75: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: 1ZKkRT

- Response violates schema

    "type" is a required property

    Validated against the response schema for status code 400.

    Schema at /ProblemDetails:

        {
            "required": [
                "type",
                "title",
                "status"
            ],
            "type": "object",
            "description": "RFC 9457 problem details returned for API errors.",
            "properties": {
                "type": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Problem type identifier."
                },
                "title": {
                    "type": "string",
                    "description": "Short, human-readable problem summary."
                },
                "status": {
                    "type": "integer",
                    "minimum": 100,
                    "maximum": 599,
                    "description": "HTTP status code."
                },
                "detail": {
                    "type": "string",
                    "description": "Problem details specific to this occurrence."
                },
                "instance": {
                    "type": "string",
                    "format": "uri-reference",
                    "description": "Identifier for this occurrence."
                },
                "code": {
                    "type": "string",
                    "description": "Stable, machine-readable Taska error code."
                },
                "violations": {
                    "type": "array",
                    "items": {
                        "$ref": "#/Violation"
                    }
                }
            }
        }

    Value:

        {
            "detail": "Missing expected token, last token: }\u00e3,\ud863\uddcb.G",
            "instance": "/tasks",
            "status": 400,
            "title": "Bad Request"
        }

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx

[400] Bad Request:

    `{"detail":"Missing expected token, last token: }ã,𨷋.G","instance":"/tasks","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?label=q%C3%96%1EU%F1%87%9C%82%C3%B2E%14%C2%B3%C3%BA%C2%A1W&from=0016-07-27&singleDate=2000-01-11&section_id=ebdd2403-13a1-4bb1-b199-729707697429&to=0027-01-05&filter=.exe&show_completed=false&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e'
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' 'http://taska-backend:8080/tasks?label=q%C3%96%1EU%F1%87%9C%82%C3%B2E%14%C2%B3%C3%BA%C2%A1W&from=0016-07-27&singleDate=2000-01-11&section_id=ebdd2403-13a1-4bb1-b199-729707697429&to=0027-01-05&filter=.exe&show_completed=false&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e'
````

### failure-76: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: gz2HRi

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx
    Hint: The request body contains 5 additional properties not defined in the schema (``, `�û�𯩐�õ"`, `&D¢×Ç𡨯󕎩Õiû` and 2 more). The server likely rejects unexpected fields. Add `additionalProperties: false` to your schema to prevent this.

[400] Bad Request:

    `{"detail":"Validation failed","instance":"/labels","status":400,"title":"Bad Request","errors":{"name":"must not be blank"}}`

Reproduce with:

    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0005\u00fb\u000f\ud87e\ude50\u0002\u00f5\u0081\"\u0091": [], "": {}, "_\udaed\udcab\u00a0\u00a9\ud829\udd9d": {}, "\ud964\udd48\u00cb\u00f4\u0017": {"\u00fe": [true], "": [], "\u00a0\u0083J+\ud9e0\udcc1N\u00b1\u00f7\u00cco\u00be\u00e5\u000f\u00a6\u001c\u00c3\u00c3\u00bd\u0087": {}}, "&D\u00a2\u00d7\u00c7\ud846\ude2f\udb14\udfa9\u00d5i\u00fb": [-2309384, 508444, 1972587992223588.0], "name": "\u001c", "order": -1346, "color": "\u00bc\u0095}\u00844\ud869\udf79\uda5b\udee2\u0095\u00bb"}' http://taska-backend:8080/labels
````

Reproduction:

````shell
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"\u0005\u00fb\u000f\ud87e\ude50\u0002\u00f5\u0081\"\u0091": [], "": {}, "_\udaed\udcab\u00a0\u00a9\ud829\udd9d": {}, "\ud964\udd48\u00cb\u00f4\u0017": {"\u00fe": [true], "": [], "\u00a0\u0083J+\ud9e0\udcc1N\u00b1\u00f7\u00cco\u00be\u00e5\u000f\u00a6\u001c\u00c3\u00c3\u00bd\u0087": {}}, "&D\u00a2\u00d7\u00c7\ud846\ude2f\udb14\udfa9\u00d5i\u00fb": [-2309384, 508444, 1972587992223588.0], "name": "\u001c", "order": -1346, "color": "\u00bc\u0095}\u00844\ud869\udf79\uda5b\udee2\u0095\u00bb"}' http://taska-backend:8080/labels
````

### failure-77: Stateful tests

- Test case: `Stateful tests`
- Kind: `failure`
- Message: failure

````text
1. Test Case ID: AXoIbP

- API rejected schema-compliant request

    Valid data should have been accepted
    Expected: 2xx, 401, 403, 404, 409, 5xx
    Hint: The request body contains 5 additional properties not defined in the schema (``, `<`, `` and 2 more). The server likely rejects unexpected fields. Add `additionalProperties: false` to your schema to prevent this.

[400] Bad Request:

    `{"detail":"Invalid request body","instance":"/time-entries","status":400,"title":"Bad Request"}`

Reproduce with:

    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/tasks?show_completed=false&label=&to=6413-01-08&singleDate=0976-10-07&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e&section_id=a48fbd96-4d49-4b42-b98d-18521a0e9cb1'
    curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
    curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": {"\u00a0\u00cb\ud98c\udf46\u0016(\u00bd\udb78\udf44\u0014\u00ff\f\ud941\udfb6\udbe1\ude33": 7997962203934142.0, "\u0096": null, "\u00c6\uda9a\udc587": "\udbe9\udf6e\"\udbeb\ude53\u00a0\u009aU\u001b"}, "\u009d": [[], [-2.220446049250313e-16], {}], "\u00d5}": [-1.9967092008064353e+62], "<": {"\u00ffp\u0002\u00f5K": {}, "\u00c5\ud9ec\udecf\ud9d7\udc7e\u00b8\u00bd}": [null, -234629898288723263488]}, "\u00db\ud833\uddb1D\u0015\ud9f9\udc75\u00bb\u0013\u00f4J9\u00e4": {"(\u256f\u00b0\u25a1\u00b0\uff09\u256f\ufe35 \u253b\u2501\u253b)": null}, "endAt": "2026-09-04T22:33:12.166473Z", "startAt": "2657-03-21T13:10:19+22:35", "description": "\u0014", "notes": "__main__", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e"}' http://taska-backend:8080/time-entries
````

Reproduction:

````shell
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' 'http://taska-backend:8080/tasks?show_completed=false&label=&to=6413-01-08&singleDate=0976-10-07&project_id=62d0f6c0-9b92-4435-8ded-70c9c98f7c5e&section_id=a48fbd96-4d49-4b42-b98d-18521a0e9cb1'
curl -X GET -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' http://taska-backend:8080/projects/62d0f6c0-9b92-4435-8ded-70c9c98f7c5e
curl -X POST -H 'Authorization: [Filtered]' -H 'Content-Type: application/json' -d '{"": {"\u00a0\u00cb\ud98c\udf46\u0016(\u00bd\udb78\udf44\u0014\u00ff\f\ud941\udfb6\udbe1\ude33": 7997962203934142.0, "\u0096": null, "\u00c6\uda9a\udc587": "\udbe9\udf6e\"\udbeb\ude53\u00a0\u009aU\u001b"}, "\u009d": [[], [-2.220446049250313e-16], {}], "\u00d5}": [-1.9967092008064353e+62], "<": {"\u00ffp\u0002\u00f5K": {}, "\u00c5\ud9ec\udecf\ud9d7\udc7e\u00b8\u00bd}": [null, -234629898288723263488]}, "\u00db\ud833\uddb1D\u0015\ud9f9\udc75\u00bb\u0013\u00f4J9\u00e4": {"(\u256f\u00b0\u25a1\u00b0\uff09\u256f\ufe35 \u253b\u2501\u253b)": null}, "endAt": "2026-09-04T22:33:12.166473Z", "startAt": "2657-03-21T13:10:19+22:35", "description": "\u0014", "notes": "__main__", "projectId": "62d0f6c0-9b92-4435-8ded-70c9c98f7c5e"}' http://taska-backend:8080/time-entries
````

## Raw artifacts

- JUnit: `reports/raw/junit.xml`
- NDJSON: `reports/raw/events.ndjson`
- Schemathesis log: `reports/raw/schemathesis.log`
