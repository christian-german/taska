import { Routes } from '@angular/router';
import {CallbackComponent} from './shared/components/callback/callback.component';
import {AutoLoginPartialRoutesGuard} from 'angular-auth-oidc-client';

export const routes: Routes = [
  { path: 'callback', component: CallbackComponent },
  { path: '', redirectTo: 'inbox', pathMatch: 'full' },
  {
    path: 'inbox',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/inbox/inbox.component').then(m => m.InboxComponent)
  },
  {
    path: 'today',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/today/today.component').then(m => m.TodayComponent)
  },
  {
    path: 'upcoming',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/upcoming/upcoming.component').then(m => m.UpcomingComponent)
  },
  {
    path: 'project/:id',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/project-view/project-view.component').then(m => m.ProjectViewComponent)
  },
  {
    path: 'labels',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/labels/labels.component').then(m => m.LabelsComponent)
  },
  {
    path: 'label/:name',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/label-tasks/label-tasks.component').then(m => m.LabelTasksComponent)
  },
  {
    path: 'filters',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/filters/filters.component').then(m => m.FiltersComponent)
  },
  {
    path: 'filter/:id',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/filter-tasks/filter-tasks.component').then(m => m.FilterTasksComponent)
  },
  { path: '**', redirectTo: '' }
];
