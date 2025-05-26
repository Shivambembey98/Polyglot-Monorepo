import { Component, OnInit, ChangeDetectorRef , ElementRef, ViewChild } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SearchFormService } from '../../services/search-form/search-form.service';
import {
  FormGroup,
  FormBuilder,
  Validators,
  FormArray,
  FormsModule,
} from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AirCreateReservationService } from '../../services/air-create-reservation/air-create-reservation.service';
import { FlightDetailModalComponent } from '../../pages/flight-detail-modal/flight-detail-modal.component';
import { SeatSelectionComponent } from '../seat-selection/seat-selection.component';
import { MatDialog } from '@angular/material/dialog';
import * as countries from 'i18n-iso-countries';
import * as enLocale from 'i18n-iso-countries/langs/en.json';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import dayjs from 'dayjs'; // Ensure this is installed

// Custom validator function to check if the date is in the future
export function futureDateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const currentDate = new Date();
    const selectedDate = new Date(control.value);

    // If the selected date is greater than the current date, return an error
    if (selectedDate > currentDate) {
      return { futureDate: true }; // Error key
    }
    return null; // No error
  };
}

export function ageValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control || !control.parent) return null;

    const dob = control.value;
    const type = control.parent.get('passengerType')?.value;

    if (!dob || !type) return null;

    const today = dayjs();
    const birthDate = dayjs(dob);
    const age = today.diff(birthDate, 'year');

    switch (type) {
      case 'adult':
        return age >= 12 ? null : { ageInvalid: 'Adults must be at least 12 years old' };
      case 'child':
        return age >= 2 && age < 12
          ? null
          : { ageInvalid: 'Children must be between 2 and 12 years old' };
      case 'infant':
        return age < 2 ? null : { ageInvalid: 'Infants must be below 2 years old' };
      case 'seniorCitizen':
        return age >= 60 ? null : { ageInvalid: 'Senior Citizens must be at least 60 years old' };
      case 'student':
        return null; // No age check
      default:
        return null;
    }
  };
}

countries.registerLocale(enLocale);
interface PriceBreakdown {
  type: string;
  label: string;
  count: number;
  baseFare: number;
  taxes: number;
  total: number;
}

@Component({
  selector: 'app-flight-details',
  standalone: true,
  imports: [RouterLink, CommonModule, ReactiveFormsModule, FormsModule, NgIf , SeatSelectionComponent , MatProgressSpinnerModule],
  templateUrl: './flight-details.component.html',
  styleUrls: ['./flight-details.component.css'],
})
export class FlightDetailsComponent implements OnInit {
  passengerForm: any = FormGroup;
  submitted = false;
  searchFormData: any = null;
  selectedPassengerType: string = 'adult';
  flight: any = null;
  departureFlight: any = null;
  multicityFlight: any[] = [];
  arrivalFlight: any = null;
  departureAirport: any = null;
  arrivalAirport: any = null;
  baggageCarryOnFormatted: string = '';
  baggageCheckedFormatted: string = '';
  totalBaggagePieces: string = '';
  returnBaggageCarryOnFormatted: string = '';
  returnBaggageCheckedFormatted: string = '';
  returnDepartureAirport: any = null;
  returnArrivalAirport: any = null;
  passengerLabels: any = {
    adult: 'Adult (12+)',
    child: 'Child (2-12)',
    infant: 'Infant (0-2)',
    student: 'Student',
    seniorCitizen: 'Senior Citizen',
  };
  baseFare: number = 0;
  taxes: number = 0;
  returnBaseFare: number = 0;
  returnTaxes: number = 0;
  totalBaseFare: number = 0;
  totalTaxes: number = 0;
  totalFare: number = 0;
  maxDate!: string;
  formData: any = {};
  isLoading = false;
  showPriceDetails: boolean = false;
  showBaseFareBreakdown: boolean = true;
  priceBreakdown: PriceBreakdown[] = [];
  locatorCode!: string;
  providerCode!: string;
  countriesList: { name: string; code: string }[] = [];
  private passengerTypeLabels: { [key: string]: string } = {
    ADT: 'Adult',
    CNN: 'Child',
    INF: 'Infant',
    STD: 'Student',
    SRC: 'Senior Citizen',
  };

  getAirportByIata(iata: string): any {
    return this.searchFormService.getAirportByIata(iata);
  }

 isExpandedGSTForm = false;
  contentHeight = 0;

  @ViewChild('gstContent') gstContent!: ElementRef<HTMLDivElement>;

  toggleGSTForm() {
    this.isExpandedGSTForm = !this.isExpandedGSTForm;

    if (this.isExpandedGSTForm) {
      // Wait for Angular to render the expanded content
      setTimeout(() => {
        const scrollHeight = this.gstContent.nativeElement.scrollHeight;
        this.contentHeight = scrollHeight;
      });
    } else {
      this.contentHeight = 0;
    }
  }

  constructor(
    private formbuilder: FormBuilder,
    private searchFormService: SearchFormService,
    private router: Router,
    private airCreateReservationService: AirCreateReservationService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) {
    // Initialize the passenger form group structure
    this.passengerForm = this.formbuilder.group({
      passengers: this.formbuilder.array([]), // Will hold dynamic passenger forms
      contactInfo: this.formbuilder.group({
        email: ['', [Validators.required, Validators.email]],
        mobilePhone: [
          '',
          [Validators.required, Validators.pattern(/^\d{10}$/)],
        ],
      }),
    });
  }
  get email() {
    return this.passengerForm.get('contactInfo.email');
  }

  // Getters to access form controls
  get f(): { [key: string]: any } {
    return this.passengerForm.controls;
  }

  get passengers(): FormArray {
    return this.passengerForm.get('passengers') as FormArray;
  }

  // Function to create a new passenger form group
  createPassengerForm(type: string): FormGroup {
    return this.formbuilder.group({
      passengerType: [type],
      title: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      dateOfBirth: ['', [Validators.required, futureDateValidator(), ageValidator()]],
      nationality: ['', Validators.required],
      gender: ['', Validators.required],
    });
  }

  // Function to add passengers based on the form data
  addPassengersByCount(data: any): void {
    const passengerTypes = [
      { type: 'adult', count: +data.adults || 0 },
      { type: 'child', count: +data.children || 0 },
      { type: 'infant', count: +data.infants || 0 },
      { type: 'student', count: +data.students || 0 },
      { type: 'seniorCitizen', count: +data.seniorCitizens || 0 },
    ];

    const passengerArray = this.passengers;
    passengerArray.clear(); // Clear the existing passengers

    // Add passengers based on the count
    for (const group of passengerTypes) {
      for (let i = 0; i < group.count; i++) {
        const passenger = this.createPassengerForm(group.type);
        passengerArray.push(passenger);
      }
    }
  }
  // Helper Functions
  mapPassengerType(type: string): string {
    switch (type) {
      case 'adult':
        return 'ADT';
      case 'child':
        return 'CNN';
      case 'infant':
        return 'INF';
      case 'student':
        return 'STD';
      case 'seniorCitizen':
        return 'SRC';
      default:
        return 'ADT';
    }
  }
  formatDOB(date: string, returnInFormat = false): string {
    const dob = new Date(date);
    const yyyy = dob.getFullYear();
    const mm = dob.toLocaleString('default', { month: 'short' }).toUpperCase();
    const dd = String(dob.getDate()).padStart(2, '0');

    return returnInFormat
      ? `${dd}${mm}${String(yyyy).slice(-2)}`
      : dob.toISOString().split('T')[0];
  }

  // Function to handle form submission
  // onSubmit(): void {
  //   this.submitted = true;

  //   if (this.passengerForm.invalid) {
  //     console.warn('Form is invalid:', this.passengerForm.errors);

  //     // Find the first invalid input, select, or textarea
  //     const firstInvalidControl = document.querySelector(
  //       'input.ng-invalid, select.ng-invalid, textarea.ng-invalid'
  //     );

  //     if (firstInvalidControl) {
  //       (firstInvalidControl as HTMLElement).scrollIntoView({
  //         behavior: 'smooth',
  //         block: 'center',
  //       });
  //       firstInvalidControl.classList.add('highlight-invalid');

  //       setTimeout(() => {
  //         firstInvalidControl.classList.remove('highlight-invalid');
  //       }, 2000);
  //     }

  //     return;
  //   }

  //   // Extract form data to pass into modal for review
  //   const formValue = this.passengerForm.value;

  //   const travelDtoList = formValue.passengers.map((p: any) => ({
  //     firstName: p.firstName,
  //     lastName: p.lastName,
  //     prefix: p.title,
  //     nationality: p.nationality,
  //     gender: p.gender === 'Male' ? 'M' : 'F',
  //     travelerType: this.mapPassengerType(p.passengerType),
  //     phoneNumber: String(formValue.contactInfo.mobilePhone),
  //     email: formValue.contactInfo.email,
  //     dob: this.formatDOB(p.dateOfBirth),
  //     docText: `P/IN/A1234567/IN/15JAN30/${
  //       p.gender === 'Male' ? 'M' : 'F'
  //     }/${this.formatDOB(
  //       p.dateOfBirth,
  //       true
  //     )}/${p.firstName.toUpperCase()}/${p.lastName.toUpperCase()}`,
  //   }));

  //   const sessionIdLfs = localStorage.getItem('sessionIdLfs') || '';
  //   const sessionIdAirPrice = localStorage.getItem('sessionIdAirPrice') || '';

  //   // Prepare form data to pass to modal for review
  //   this.formData = {
  //     travelDtoList,
  //     street: '456 Park Avenue',
  //     city: 'Mumbai',
  //     state: 'MH',
  //     postalCode: '400001',
  //     country: 'IN',
  //     airLine: this.flight?.airline || '',
  //     sessionIdLfs,
  //     sessionIdAirPrice,
  //   };

  //   // Open modal for review
  //   this.openFlightDetails();
  // }

  onSubmit(): void {
    this.submitted = true;
  
    if (this.passengerForm.invalid) {
      console.warn('Form is invalid:', this.passengerForm.errors);
  
      const firstInvalidControl = document.querySelector(
        'input.ng-invalid, select.ng-invalid, textarea.ng-invalid'
      );
  
      if (firstInvalidControl) {
        (firstInvalidControl as HTMLElement).scrollIntoView({
          behavior: 'smooth',
          block: 'center',
        });
        firstInvalidControl.classList.add('highlight-invalid');
        setTimeout(() => firstInvalidControl.classList.remove('highlight-invalid'), 2000);
      }
      return;
    }
    this.isLoading = true;
  
    const formValue = this.passengerForm.value;
  
    const travelDtoList = formValue.passengers.map((p: any) => ({
      firstName: p.firstName,
      lastName: p.lastName,
      prefix: p.title,
      nationality: p.nationality,
      gender: p.gender === 'Male' ? 'M' : 'F',
      travelerType: this.mapPassengerType(p.passengerType),
      phoneNumber: String(formValue.contactInfo.mobilePhone),
      email: formValue.contactInfo.email,
      dob: this.formatDOB(p.dateOfBirth),
      docText: `P/IN/A1234567/IN/15JAN30/${p.gender === 'Male' ? 'M' : 'F'}/${this.formatDOB(p.dateOfBirth, true)}/${p.firstName.toUpperCase()}/${p.lastName.toUpperCase()}`
    }));
  
    const sessionIdLfs = localStorage.getItem('sessionIdLfs') || '';
    const sessionIdAirPrice = localStorage.getItem('sessionIdAirPrice') || '';
  
    this.formData = {
      travelDtoList,
      street: '456 Park Avenue',
      city: 'Mumbai',
      state: 'MH',
      postalCode: '400001',
      country: 'IN',
      airLine: this.flight?.airline || '',
      sessionIdLfs,
      sessionIdAirPrice,
    };
  
    // Save formData for modal access
    this.airCreateReservationService.setFormData(this.formData);
  
    this.searchFormService.createAirReservation(this.formData).subscribe({
      next: (res) => {
        const { locatorCode, providerCode } =
          this.airCreateReservationService.extractReservationInfoFromXml(res);
  
        this.locatorCode = locatorCode;
        this.providerCode = providerCode;
  
        console.log('Reservation created:', res);
        
      },
      error: (err) => {
        console.error('Reservation Error:', err);
      },
      complete: () => {
        this.isLoading = false; // ✅ Stop loading after success or error
      },
    });
  }

  openFlightDetails() {
    const dialogRef = this.dialog.open(FlightDetailModalComponent, {
      width: '60vw',
      maxWidth: '100vw',
      data: {
        searchFormData: this.searchFormData,
        departureFlight: this.departureFlight,
        arrivalFlight: this.arrivalFlight,
        multicityFlight: this.multicityFlight,
        formData: this.formData,
        totalFare: this.totalFare,
        locatorCode: this.locatorCode,  
      providerCode: this.providerCode,
      },
    });

    // dialogRef.afterClosed().subscribe((result) => {
    //   if (result === 'booked') {
    //     // Modal is confirmed closed — safe to navigate
    //     this.router.navigate(['/flight-confirm']);
    //   }
    // });
  }

  ngOnInit(): void {
    // Get the selected flight from the service
    this.flight = this.searchFormService.getSelectedFlight();

    const today = new Date();
    this.maxDate = today.toISOString().split('T')[0];

    const countryObj = countries.getNames('en', { select: 'official' });

    this.countriesList = Object.entries(countryObj).map(([code, name]) => ({
      code,
      name,
    }));

    // Handle case when no flight data is available
    if (!this.flight) {
      console.warn('No flight data found, redirecting...');
      return;
    }

    // Load search form data from localStorage if available
    const storedForm = localStorage.getItem('flightFormData');
    if (storedForm) {
      this.searchFormData = JSON.parse(storedForm);
    } else {
      console.warn('No search form data found in localStorage');
    }

    // Add passengers based on the data from localStorage
    if (this.searchFormData) {
      this.addPassengersByCount(this.searchFormData);
    }

    // Check if it's a round-trip or one-way trip
    if (this.searchFormData.formType === 'round-trip') {
      this.departureFlight = this.flight[0];
      this.arrivalFlight = this.flight[1];

      const departureFare = Number(
        this.departureFlight?.fare?.base ?? this.departureFlight?.basePrice ?? 0
      );
      const arrivalFare = Number(
        this.arrivalFlight?.fare?.base ?? this.arrivalFlight?.basePrice ?? 0
      );

      const departureTax = Number(
        this.departureFlight?.fare?.tax ?? this.departureFlight?.taxes ?? 0
      );
      const arrivalTax = Number(
        this.arrivalFlight?.fare?.tax ?? this.arrivalFlight?.taxes ?? 0
      );

      this.totalBaseFare = departureFare + arrivalFare;
      this.totalTaxes = departureTax + arrivalTax;
    } else if (this.searchFormData.formType === 'multi-city') {
      this.multicityFlight = this.flight;
    } else {
      this.departureFlight = this.flight;
      this.totalBaseFare = Number(
        this.departureFlight?.fare?.base ?? this.departureFlight?.basePrice ?? 0
      );
      this.totalTaxes = Number(
        this.departureFlight?.fare?.tax ?? this.departureFlight?.taxes ?? 0
      );
    }

    this.totalFare = this.totalBaseFare + this.totalTaxes;
    // Subscribe to airport list to get departure and arrival airports
    this.searchFormService.airportList$.subscribe((airports) => {
      if (!airports.length) return;

      if (this.searchFormData.formType === 'multi-city') {
        this.multicityFlight = this.multicityFlight.map((flight) => {
          const departureAirport = airports.find(
            (a) => a.iataCode === flight.departure
          );
          const arrivalAirport = airports.find(
            (a) => a.iataCode === flight.arrival
          );

          const updatedFlight = {
            ...flight,
            departureAirportCity: departureAirport?.airportCity,
            departureAirportName: departureAirport?.airportName,
            arrivalAirportCity: arrivalAirport?.airportCity,
            arrivalAirportName: arrivalAirport?.airportName,
          };
          return updatedFlight;
        });
      } else {
        if (this.departureFlight) {
          this.departureAirport = airports.find(
            (a) => a.iataCode === this.departureFlight.departure
          );
          this.arrivalAirport = airports.find(
            (a) => a.iataCode === this.departureFlight.arrival
          );
        }

        if (this.arrivalFlight) {
          this.returnDepartureAirport = airports.find(
            (a) => a.iataCode === this.arrivalFlight.departure
          );
          this.returnArrivalAirport = airports.find(
            (a) => a.iataCode === this.arrivalFlight.arrival
          );
        }
      }
    });

    // Format baggage details
    if (this.multicityFlight) {
      this.multicityFlight.map((flight) => {
        if (
          flight.type === 'connecting' ||
          flight.type === 'connecting-return'
        ) {
          // For connecting flights, get baggage from the first segment
          const firstSegment = flight.flights[0];
          this.baggageCarryOnFormatted = this.getFormattedBaggageDetail(
            firstSegment.baggageCarryOn || flight.baggageCarryOn
          );
          this.baggageCheckedFormatted = this.getFormattedBaggageDetail(
            firstSegment.baggageChecked || flight.baggageChecked
          );
        } else {
          // For direct flights
          this.baggageCarryOnFormatted = this.getFormattedBaggageDetail(
            flight.baggageCarryOn
          );
          this.baggageCheckedFormatted = this.getFormattedBaggageDetail(
            flight.baggageChecked
          );
        }
      });
    }
    if (this.departureFlight) {
      if (
        this.departureFlight.type === 'connecting' ||
        this.departureFlight.type === 'connecting-return'
      ) {
        // For connecting flights, get baggage from the first segment
        const firstSegment = this.departureFlight.flights[0];
        this.baggageCarryOnFormatted = this.getFormattedBaggageDetail(
          firstSegment.baggageCarryOn || this.departureFlight.baggageCarryOn
        );
        this.baggageCheckedFormatted = this.getFormattedBaggageDetail(
          firstSegment.baggageChecked || this.departureFlight.baggageChecked
        );
      } else {
        // For direct flights
        this.baggageCarryOnFormatted = this.getFormattedBaggageDetail(
          this.departureFlight.baggageCarryOn
        );
        this.baggageCheckedFormatted = this.getFormattedBaggageDetail(
          this.departureFlight.baggageChecked
        );
      }
    }

    if (this.arrivalFlight) {
      if (
        this.arrivalFlight.type === 'connecting' ||
        this.arrivalFlight.type === 'connecting-return'
      ) {
        // For connecting flights, get baggage from the first segment
        const firstSegment = this.arrivalFlight.flights[0];
        this.returnBaggageCarryOnFormatted = this.getFormattedBaggageDetail(
          firstSegment.baggageCarryOn || this.arrivalFlight.baggageCarryOn
        );
        this.returnBaggageCheckedFormatted = this.getFormattedBaggageDetail(
          firstSegment.baggageChecked || this.arrivalFlight.baggageChecked
        );
      } else {
        // For direct flights
        this.returnBaggageCarryOnFormatted = this.getFormattedBaggageDetail(
          this.arrivalFlight.baggageCarryOn
        );
        this.returnBaggageCheckedFormatted = this.getFormattedBaggageDetail(
          this.arrivalFlight.baggageChecked
        );
      }
    }

    this.calculateTotalFare();
  }

  calculateTotalFare(): void {
    this.priceBreakdown = [];
    let totalBase = 0;
    let totalTaxes = 0;

    // Get passenger counts from form data
    const passengerCounts = {
      ADT: this.searchFormData.adults || 0,
      CNN: this.searchFormData.children || 0,
      INF: this.searchFormData.infants || 0,
      STD: this.searchFormData.students || 0,
      SRC: this.searchFormData.seniorCitizens || 0,
    };

    // Handle different flight types (one-way, round-trip, multicity)
    if (this.searchFormData.formType === 'multi-city') {
      // Multicity flights
      this.flight.forEach((legFlight: any, index: number) => {
        Object.entries(passengerCounts).forEach(([type, count]) => {
          if (count > 0) {
            let baseFare = 0;
            let taxes = 0;

            if (legFlight.type === 'connecting') {
              // Handle connecting flights in multicity
              legFlight.flights.forEach((flight: any) => {
                const priceInfo =
                  flight.priceArray?.find((p: any) => p.priceType === type) ||
                  flight.priceArray?.[0];
                baseFare += Number(
                  priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0
                );
                taxes += Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
              });
            } else {
              // Handle direct flights in multicity
              const priceInfo =
                legFlight.priceArray?.find((p: any) => p.priceType === type) ||
                legFlight.priceArray?.[0];
              baseFare = Number(
                priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0
              );
              taxes = Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
            }

            const typeTotal = (baseFare + taxes) * count;
            totalBase += baseFare * count;
            totalTaxes += taxes * count;

            // Update or create price breakdown entry
            const existingEntry = this.priceBreakdown.find(
              (pb) => pb.type === type
            );
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
                total: typeTotal,
              });
            }
          }
        });
      });
    } else {
      // Handle single flight (one-way) or round-trip
      const departurePrices =
        this.departureFlight?.type === 'connecting' ||
        this.departureFlight?.type === 'connecting-return'
          ? this.departureFlight?.flights.reduce(
              (acc: any[], flight: any) => [
                ...acc,
                ...(flight.priceArray || []),
              ],
              []
            )
          : this.departureFlight?.priceArray || [];

      const returnPrices =
        this.arrivalFlight?.type === 'connecting' ||
        this.arrivalFlight?.type === 'connecting-return'
          ? this.arrivalFlight?.flights.reduce(
              (acc: any[], flight: any) => [
                ...acc,
                ...(flight.priceArray || []),
              ],
              []
            )
          : this.arrivalFlight?.priceArray || [];

      Object.entries(passengerCounts).forEach(([type, count]) => {
        if (count > 0) {
          let baseFare = 0;
          let taxes = 0;
          let baseFare2 = 0;
          let taxes2 = 0;

          // --- DEPARTURE HANDLING ---
          if (
            this.departureFlight?.type?.includes('connecting') ||
            this.departureFlight?.type?.includes('connecting-return')
          ) {
            this.departureFlight?.flights?.forEach((flight: any) => {
              const priceInfo =
                flight.priceArray?.find((p: any) => p.priceType === type) ||
                flight.priceArray?.[0];
              baseFare += Number(
                priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0
              );
              taxes += Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
            });
          } else {
            const priceInfo =
              departurePrices.find((p: any) => p.priceType === type) ||
              departurePrices[0];
            baseFare = Number(
              priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0
            );
            taxes = Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
          }

          // --- ARRIVAL HANDLING ---
          if (
            this.arrivalFlight?.type?.includes('connecting') ||
            this.arrivalFlight?.type?.includes('connecting-return')
          ) {
            this.arrivalFlight?.flights?.forEach((flight: any) => {
              const priceInfo =
                flight?.priceArray?.find((p: any) => p.priceType === type) ||
                flight.priceArray?.[0];
              baseFare2 += Number(
                priceInfo?.basePrice?.replace(/[^\d.]/g, '') || 0
              );
              taxes2 += Number(priceInfo?.taxes?.replace(/[^\d.]/g, '') || 0);
            });
          } else {
            const priceInfo2 =
              returnPrices.find((p: any) => p.priceType === type) ||
              returnPrices[0];
            baseFare2 = Number(
              priceInfo2?.basePrice?.replace(/[^\d.]/g, '') || 0
            );
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
            total: typeTotal,
          });
        }
      });

      // Handle return flight if it exists
    }

    this.totalBaseFare = totalBase;
    this.totalTaxes = totalTaxes;
    this.totalFare = totalBase + totalTaxes;
  }

  togglePriceDetails(): void {
    this.showPriceDetails = !this.showPriceDetails;
  }

  toggleBaseFareBreakdown(): void {
    this.showBaseFareBreakdown = !this.showBaseFareBreakdown;
  }

  // Helper function to get formatted city name from IATA code
  getCityName(iata: string): string {
    const airport = this.searchFormService.getAirportByIata(iata);
    return airport?.airportCity || iata;
  }

  // Helper function to get formatted layover details
  getLayoverText(layovers: { [key: string]: number }): string {
    if (!layovers || Object.keys(layovers).length === 0) return 'No layover';
    const [iataCode, minutes] = Object.entries(layovers)[0];
    const airport = this.getAirportByIata(iataCode); // This returns full airport object
    const city = airport ? airport.airportCity : iataCode; // Fallback to IATA if not found
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;

    return `${city} - ${hours} hrs ${mins} mins`;
  }

  // Function to format baggage details
  getFormattedBaggageDetail(baggageTexts: string[]): string {
    if (!Array.isArray(baggageTexts) || baggageTexts.length === 0) return 'N/A';

    let pieces: number | null = null;
    let weightInKg: string | null = null;

    for (const text of baggageTexts) {
      const trimmed = text.trim();

      // Extract piece info like 1P, 2P
      if (pieces === null && /^[0-9]+P$/i.test(trimmed)) {
        const match = trimmed.match(/^([0-9]+)P$/i);
        if (match) {
          pieces = parseInt(match[1], 10);
        }
      }

      // Extract weight like 23KG, 25K, etc.
      if (!weightInKg) {
        const kgMatch = trimmed.match(/([0-9]+)(K(G)?)/i);
        if (kgMatch) {
          weightInKg = `${kgMatch[1]}Kgs`;
        }
      }
    }

    const pcs = pieces !== null ? pieces : 1;
    if (weightInKg) {
      return `${weightInKg} (${pcs} Piece${
        pcs > 1 ? 's' : ''
      } * ${weightInKg})`;
    }

    return 'N/A';
  }
}
