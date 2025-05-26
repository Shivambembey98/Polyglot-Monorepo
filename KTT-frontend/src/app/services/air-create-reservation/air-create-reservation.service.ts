import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AirCreateReservationService {
  private readonly STORAGE_KEY = 'airReservationFormData';

  setFormData(data: any) {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(data));
  }

  getFormData(): any {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  }

  clearFormData() {
    localStorage.removeItem(this.STORAGE_KEY);
  }
  extractReservationInfoFromXml(xmlString: string): { locatorCode: string, providerCode: string } {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, 'text/xml');
  
    const UNIVERSAL_NS = 'http://www.travelport.com/schema/universal_v52_0';
    const providerReservationInfo = xmlDoc.getElementsByTagNameNS(UNIVERSAL_NS, 'ProviderReservationInfo')[0];
  
    const locatorCode = providerReservationInfo?.getAttribute('LocatorCode') ?? 'NotFound';
    const providerCode = providerReservationInfo?.getAttribute('ProviderCode') ?? 'NotFound';
  
    return { locatorCode, providerCode };
  }
}
