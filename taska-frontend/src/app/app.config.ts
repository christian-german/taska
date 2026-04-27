import {APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners} from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {routes} from './app.routes';
import {ConfigService} from './core/services/config.service';
import {authInterceptor, OidcSecurityService, provideAuth, StsConfigLoader} from 'angular-auth-oidc-client';
import {map} from 'rxjs/operators';
import {firstValueFrom} from 'rxjs';

export class DynamicConfigLoader implements StsConfigLoader {

  constructor(private configService: ConfigService) {}

  loadConfigs() {
    return this.configService.configObservable.pipe(
      map((config) => {
        const authority = config.oidc.authority;

        return [{
          authority: authority,
          redirectUrl: config.oidc.redirectUri,
          postLogoutRedirectUri: config.oidc.postLogoutRedirectUri,
          clientId: config.oidc.clientId,
          scope: config.oidc.scope,
          responseType: 'code',
          silentRenew: true,
          useRefreshToken: true,
          secureRoutes: [config.apiUrl],
        }];
      })
    );
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor()])),
    provideAnimationsAsync(),
    provideAuth({
      loader: {
        provide: StsConfigLoader,
        useFactory: (configService: ConfigService) => new DynamicConfigLoader(configService),
        deps: [ConfigService],
      },
    }),
    {
      provide: APP_INITIALIZER,
      useFactory: (configService: ConfigService, oidcSecurityService: OidcSecurityService) => async () => {
        await configService.loadConfig();
        // Maintenant la config est dispo, on peut init OIDC
        await firstValueFrom(oidcSecurityService.checkAuth());
      },
      deps: [ConfigService, OidcSecurityService],
      multi: true
    }
  ]
};

