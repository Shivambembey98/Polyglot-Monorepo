import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router'; // ✅ needed for navigation

@Component({
  selector: 'app-top-flight-routes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './top-flight-routes.component.html',
  styleUrl: './top-flight-routes.component.css'
})
export class TopFlightRoutesComponent {
  constructor(private router: Router) {}

  searchFlight(from: string, to: string): void {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const yyyy = tomorrow.getFullYear();
    const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
    const dd = String(tomorrow.getDate()).padStart(2, '0');

    const formattedDate = `${yyyy}-${mm}-${dd}`; // e.g. 2025-05-24

    const queryParams: any = {
      origin: from,
      destination: to,
      fromDate: formattedDate,
      formType: 'one-way',
      students: '0',
      adults:'1',
      armedForces: '0',
      seniorCitizens: '0',
      medicalProfessionals: '0',
    };

    this.router.navigate(['/flight-search'], { queryParams });
  }
}
