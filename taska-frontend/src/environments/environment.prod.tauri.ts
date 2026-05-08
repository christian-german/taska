export const environment = {
  production: true,
  apiUrl: 'https://api-taska.atlascore.dev',
  oidc: {
    authority: 'https://authentik.atlascore.dev/application/o/taska',
    clientId: 'mE2vXI67I43D8fmclgsjHKwt42W4dkDpXJQUOQEJ',
    redirectUri: 'http://tauri.localhost/callback',
    postLogoutRedirectUri: 'http://tauri.localhost',
    scope: 'openid profile email offline_access'
  }
};
