import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { EditProfileComponent } from '../edit-profile/edit-profile.component';
import { AuthService } from '../../services/auth/auth.service';
@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css',
})
export class UserProfileComponent implements OnInit {
  user: any = {};
  selectedTab: string = 'profileSection';

  constructor(
    private router: Router,
    private dialog: MatDialog,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const username = localStorage.getItem('username'); // or whatever key you store it under
    if (username) {
      this.authService.getProfile(username).subscribe({
        next: (res: any) => {
          this.user = res.data; // Adjust if response is different
        },
        error: (err) => {
          console.error('Failed to fetch user profile:', err);
        }
      });
    } else {
      console.warn('No username found in local storage.');
    }
  }

  getInitials(firstName: string, lastName: string): string {
    if (!firstName && !lastName) return 'U';
    const first = firstName?.charAt(0).toUpperCase() || '';
    const last = lastName?.charAt(0).toUpperCase() || '';
    return first + last;
  }  

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
  
    const file = input.files[0];
    const reader = new FileReader();
  
    reader.onload = () => {
      this.user.profileImageUrl = reader.result as string;
      this.authService.setUser(this.user); // <-- Update shared user
    };
  
    reader.readAsDataURL(file);
  
    this.uploadProfileImage(file);
  }  
  
  uploadProfileImage(file: File): void {
    const formData = new FormData();
    formData.append('profileImage', file);
  
    this.authService.uploadProfileImage(this.user.id, formData).subscribe({
      next: (res) => console.log('Image uploaded successfully:', res),
      error: (err) => console.error('Upload failed:', err),
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
  bookings = [
    {
      origin: 'Delhi (DEL)',
      destination: 'Dubai (DXB)',
      date: new Date('2025-04-23'),
      amount: 15000,
      status: 'Success',
    },
    {
      origin: 'Delhi (DEL)',
      destination: 'Dubai (DXB)',
      date: new Date('2025-04-23'),
      amount: 15000,
      status: 'Success',
    },
    {
      origin: 'Delhi (DEL)',
      destination: 'Dubai (DXB)',
      date: new Date('2025-04-23'),
      amount: 15000,
      status: 'Success',
    },
  ];

  selectTab(tabName: string) {
    this.selectedTab = tabName;
  }

  openEditProfile() {
    this.dialog.open(EditProfileComponent, {
      width: '600px',
      panelClass: 'edit-profile-modal',
      data: { ...this.user }, // Pass the user data here
    });
  }  

  addTraveller() {
    // Logic to add a new co-traveller
    console.log('Add traveller clicked');
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
