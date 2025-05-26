import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';
import { FormBuilder, FormsModule, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { CommonAlertComponent } from '../../pages/common-alert/common-alert.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    CommonModule,
    CommonAlertComponent,
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css'],
})
export class ForgotPasswordComponent implements OnInit {
  @ViewChild(CommonAlertComponent) alertModal!: CommonAlertComponent; // <-- here at top

  forgotPasswordForm!: FormGroup;
  submitted = false;
  email: string | null = '';
  modalMessage: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.forgotPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      companyCode: ['Bellblaze', Validators.required],
    });
  }

  showAlert(message: string) {
    this.alertModal.show(message); // <-- updated
  }

  onSubmit(): void {
    this.submitted = true;
  
    if (this.forgotPasswordForm.valid) {
      const formData = new FormData();
      formData.append('email', this.forgotPasswordForm.value.email);
      formData.append('companyCode', this.forgotPasswordForm.value.companyCode);
  
      this.authService.forgotPassword(formData).subscribe(
        (response: any) => {
          const message = response?.message || 'OTP sent successfully';
          if (response && response.token) {
            const sub = this.alertModal.closed.subscribe(() => {
              this.router.navigate(['/reset-password'], {
                queryParams: {
                  token: response.token,
                  email: this.forgotPasswordForm.value.email
                }
              });
              sub.unsubscribe();
            });
  
            this.alertModal.show(message);
          } else {
            this.alertModal.show('Token not received. Please try again.');
          }
        },
        (error) => {
          console.error('Error:', error);
          const apiError = error?.error?.message || error?.error?.errors?.[0];
          const errorMessage = apiError || 'Something went wrong. Please try again.';
          this.alertModal.show(errorMessage);
        }
      );
    }
  }       

  goBack(): void {
    this.router.navigate(['/login']);
  }
}
