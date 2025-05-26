import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit {
  isLoggedIn = false;
  username: string | null = '';

  constructor(private router: Router, private authService: AuthService) {}

  onProfileClick(): void {
    const tenantId = localStorage.getItem('tenantId');
    if (!tenantId) {
      console.error('Tenant ID not found in localStorage');
      return;
    }

    this.authService.getTenantDetails(tenantId).subscribe({
      next: (response: any) => {
        // console.log('Tenant details:', response);
        // Optionally store response if needed later
        this.router.navigate(['/admin-profile']);
      },
      error: (error) => {
        console.error('Error fetching tenant details:', error);
      },
    });
  }

  ngOnInit(): void {
    this.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    this.username = localStorage.getItem('username');
  }

  onLogoutClick(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
