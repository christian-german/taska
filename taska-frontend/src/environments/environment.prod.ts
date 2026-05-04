export const environment = {
  production: true,
  apiUrl: "https://api-taska.atlascore.dev",
  oidc: {
    authority: "https://authentik.atlascore.dev/application/o/taska",
    "clientId": "mE2vXI67I43D8fmclgsjHKwt42W4dkDpXJQUOQEJ",
    "redirectUri": "https://taska.atlascore.dev/callback",
    "postLogoutRedirectUri": "https://taska.atlascore.dev",
    "scope": "openid profile email"
  }
};
