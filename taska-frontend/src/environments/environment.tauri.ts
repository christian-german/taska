export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  oidc: {
    authority: 'http://localhost:8000/application/o/taska',
    clientId: 'taska-client',
    redirectUri: 'http://127.0.0.1:1430/callback',
    postLogoutRedirectUri: 'http://tauri.localhost',
    scope: 'openid profile email offline_access'
  }
};
