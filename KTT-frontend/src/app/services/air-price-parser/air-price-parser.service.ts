import { Injectable } from '@angular/core';

export interface BaggageInfo {
  checked: string[];
  carryOn: string[];
}

export interface ParsedAirPrice {
  baggageInfo: BaggageInfo;
  refundability?: string;
  sessionIdAirPrice?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AirPriceParserService {
  constructor() {}

  parseAirPriceXML(xmlString: string): ParsedAirPrice {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, 'application/xml');    
  
    const checked: string[] = [];
    const carryOn: string[] = [];
  
    const extractTextValues = (parent: Element, tag: string): string[] => {
      return Array.from(parent.getElementsByTagNameNS('*', tag)).map(
        (node) => node.textContent?.trim().toUpperCase() || ''
      ).filter(Boolean);
    };
  
    // ✅ Checked Baggage
    const baggageAllowance = Array.from(xmlDoc.getElementsByTagNameNS('*', 'BaggageAllowanceInfo'));
    baggageAllowance.forEach((node) => {
      const topLevelText = extractTextValues(node, 'Text');
      topLevelText.forEach((val) => {
        if (!checked.includes(val)) checked.push(val);
      });
  
      const bagDetails = Array.from(node.getElementsByTagNameNS('*', 'BagDetails'));
      bagDetails?.forEach((detail) => {
        const restrictionTexts = extractTextValues(detail, 'Text');
        restrictionTexts.forEach((val) => {
          if (!checked.includes(val)) checked.push(val);
        });
      });
    });
  
    // ✅ Carry-On Baggage
    const carryNodes = Array.from(xmlDoc.getElementsByTagNameNS('*', 'CarryOnAllowanceInfo'));
    carryNodes?.forEach((node) => {
      const textValues = extractTextValues(node, 'Text');
      textValues.forEach((val) => {
        if (!carryOn.includes(val)) carryOn.push(val);
      });
  
      const carryDetails = Array.from(node.getElementsByTagNameNS('*', 'CarryOnDetails'));
      carryDetails?.forEach((detail) => {
        const restrictionTexts = extractTextValues(detail, 'Text');
        restrictionTexts.forEach((val) => {
          if (!carryOn.includes(val)) carryOn.push(val);
        });
      });
    });
  
    // ✅ Refundability
    const pricing = xmlDoc.getElementsByTagNameNS('*', 'AirPricingInfo')[0];
    const refundability = pricing?.getAttribute('Refundable') === 'true' ? 'Refundable' : 'Non-refundable';
  
    // ✅ Session ID Air Price
    const sessionIdNode = xmlDoc.getElementsByTagName('sessionIdAirPrice')[0];
    const sessionIdAirPrice = sessionIdNode?.textContent?.trim() || '';
  
    if (sessionIdAirPrice) {
      localStorage.setItem('sessionIdAirPrice', sessionIdAirPrice);
    }
    console.log('sessionIdairPrice',sessionIdAirPrice);
    
  
    return {
      baggageInfo: {
        checked,
        carryOn,
      },
      refundability,
      sessionIdAirPrice,
    };
  }     
}