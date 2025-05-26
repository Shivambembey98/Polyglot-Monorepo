import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { development_environment } from '../../../environments/environment';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SearchFormService {
  // url = development_environment.apiUrl;
  lccUrl = development_environment.lccUrl;
  private selectedFlight: any = null;

  // Airport List
  private airportListSubject = new BehaviorSubject<any[]>([]);
  public airportList$ = this.airportListSubject.asObservable();
  
  constructor(private httpClient: HttpClient, private router: Router) {
    this.loadAirportData();
  }
  
  private loadAirportData(): any {
    this.httpClient.get<any[]>('/assets/airports.json').subscribe((data) => {      
      const formatted = data.map((airport) => ({
        ...airport,
        displayLabel: `${airport.airportCity} (${airport.iataCode})`, 
        iataCode: airport.iataCode,
        airportCity: airport.airportCity,
        airportName: airport.airportName,
      }));
      this.airportListSubject.next(formatted);
    });
  }
  
  getAirportByIata(iata: string): any | undefined {
    const list = this.airportListSubject.getValue();
    return list.find((a: { iataCode: string }) => a.iataCode === iata);
  }  

  // ✳️ Your existing API and flight storage methods below
  getAirports(query: string): Observable<any[]> {
    const params = new HttpParams().set('query', query);
    return this.httpClient.get<any[]>(`${this.lccUrl}/search`, { params });
  }

  getAllLowCostCarrier(formData: any): any {
    return this.httpClient.post(
      `${this.lccUrl}/low-fare-search`,
      formData,
      { responseType: 'text' as 'json' }
    );
  }

  getAirPrice(formData: any): any {
    return this.httpClient.post(
      this.lccUrl + '/air-price',
      formData,
      { responseType: 'text' as 'json' }
    );
  }
  createAirReservation(formData: any): Observable<any> {
    return this.httpClient.post(
      this.lccUrl + '/air-reservation',
      formData,
      { responseType: 'text' as 'json' } // or 'json' if the backend sends JSON
    );
  }  
  retrieveTicket(locatorCode: string, providerCode: string) {
    const params = new HttpParams()
      .set('locatorCode', locatorCode)
      .set('providerCode', providerCode);
  
    return this.httpClient.post(
      this.lccUrl + '/retrieve-ticket',
      {},
      {
        params,
        responseType: 'text' as 'json'  // 👈 treat XML as plain text
      }
    );
  } 
  airTicket(airReservationLocatorCode: string) {
    const params = new HttpParams()
      .set('airReservationLocatorCode', airReservationLocatorCode)  

    return this.httpClient.post(
      this.lccUrl + '/air-ticket',
      {},
      {
        params,
        responseType: 'text' as 'json'  // 👈 treat XML as plain text
      }
    );
  } 

  setSelectedFlight(flight: any): void {
    this.selectedFlight = flight;
    localStorage.setItem('selectedFlight', JSON.stringify(flight));
  }

  getSelectedFlight(): any {
    if (this.selectedFlight) return this.selectedFlight;

    const stored = localStorage.getItem('selectedFlight');
    if (stored) {
      this.selectedFlight = JSON.parse(stored);
      return this.selectedFlight;
    }
    return null;
  }

  clearSelectedFlight(): any {
    this.selectedFlight = null;
    localStorage.removeItem('selectedFlight');
  }
}