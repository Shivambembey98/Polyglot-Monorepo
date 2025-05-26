import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatDialog , MatDialogModule } from '@angular/material/dialog';
import { AuthService } from '../../services/auth/auth.service';
import { EditAdminProfileComponent } from '../edit-admin-profile/edit-admin-profile.component';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [RouterOutlet, CommonModule , MatDialogModule],
  templateUrl: './admin-profile.component.html',
  styleUrl: './admin-profile.component.css'
})
export class AdminProfileComponent {
  tenant: any = {};
  selectedTab: string = 'profileSection';

  constructor(
    private router: Router,
    private dialog: MatDialog,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const tenantId = localStorage.getItem('tenantId');
    if (!tenantId) {
      console.error('Tenant ID not found in localStorage');
      return;
    }
  
    this.authService.getTenantDetails(tenantId).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.tenant = response.data;
          console.log('User data loaded:', this.tenant);
        }
      },
      error: (error) => {
        console.error('Failed to fetch tenant details', error);
      }
    });
  }
  

  setTab(tabId: string) {
    this.selectedTab = tabId;
    this.scrollTo(tabId);
  }

  scrollTo(sectionId: string) {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  openEditProfile() {
    this.dialog.open(EditAdminProfileComponent, {
      width: '600px',
      panelClass: 'edit-admin-profile-modal',
      data: { ...this.tenant }, // Pass the user data here
    });
  }  

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
