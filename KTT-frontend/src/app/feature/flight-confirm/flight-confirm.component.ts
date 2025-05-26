import { Component, OnInit } from '@angular/core';
import { Router, RouterLink,ActivatedRoute } from '@angular/router';
import { CommonModule, NgIf } from '@angular/common';
import { AirCreateReservationService } from '../../services/air-create-reservation/air-create-reservation.service';
import { SearchFormService } from '../../services/search-form/search-form.service';
import { AirRetrieveService } from '../../services/air-retrieve/air-retrieve.service';

@Component({
  selector: 'app-flight-confirm',
  standalone: true,
  imports: [RouterLink, CommonModule, NgIf],
  templateUrl: './flight-confirm.component.html',
  styleUrl: './flight-confirm.component.css'
})
export class FlightConfirmComponent implements OnInit {
  showDetails = true;
  formData: any;
  flightDetails: any;
  departureFlight: any;
  arrivalFlight: any;
  selectedFlight: any;
  
  baseFare: number = 0;
  taxes: number = 0;
  returnBaseFare: number = 0;
  returnTaxes: number = 0;
  totalBaseFare: number = 0;
  totalTaxes: number = 0;
  totalFare: number = 0;
  showPriceDetails: boolean = false;
  showBaseFareBreakdown: boolean = false;
  priceBreakdown: PriceBreakdown[] = [];
  private passengerTypeLabels: { [key: string]: string } = {
    'ADT': 'Adult',
    'CNN': 'Child',
    'INF': 'Infant',
    'STD': 'Student',
    'SRC': 'Senior Citizen'
  };
  searchFormData: any;
  multicityFlight:any[] = [];
  locatorCode!: string;
  providerCode!: string;

  // Confirm modal TS 
    isModalOpen = false;
    airReservationLocatorCode!: string;
  
    handleContinueClick(): void {
      console.log('Sending request with locator code:', this.airReservationLocatorCode);
      this.searchFormService.airTicket(this.airReservationLocatorCode).subscribe(
        (response) => {
          console.log('API response:', response);
          this.isModalOpen = true;  // Open the modal after the API call
        },
        (error) => {
          console.error('API call failed', error);
          // Handle error, maybe show an error message or alert
        }
      );
    }    
  
    closeModal(): void {
      this.isModalOpen = false;
    }
  ////
  
  constructor(
    private searchFormService: SearchFormService,
    private router: Router,
    private airCreateReservationService: AirCreateReservationService,
    private airRetrieveService: AirRetrieveService,
    private route: ActivatedRoute
  ) {}


  // callRetrieveTicket(locatorCode: string, providerCode: string): void {
  //   this.searchFormService.retrieveTicket(locatorCode, providerCode).subscribe({
  //     next: (response) => {
  //       console.log('Ticket retrieved:', response);
  //       // handle response
  //     },
  //     error: (err) => {
  //       console
  //   });
  // }

  toggleDetails(): void {
    this.showDetails = !this.showDetails;
  }

  togglePriceDetails(): void {
    this.showPriceDetails = !this.showPriceDetails;
  }

  toggleBaseFareBreakdown(): void {
    this.showBaseFareBreakdown = !this.showBaseFareBreakdown;
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.locatorCode = params['locatorCode'];
      this.providerCode = params['providerCode'];
      this.airReservationLocatorCode = params['airReservationLocatorCode'];
      
      // No need to call retrieveTicket again as we already have the airReservationLocatorCode
      if (!this.airReservationLocatorCode) {
        console.warn('No airReservationLocatorCode provided in query params');
      }
    });

    this.formData = this.airCreateReservationService.getFormData();
    this.flightDetails = this.searchFormService.getSelectedFlight();
    const storedForm = localStorage.getItem('flightFormData');
    this.searchFormData = JSON.parse(storedForm || '');
  
    if (this.searchFormData.formType === 'round-trip') {
      this.departureFlight = this.flightDetails[0];
      this.arrivalFlight = this.flightDetails[1];
      this.totalBaseFare =
        Number(this.departureFlight?.fare?.base ?? this.departureFlight?.basePrice ?? 0) +
        Number(this.arrivalFlight?.fare?.base ?? this.arrivalFlight?.basePrice ?? 0);
      this.totalTaxes =
        Number(this.departureFlight?.fare?.tax ?? this.departureFlight?.taxes ?? 0) +
        Number(this.arrivalFlight?.fare?.tax ?? this.arrivalFlight?.taxes ?? 0);
    } else if (this.searchFormData.formType === 'multi-city') {
      this.multicityFlight = this.flightDetails;
    } else {
      this.departureFlight = this.flightDetails;
      this.totalBaseFare = Number(this.departureFlight?.fare?.base ?? this.departureFlight?.basePrice ?? 0);
      this.totalTaxes = Number(this.departureFlight?.fare?.tax ?? this.departureFlight?.taxes ?? 0);
    }
  
    this.totalFare = this.totalBaseFare + this.totalTaxes;
  
    this.calculateTotalFare();
  
    if (!this.formData) {
      console.warn('No reservation data found in storage');
    }
  }
  

  calculateTotalFare(): void {
    this.priceBreakdown = [];
    let totalBase = 0;
    let totalTaxes = 0;

    // Get passenger counts from formData
    const passengerCounts = {
      ADT: this.searchFormData.adults || 0,
      CNN: this.searchFormData.children || 0,
      INF: this.searchFormData.infants || 0,
      STD: this.searchFormData.students || 0,
      SRC: this.searchFormData.seniorCitizens || 0
    };
   

    if (this.searchFormData.formType === 'multi-city') {
      // Multicity flights
      this.flightDetails.forEach((legFlight: any, index: number) => {
        Object.entries(passengerCounts).forEach(([type, count]) => {
          if (count > 0) {
            let baseFare = 0;
            let taxes = 0;

            if (legFlight.type === 'connecting') {
              // Handle connecting flights in multicity
              legFlight.flights.forEach((flight: any) => {
                const priceInfo = flight.priceArray?.find((p: any) => p.priceType === type) || flight.priceArray?.[0];
                baseFare += Number(priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0);
                taxes += Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
              });
            } else {
              // Handle direct flights in multicity
              const priceInfo = legFlight.priceArray?.find((p: any) => p.priceType === type) || legFlight.priceArray?.[0];
              baseFare = Number(priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0);
              taxes = Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
            }

            const typeTotal = (baseFare + taxes) * count;
            totalBase += baseFare * count;
            totalTaxes += taxes * count;

            // Update or create price breakdown entry
            const existingEntry = this.priceBreakdown.find(pb => pb.type === type);
            if (existingEntry) {
              existingEntry.baseFare += baseFare * count;
              existingEntry.taxes += taxes * count;
              existingEntry.total += typeTotal;
            } else {
              this.priceBreakdown.push({
                type,
                label: this.passengerTypeLabels[type],
                count,
                baseFare: baseFare * count,
                taxes: taxes * count,
                total: typeTotal
              });
            }
          }
        });
      });
    } else {
      // Handle single flight (one-way) or round-trip
      const departurePrices = this.departureFlight?.type === 'connecting' ||  this.departureFlight?.type ==='connecting-return'
        ? this.departureFlight?.flights.reduce((acc: any[], flight: any) => [...acc, ...(flight.priceArray || [])], [])
        : this.departureFlight?.priceArray || [];

      const returnPrices = this.arrivalFlight?.type === 'connecting' ||  this.arrivalFlight?.type ==='connecting-return'
      ? this.arrivalFlight?.flights.reduce((acc: any[], flight: any) => [...acc, ...(flight.priceArray || [])], [])
      : this.arrivalFlight?.priceArray || [];
    

      Object.entries(passengerCounts).forEach(([type, count]) => {
        if (count > 0) {
          let baseFare = 0;
          let taxes = 0;
          let baseFare2 = 0;
          let taxes2 = 0;
      
          // --- DEPARTURE HANDLING ---
          if (this.departureFlight?.type?.includes('connecting') || this.departureFlight?.type?.includes('connecting-return')) {
            this.departureFlight?.flights?.forEach((flight: any) => {
              const priceInfo = flight.priceArray?.find((p: any) => p.priceType === type) || flight.priceArray?.[0];
              baseFare += Number(priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0);
              taxes += Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
            });
          } else {
            const priceInfo = departurePrices.find((p: any) => p.priceType === type) || departurePrices[0];
            baseFare = Number(priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0);
            taxes = Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
          }
      
          // --- ARRIVAL HANDLING ---
          if (this.arrivalFlight?.type?.includes('connecting') || this.arrivalFlight?.type?.includes('connecting-return')) {
            this.arrivalFlight?.flights?.forEach((flight: any) => {
              const priceInfo = flight?.priceArray?.find((p: any) => p.priceType === type) || flight.priceArray?.[0];
              baseFare2 += Number(priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0);
              taxes2 += Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
            });
          } else {
            const priceInfo2 = returnPrices.find((p: any) => p.priceType === type) || returnPrices[0];
            baseFare2 = Number(priceInfo2?.basePrice?.replace(/[^\d.]/g, '') || 0);
            taxes2 = Number(priceInfo2?.taxes?.replace(/[^\d.]/g, '') || 0);
          }
      
          // --- Totals ---
          const typeTotal = (baseFare + baseFare2 + taxes + taxes2) * count;
          totalBase += (baseFare + baseFare2) * count;
          totalTaxes += (taxes + taxes2) * count;
      
          this.priceBreakdown.push({
            type,
            label: this.passengerTypeLabels[type],
            count,
            baseFare: (baseFare + baseFare2) * count,
            taxes: (taxes + taxes2) * count,
            total: typeTotal
          });
      
        }
      });

      // Handle return flight if it exists
     
    }
    this.totalBaseFare = totalBase;
    this.totalTaxes = totalTaxes;
    this.totalFare = totalBase + totalTaxes;
  }
}

interface PriceBreakdown {
  type: string;
  label: string;
  count: number;
  baseFare: number;
  taxes: number;
  total: number;
}
