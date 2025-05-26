import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'https://dev-backend.kwiktraveltrip.com/admin/tenants';

  constructor(private httpClient: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    const params = new HttpParams()
      .set('email', email)
      .set('password', password);

    return this.httpClient.post(`${this.baseUrl}/login`, {}, { params });
  }

  getTenantDetails(id: string): Observable<any> {
    return this.httpClient.get(`${this.baseUrl}/${id}`);
  }
  updateTenantProfile(payload: any): Observable<any> {
    return this.httpClient.put<any>(
      `${this.baseUrl}/${payload.id}`,
      payload
    );
  }  
}
