import { Component , OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SignUpComponent } from '../../auth/sign-up/sign-up.component';
import { RouterLink, RouterOutlet , Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [ RouterLink, CommonModule ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit {
  isLoggedIn = false;
  username: string | null = '';
  user: any = null; 
  constructor(private authService: AuthService, private router: Router
  ) {}

  onProfileClick(): void {
    const username = localStorage.getItem('username');
    console.log('Username from localStorage:', username); // Debug
  
    if (username) {
      this.authService.getProfile(username).subscribe(
        (response) => {
          console.log('Profile Response:', response);
  
          const userData = response?.data;
  
          if (userData?.id !== undefined) {
            localStorage.setItem('userId', userData.id.toString());
            // console.log('User ID stored in localStorage:', userData.id); // Debug
          } else {
            console.warn('No ID found in response.data');
          }
  
          this.router.navigate(['/user-profile']);
        },
        (error) => {
          console.error('Error fetching profile:', error);
        }
      );
    }
  }
    
  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe((status) => {
      this.isLoggedIn = status;
      this.username = localStorage.getItem('username');
  
      if (this.username) {
        this.authService.getProfile(this.username).subscribe({
          next: (response) => {
            const userData = response?.data;
            this.user = userData;
            this.authService.setUser(userData); // Broadcast on init
          },
          error: (error) => {
            console.error('Error fetching user profile in header:', error);
          }
        });
      }
    });
  
    // Listen for changes to user profile (e.g., image update)
    this.authService.user$.subscribe((updatedUser) => {
      if (updatedUser) {
        this.user = updatedUser;
      }
    });
  }   

  getInitials(firstName: string, lastName: string): string {
    const f = firstName?.charAt(0).toUpperCase() || '';
    const l = lastName?.charAt(0).toUpperCase() || '';
    return f + l;
  }
  
  onLogoutClick(): void {
    this.authService.logout(); // ✅ Logout logic moved to AuthService
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
