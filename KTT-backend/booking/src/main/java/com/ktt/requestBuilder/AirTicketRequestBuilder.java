package com.ktt.requestBuilder;

import org.springframework.stereotype.Component;

@Component
public class AirTicketRequestBuilder {

    public String buildAirTicketRequest(String airReservationLocatorCode) {
        StringBuilder soapRequestXml = new StringBuilder();

        soapRequestXml.append("<soap:Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">")
                .append("<soap:Body>")
                .append("<AirTicketingReq xmlns=\"http://www.travelport.com/schema/air_v52_0\" ")
                .append("TargetBranch=\"P7232098\" ")
                .append("TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\" ")
                .append("AuthorizedBy=\"APIUser\" ")
                .append("ReturnInfoOnFail=\"true\" ")
                .append("BulkTicket=\"false\">")
                .append("<BillingPointOfSaleInfo xmlns=\"http://www.travelport.com/schema/common_v52_0\" ")
                .append("OriginApplication=\"uAPI\"/>")
                .append("<AirReservationLocatorCode>").append(airReservationLocatorCode).append("</AirReservationLocatorCode>")
                .append("</AirTicketingReq>")
                .append("</soap:Body>")
                .append("</soap:Envelope>");

        return soapRequestXml.toString();
    }
}
