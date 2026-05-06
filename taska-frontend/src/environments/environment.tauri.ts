export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  oidc: {
    authority: 'http://localhost:8000/application/o/taska',
    clientId: 'taska-client',
    redirectUri: 'taska://callback',
    postLogoutRedirectUri: 'taska://callback',
    scope: 'openid profile email offline_access'
  }
};
