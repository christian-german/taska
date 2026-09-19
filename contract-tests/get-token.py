#!/usr/bin/env python3
"""Log in to Authentik and obtain a development OAuth2 token using PKCE."""

from __future__ import annotations

import base64
import hashlib
import http.cookiejar
import json
import os
import secrets
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Any


def required_environment(name: str) -> str:
    value = os.environ.get(name)
    if not value:
        print(f"Missing required environment variable: {name}", file=sys.stderr)
        raise SystemExit(1)
    return value


class NoRedirect(urllib.request.HTTPRedirectHandler):
    """Expose redirects so that the OAuth callback can be intercepted."""

    def redirect_request(self, request, file_pointer, code, message, headers, new_url):
        return None


@dataclass(frozen=True)
class Response:
    status: int
    headers: Any
    body: bytes

    def json(self) -> dict[str, Any]:
        payload = json.loads(self.body.decode("utf-8"))
        if not isinstance(payload, dict):
            raise RuntimeError("Authentik returned an unexpected JSON value")
        return payload


class AuthentikSession:
    def __init__(self, base_url: str) -> None:
        self.base_url = base_url.rstrip("/")
        self.cookies = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(self.cookies), NoRedirect()
        )

    def request(
        self,
        url: str,
        *,
        data: bytes | None = None,
        headers: dict[str, str] | None = None,
        method: str = "GET",
    ) -> Response:
        absolute_url = urllib.parse.urljoin(f"{self.base_url}/", url)
        request = urllib.request.Request(
            absolute_url,
            data=data,
            headers=headers or {},
            method=method,
        )
        try:
            with self.opener.open(request, timeout=15) as response:
                return Response(response.status, response.headers, response.read())
        except urllib.error.HTTPError as error:
            return Response(error.code, error.headers, error.read())

    def csrf_token(self) -> str | None:
        for cookie in self.cookies:
            if cookie.name == "authentik_csrf":
                return cookie.value
        return None


def error_description(response: Response) -> str:
    body = response.body.decode("utf-8", errors="replace")
    try:
        payload = json.loads(body)
    except json.JSONDecodeError:
        return body[:300] or "empty response"
    if isinstance(payload, dict):
        detail = payload.get("detail") or payload.get("error_description") or payload.get("error")
        if detail:
            return str(detail)
        errors = payload.get("response_errors") or payload.get("non_field_errors")
        if errors:
            return json.dumps(errors, ensure_ascii=False)
    return json.dumps(payload, ensure_ascii=False)[:300]


def flow_location(url: str) -> tuple[str, str, bool] | None:
    parsed = urllib.parse.urlsplit(url)
    parts = [part for part in parsed.path.split("/") if part]
    if len(parts) >= 3 and parts[0] == "flows" and parts[1] == "-":
        return parts[2], parsed.query, False
    if len(parts) >= 3 and parts[0] == "if" and parts[1] == "flow":
        return parts[2], parsed.query, False
    if len(parts) >= 5 and parts[:4] == ["api", "v3", "flows", "executor"]:
        executor_query = urllib.parse.parse_qs(parsed.query).get("query", [""])[0]
        return parts[4], executor_query, True
    return None


def run_flow(
    session: AuthentikSession,
    location: str,
    username: str,
    password: str,
) -> str:
    parsed_flow = flow_location(location)
    if parsed_flow is None:
        raise RuntimeError(f"Unexpected Authentik flow URL: {location}")
    slug, original_query, is_executor = parsed_flow

    if is_executor:
        executor_url = location
    else:
        # Match normal browser behaviour before calling the flow API.
        page = session.request(location)
        if page.status in range(300, 400):
            redirect = page.headers.get("Location")
            if not redirect:
                raise RuntimeError("Authentik returned a redirect without Location")
            redirected_url = urllib.parse.urljoin(location, redirect)
            redirected_flow = flow_location(redirected_url)
            if redirected_flow is None or not redirected_flow[2]:
                return redirected_url
            executor_url = redirected_url
        else:
            if page.status >= 400:
                raise RuntimeError(
                    f"Authentik flow page returned HTTP {page.status}: "
                    f"{error_description(page)}"
                )
            executor_query = urllib.parse.urlencode({"query": original_query})
            executor_url = (
                f"{session.base_url}/api/v3/flows/executor/{slug}/?{executor_query}"
            )
    response = session.request(executor_url)

    for _ in range(20):
        if response.status in range(300, 400):
            redirect = response.headers.get("Location")
            if not redirect:
                raise RuntimeError("Authentik returned a redirect without Location")
            return urllib.parse.urljoin(executor_url, redirect)
        if response.status >= 400:
            raise RuntimeError(
                f"Authentik flow {slug} returned HTTP {response.status}: "
                f"{error_description(response)}"
            )

        flow_challenge = response.json()
        component = flow_challenge.get("component")
        if not isinstance(component, str):
            raise RuntimeError(
                f"Authentik flow {slug} returned no component: "
                f"{json.dumps(flow_challenge, ensure_ascii=False)[:500]}"
            )

        if component in {"xak-flow-redirect", "ak-stage-flow-redirect"}:
            redirect = (
                flow_challenge.get("to")
                or flow_challenge.get("url")
                or flow_challenge.get("redirect")
            )
            if not isinstance(redirect, str) or not redirect:
                raise RuntimeError(f"Authentik flow {slug} returned no redirect target")
            return urllib.parse.urljoin(executor_url, redirect)

        payload: dict[str, Any] = {"component": component}
        if component == "ak-stage-identification":
            payload["uid_field"] = username
        elif component == "ak-stage-password":
            payload["password"] = password
        elif component == "ak-stage-consent":
            payload["consent_input"] = "authorize"
        elif component == "ak-stage-access-denied":
            raise RuntimeError("Authentik denied access during the authorization flow")
        else:
            raise RuntimeError(
                f"Authentik requested unsupported flow component {component!r}: "
                f"{json.dumps(flow_challenge, ensure_ascii=False)[:500]}"
            )

        headers = {
            "Content-Type": "application/json",
            "Origin": session.base_url,
            "Referer": location,
        }
        if csrf_token := session.csrf_token():
            headers["X-authentik-CSRF"] = csrf_token

        response = session.request(
            executor_url,
            data=json.dumps(payload).encode("utf-8"),
            headers=headers,
            method="POST",
        )

    raise RuntimeError(f"Authentik flow {slug} did not complete")


def obtain_token() -> str:
    base_url = required_environment("AUTHENTIK_BASE_URL")
    token_url = required_environment("TOKEN_URL")
    client_id = required_environment("TOKEN_CLIENT_ID")
    username = required_environment("TOKEN_USERNAME")
    password = required_environment("TOKEN_PASSWORD")
    scope = os.environ.get("TOKEN_SCOPE", "openid profile email")
    redirect_uri = os.environ.get("TOKEN_REDIRECT_URI", "http://localhost:4200/callback")

    verifier = secrets.token_urlsafe(64)
    challenge = base64.urlsafe_b64encode(
        hashlib.sha256(verifier.encode("ascii")).digest()
    ).rstrip(b"=").decode("ascii")
    state = secrets.token_urlsafe(24)

    authorize_url = f"{base_url.rstrip('/')}/application/o/authorize/?" + urllib.parse.urlencode(
        {
            "client_id": client_id,
            "redirect_uri": redirect_uri,
            "response_type": "code",
            "scope": scope,
            "state": state,
            "code_challenge": challenge,
            "code_challenge_method": "S256",
        }
    )
    session = AuthentikSession(base_url)
    current_url = authorize_url

    for _ in range(20):
        parsed = urllib.parse.urlsplit(current_url)
        callback = urllib.parse.urlsplit(redirect_uri)
        if (parsed.scheme, parsed.netloc, parsed.path) == (
            callback.scheme,
            callback.netloc,
            callback.path,
        ):
            query = urllib.parse.parse_qs(parsed.query)
            returned_state = query.get("state", [None])[0]
            if returned_state != state:
                raise RuntimeError("Authentik returned an invalid OAuth2 state")
            oauth_error = query.get("error_description", query.get("error", [None]))[0]
            if oauth_error:
                raise RuntimeError(f"Authentik rejected authorization: {oauth_error}")
            code = query.get("code", [None])[0]
            if not code:
                raise RuntimeError("Authentik callback did not contain an authorization code")
            break

        if flow_location(current_url) is not None:
            current_url = run_flow(session, current_url, username, password)
            continue

        response = session.request(current_url)
        if response.status not in range(300, 400):
            raise RuntimeError(
                f"OAuth2 authorization at {current_url} returned HTTP {response.status}: "
                f"{error_description(response)}"
            )
        redirect = response.headers.get("Location")
        if not redirect:
            raise RuntimeError("OAuth2 authorization redirect has no Location header")
        current_url = urllib.parse.urljoin(current_url, redirect)
    else:
        raise RuntimeError("OAuth2 authorization did not reach the callback")

    token_form = urllib.parse.urlencode(
        {
            "grant_type": "authorization_code",
            "client_id": client_id,
            "redirect_uri": redirect_uri,
            "code": code,
            "code_verifier": verifier,
        }
    ).encode("utf-8")
    token_response = session.request(
        token_url,
        data=token_form,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )
    if token_response.status >= 400:
        raise RuntimeError(
            f"Token endpoint returned HTTP {token_response.status}: "
            f"{error_description(token_response)}"
        )
    token_payload = token_response.json()
    access_token = token_payload.get("access_token")
    if not isinstance(access_token, str) or not access_token:
        raise RuntimeError("Token response did not contain access_token")
    return access_token


def main() -> int:
    timeout_seconds = float(os.environ.get("TOKEN_TIMEOUT_SECONDS", "120"))
    deadline = time.monotonic() + timeout_seconds
    delay_seconds = 0.5
    last_error = "Authentik is not ready"

    while time.monotonic() < deadline:
        try:
            print(obtain_token())
            return 0
        except (RuntimeError, urllib.error.URLError, TimeoutError, json.JSONDecodeError) as error:
            last_error = f"{type(error).__name__}: {error}"

        remaining = deadline - time.monotonic()
        if remaining > 0:
            time.sleep(min(delay_seconds, remaining))
            delay_seconds = min(delay_seconds * 1.5, 5.0)

    print(
        f"Unable to obtain an OAuth2 token from Authentik within "
        f"{timeout_seconds:g}s ({last_error}).",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
