import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  Validators,
} from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth/auth.service';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { NgxIntlTelInputModule } from 'ngx-intl-tel-input';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { CommonAlertComponent } from '../../pages/common-alert/common-alert.component';
import { ViewChild } from '@angular/core';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,
    RouterLink,
    NgxIntlTelInputModule,
    BsDropdownModule,
    CommonAlertComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit {
  @ViewChild(CommonAlertComponent) commonAlert!: CommonAlertComponent;
  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
  loginForm: any = FormGroup;
  isEmailLogin: boolean = true;
  isMobileLogin: boolean = false;
  showPassword: boolean = false;
  CountryISO = CountryISO;
  SearchCountryField = SearchCountryField;

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
  activeTab: string = 'EmailLogin'; // Set the default active tab to 'oneWay'

  toggleEmailLogin() {
    if (this.isMobileLogin) {
      this.isEmailLogin = true;
      this.isMobileLogin = !this.isMobileLogin;
      this.activeTab = 'EmailLogin';
    }
  }

  toggleMobileLogin() {
    if (this.isEmailLogin) {
      this.isMobileLogin = true;
      this.isEmailLogin = !this.isEmailLogin;
      this.activeTab = 'MobileLogin';
    }
  }

  modalMessage: string = '';

  showAlert(message: string) {
    this.commonAlert.show(message);
  }  

  login() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
  
    const credentials = this.loginForm.value;
    this.authService.login(credentials).subscribe(
      (res) => {
        this.authService.saveToken(res.accessToken, res.username);
        this.router.navigate(['/']);
        console.log(res, 'response');
        
      },
      (error) => {
        console.error('Login Error:', error);
  
        // ✅ Show the first error from the API response if available
        const errorMessage = error?.error?.errors?.[0] || 'Login failed. Please try again later.';
        this.showAlert(errorMessage);
      }
    );
  }  

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      login: ['', Validators.required],
      password: ['', Validators.required],
      companyCode: ['Bellblaze', Validators.required],
      mobileNo: [''],
    });
  }
}
