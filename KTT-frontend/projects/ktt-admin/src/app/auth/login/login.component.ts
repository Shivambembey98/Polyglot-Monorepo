import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  form: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.formBuilder.group({
      email: [''],
      password: [''],
    });
  }

  onLogin(): void {
    const { email, password } = this.form.value;
  
    this.authService.login(email, password).subscribe({
      next: (res) => {
        console.log('Login successful', res);
  
        // Store login info
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('username', res.username);
        localStorage.setItem('tenantId', res.tenantId);
  
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Login failed', err);
        // Optionally show an error message to the user
      }
    });
  }  
}
