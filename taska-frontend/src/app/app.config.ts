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
import {firstValueFrom, of, switchMap, filter, take, timeout} from 'rxjs';
import {catchError} from 'rxjs/operators';

function initializeAuth(oidcSecurityService: OidcSecurityService) {
  return () =>
    firstValueFrom(
      oidcSecurityService.checkAuth().pipe(
        switchMap(({ isAuthenticated }) => {
          if (isAuthenticated) {
            return of(null);
          }

          // Access token expiré ou absent — on tente un refresh silencieux.
          // Si un refresh token existe en localStorage, forceRefreshSession()
          // l'utilisera directement sans redirection.
          return oidcSecurityService.forceRefreshSession().pipe(
            catchError(() => of(null))
          );
        }),
        // Attendre que userData$ ait une valeur non-nulle avant de laisser
        // l'app s'initialiser — évite le "?" au démarrage dû au décalage async
        // entre la fin du refresh et l'émission de userData$.
        switchMap(() =>
          oidcSecurityService.userData$.pipe(
            filter(({ userData }) => userData !== null),
            take(1),
            timeout(3000),
            catchError(() => of(null))
          )
        ),
        catchError(() => of(null))
      )
    );
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
          ignoreNonceAfterRefresh: true,
          disableRefreshIdTokenAuthTimeValidation: true,
          renewTimeBeforeTokenExpiresInSeconds: 30,
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

