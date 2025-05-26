import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SearchFormService } from '../../services/search-form/search-form.service';
import { AirCreateReservationService } from '../../services/air-create-reservation/air-create-reservation.service';
import { AirRetrieveService } from '../../services/air-retrieve/air-retrieve.service';

@Component({
  selector: 'app-flight-detail-modal',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatDialogModule, MatButtonModule],
  templateUrl: './flight-detail-modal.component.html',
  styleUrl: './flight-detail-modal.component.css',
})
export class FlightDetailModalComponent {
  formData: any;
  isLoading = false;
  locatorCode!: string;
  providerCode!: string;

  constructor(
    public dialogRef: MatDialogRef<FlightDetailModalComponent>,
    private searchFormService: SearchFormService,
    private router: Router,
    private airCreateReservationService: AirCreateReservationService,
    private airRetrieveService: AirRetrieveService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.formData = data?.formData;
    this.locatorCode = data.locatorCode;
    this.providerCode = data.providerCode;
  }
  
  onClose(): void {
    this.dialogRef.close();
  }

  closeAndNavigate(): void {
    if (!this.locatorCode || !this.providerCode) {
      console.warn('Missing locator or provider code');
      return;
    }
  
    this.isLoading = true;
  
    this.searchFormService.retrieveTicket(this.locatorCode, this.providerCode).subscribe({
      next: (response) => {
        console.log('Ticket retrieved:', response);
        this.isLoading = false;

        // Parse the XML response
        let airReservationLocatorCode: string = '';
        if (typeof response === 'string') {
          const parsedResponse = this.airRetrieveService.retrievexmlparser(response);
          airReservationLocatorCode = parsedResponse.airReservationLocatorCode;
        }
  
        this.dialogRef.close();
  
        this.dialogRef.afterClosed().subscribe(() => {
          this.router.navigate(['/flight-confirm'], {
            queryParams: {
              // locatorCode: this.locatorCode,
              // providerCode: this.providerCode,
              airReservationLocatorCode: airReservationLocatorCode
            },
          });
        });
      },
      error: (err) => {
        console.error('Retrieve ticket failed:', err);
        this.isLoading = false;
      },
    });
  }   
  
  getCityName(code: string): string {
    return code; // You can enhance this to map IATA codes to city names
  }
}
