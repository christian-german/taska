import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-callback',
  standalone: true,
  template: '<div class="flex justify-content-center p-5">Authentification en cours...</div>',
})
export class CallbackComponent implements OnInit {
  constructor(private oidcSecurityService: OidcSecurityService, private router: Router) {}

  ngOnInit() {
    this.oidcSecurityService.checkAuth().subscribe(({ isAuthenticated }) => {
      if (isAuthenticated) {
        this.router.navigate(['/']);
      } else {
        // Gérer l'échec d'authentification si nécessaire
        this.router.navigate(['/']);
      }
    });
  }
}
