export interface AppConfiguration {
  apiUrl: string;
  oidc: {
    authority: string;
    clientId: string;
    redirectUri: string;
    postLogoutRedirectUri: string;
    scope: string;
  };
}
