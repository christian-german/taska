export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  oidc: {
    authority: 'http://localhost:8000/application/o/taska',
    clientId: 'taska-client',
    redirectUri: 'http://localhost:4200/callback',
    postLogoutRedirectUri: 'http://localhost:4200',
    scope: 'openid profile email offline_access'
  }
};
