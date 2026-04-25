import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'inbox', pathMatch: 'full' },
  {
    path: 'inbox',
    loadComponent: () => import('./features/inbox/inbox.component').then(m => m.InboxComponent)
  },
  {
    path: 'today',
    loadComponent: () => import('./features/today/today.component').then(m => m.TodayComponent)
  },
  {
    path: 'upcoming',
    loadComponent: () => import('./features/upcoming/upcoming.component').then(m => m.UpcomingComponent)
  },
  {
    path: 'project/:id',
    loadComponent: () => import('./features/project-view/project-view.component').then(m => m.ProjectViewComponent)
  },
  {
    path: 'labels',
    loadComponent: () => import('./features/labels/labels.component').then(m => m.LabelsComponent)
  },
  {
    path: 'label/:name',
    loadComponent: () => import('./features/label-tasks/label-tasks.component').then(m => m.LabelTasksComponent)
  },
  { path: '**', redirectTo: 'inbox' }
];
