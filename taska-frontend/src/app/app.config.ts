import {APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners} from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {routes} from './app.routes';
import {
  AbstractSecurityStorage,
  authInterceptor,
  DefaultLocalStorageService,
  OidcSecurityService,
  provideAuth
} from 'angular-auth-oidc-client';
import {environment} from '../environments/environment';
import {take} from 'rxjs';

function initializeAuth(oidc: OidcSecurityService) {
  return () => oidc.checkAuth().pipe(take(1));
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor()])),
    provideAnimationsAsync(),
    provideAuth({
        config: {
          authority: environment.oidc.authority,
          redirectUrl: environment.oidc.redirectUri,
          postLogoutRedirectUri: environment.oidc.postLogoutRedirectUri,
          clientId: environment.oidc.clientId,
          scope: environment.oidc.scope,
          responseType: 'code',
          silentRenew: true,
          useRefreshToken: true,
          secureRoutes: [environment.apiUrl]
        }
      }
    ),
    {provide: AbstractSecurityStorage, useClass: DefaultLocalStorageService},
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [OidcSecurityService],
      multi: true,
    }
  ]
};

