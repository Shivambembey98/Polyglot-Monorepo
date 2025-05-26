import { Component , Inject , Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '../../services/auth/auth.service';
import { CountryISO, NgxIntlTelInputModule, SearchCountryField } from 'ngx-intl-tel-input';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EditProfileComponent } from '../../../../../../src/app/pages/edit-profile/edit-profile.component';
@Component({
  selector: 'app-edit-admin-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule , NgxIntlTelInputModule],
  templateUrl: './edit-admin-profile.component.html',
  styleUrl: './edit-admin-profile.component.css'
})
export class EditAdminProfileComponent {
  editProfileForm: FormGroup;
  CountryISO = CountryISO;
  SearchCountryField = SearchCountryField;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<EditAdminProfileComponent>,
    private authService: AuthService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.editProfileForm = this.formBuilder.group({
      username: [data?.username || '', Validators.required],
      companyName: [data?.companyName || '', Validators.required],
      tenantName: [  data?.tenantName || '', Validators.required ],
      email: [data?.email || '', [Validators.required, Validators.email]],
      companyCode: [data?.companyCode || ''],
      mobile: [data?.mobile || '', Validators.required],
      isActive: [data?.isActive || ''],
    });    
  }

  save() {
    console.log('Save clicked');
    console.log('Form Valid:', this.editProfileForm.valid);
    console.log('Form Data:', this.editProfileForm.value);
  
    if (this.editProfileForm.valid) {
      const formData = this.editProfileForm.value;
      console.log('Form data:', formData);
  
      const userId = localStorage.getItem('tenantId');
      if (!userId) {
        console.error('User ID not found in localStorage');
        return;
      }
  
      const updatePayload = {
        id: +userId,
        username: formData.username,
        companyName: formData.companyName,
        tenantName: formData.tenantName,
        mobile: typeof formData.mobile === 'string' ? formData.mobile : formData.mobile?.number || '',
        companyCode: formData.companyCode,
        email: formData.email
      };
  
      console.log('Payload being sent:', updatePayload);
  
      this.authService.updateTenantProfile(updatePayload).subscribe(
        (res) => {
          console.log('Profile updated:', res);
          this.dialogRef.close(res);
        },
        (error) => {
          console.error('Error updating profile:', error);
        }
      );
    } else {
      console.warn('Form is invalid', this.editProfileForm.errors);
      console.log('Form controls:', this.editProfileForm.controls);
      this.editProfileForm.markAllAsTouched();
    }
  }   

  close() {
    this.dialogRef.close();
  }
}