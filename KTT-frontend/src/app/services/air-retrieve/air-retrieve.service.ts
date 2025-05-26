import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AirRetrieveService {

  constructor() {}

  retrievexmlparser(xmlString: string): { airReservationLocatorCode: string } {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, 'text/xml');
  
    // More robust search ignoring namespace
    const allElements = xmlDoc.getElementsByTagName('*');
    let airReservationLocatorCode = 'NotFound';
  
    for (let i = 0; i < allElements.length; i++) {
      const element = allElements[i];
      if (element.localName === 'AirReservation') {
        airReservationLocatorCode = element.getAttribute('LocatorCode') ?? 'NotFound';
        break;
      }
    }
  
    return { airReservationLocatorCode };
  }   
}
