import { Routes } from '@angular/router';
import { AutoLoginPartialRoutesGuard } from 'angular-auth-oidc-client';
import { CallbackComponent } from './shared/components/callback/callback.component';

export const routes: Routes = [
  { path: 'callback', component: CallbackComponent },
  { path: '', redirectTo: 'today', pathMatch: 'full' },
  {
    path: 'inbox',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/inbox/inbox.component').then(m => m.InboxComponent),
  },
  {
    path: 'today',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/today/today.component').then(m => m.TodayComponent),
  },
  {
    path: 'week',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/week/week.component').then(m => m.WeekComponent),
  },
  {
    path: 'done',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/done/done.component').then(m => m.DoneComponent),
  },
  {
    path: 'stats',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/stats/stats.component').then(m => m.StatsComponent),
  },
  {
    path: 'upcoming',
    redirectTo: 'week',
    pathMatch: 'full',
  },
  {
    path: 'project/:id',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/project-view/project-view.component').then(m => m.ProjectViewComponent),
  },
  {
    path: 'labels',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/labels/labels.component').then(m => m.LabelsComponent),
  },
  {
    path: 'label/:name',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/label-tasks/label-tasks.component').then(m => m.LabelTasksComponent),
  },
  {
    path: 'filters',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/filters/filters.component').then(m => m.FiltersComponent),
  },
  {
    path: 'filter/:id',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/filter-tasks/filter-tasks.component').then(m => m.FilterTasksComponent),
  },
  {
    path: 'time',
    canActivate: [AutoLoginPartialRoutesGuard],
    loadComponent: () => import('./features/time-tracker/time-tracker.component').then(m => m.TimeTrackerComponent),
  },
  { path: '**', redirectTo: '' },
];
