package com.ktt.requestBuilder;

import org.springframework.stereotype.Component;

@Component
public class SeatMapRequestBuilder {

    public String buildSeatMapRequest(String airSegmentXml) {
        StringBuilder requestXml = new StringBuilder();

        // SOAP Envelope
        requestXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:air=\"http://www.travelport.com/schema/air_v52_0\" ")
                .append("xmlns:com=\"http://www.travelport.com/schema/common_v52_0\">")
                .append("<soapenv:Body>");

        // SeatMapReq with attributes
        requestXml.append("<air:SeatMapReq TargetBranch=\"P7232098\" ")
                .append("AuthorizedBy=\"APIUser\" ")
                .append("ReturnSeatPricing=\"false\">");

        // Billing Point of Sale Info
        requestXml.append("<com:BillingPointOfSaleInfo OriginApplication=\"UAPI\"/>");

        // Add the AirSegment
        requestXml.append(airSegmentXml);

        requestXml.append("</air:SeatMapReq>");
        requestXml.append("</soapenv:Body>");
        requestXml.append("</soapenv:Envelope>");

        return requestXml.toString();
    }
}