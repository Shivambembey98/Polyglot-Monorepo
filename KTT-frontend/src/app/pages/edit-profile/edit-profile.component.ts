import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '../../services/auth/auth.service';
import { CountryISO, NgxIntlTelInputModule, SearchCountryField } from 'ngx-intl-tel-input';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule , NgxIntlTelInputModule],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css'],
})
export class EditProfileComponent {
  editProfileForm: FormGroup;
  CountryISO = CountryISO;
  SearchCountryField = SearchCountryField;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<EditProfileComponent>,
    private authService: AuthService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.editProfileForm = this.formBuilder.group({
      firstName: [data?.firstName || '', Validators.required],
      lastName: [data?.lastName || '', Validators.required],
      dob: [  data?.dob || '', Validators.required ],
      email: [data?.email || '', [Validators.required, Validators.email]],
      gender: [data?.gender || ''],
      mobileNo: [data?.mobileNo || '', Validators.required],
      nationality: [data?.nationality || ''],
      address: [data?.address || '', Validators.required],
      city: [data?.city || ''],
      state: [data?.state || ''],
      pinCode: [data?.pinCode || ''],
    });    
  }

  save() {
    console.log('Save clicked');
    if (this.editProfileForm.valid) {
      const formData = this.editProfileForm.value;
      console.log('Form data:', formData);
  
      const userId = localStorage.getItem('userId');
      if (!userId) {
        console.error('User ID not found in localStorage');
        return;
      }
  
      const updatePayload = {
        id: +userId,
        firstName: formData.firstName,
        middleName: formData.middleName,
        lastName: formData.lastName,
        mobileNo: formData.mobileNo?.internationalNumber || '',
        address: formData.address,
        city: formData.city,
        state: formData.state,
        nationality: formData.nationality,
        pinCode: formData.pinCode,
        gender: formData.gender,
        dob: formData.dob
      };
  
      console.log('Payload being sent:', updatePayload);
  
      this.authService.updateProfile(updatePayload).subscribe(
        (res) => {
          console.log('Profile updated:', res);
          this.dialogRef.close(res);
        },
        (error) => {
          console.error('Error updating profile:', error);
        }
      );
    } else {
      console.warn('Form is invalid');
      this.editProfileForm.markAllAsTouched();
    }
  }  

  close() {
    this.dialogRef.close();
  }
}