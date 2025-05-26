import { Routes } from '@angular/router';
import { SidebarComponent } from './shared/sidebar/sidebar.component';
import { LayoutComponent } from './shared/layout/layout.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(c => c.LoginComponent)
  },
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard/dashboard.component').then(c => c.DashboardComponent),
      },
      {
        path: 'flight-booking-table',
        loadComponent: () =>
          import('./pages/flight-booking-table/flight-booking-table.component').then(c => c.FlightBookingTableComponent),
      },
      {
        path: 'admin-profile',
        loadComponent: () =>
          import('./pages/admin-profile/admin-profile.component').then(c => c.AdminProfileComponent),
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];