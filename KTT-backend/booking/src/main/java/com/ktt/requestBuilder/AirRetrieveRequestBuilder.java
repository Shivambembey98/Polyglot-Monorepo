package com.ktt.requestBuilder;

import org.springframework.stereotype.Component;

@Component
public class AirRetrieveRequestBuilder {

    public String buildRetrieveRequest(String providerLocatorCode, String providerCode) {
        StringBuilder soapRequestXml = new StringBuilder();

        soapRequestXml.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
                .append("<soap:Body>")
                .append("<univ:UniversalRecordRetrieveReq xmlns:univ=\"http://www.travelport.com/schema/universal_v51_0\" ")
                .append("AuthorizedBy=\"APIUser\" ")
                .append("TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\">")
                .append("<com:BillingPointOfSaleInfo xmlns:com=\"http://www.travelport.com/schema/common_v51_0\" ")
                .append("OriginApplication=\"uAPI\" ")
                .append("TargetBranch=\"P7232098\"/>");

        if("1G".equals(providerCode)) {
            soapRequestXml.append("<univ:ProviderReservationInfo ProviderLocatorCode=\"").append(providerLocatorCode).append("\" ")
                    .append("ProviderCode=\"").append(providerCode).append("\"/>");
        } else {
            soapRequestXml.append("<univ:ProviderReservationInfo ProviderCode=\"").append(providerCode).append("\" ")
                    .append("SupplierLocatorCode=\"").append(providerLocatorCode).append("\"/>");
        }

        soapRequestXml.append("</univ:UniversalRecordRetrieveReq>")
                .append("</soap:Body>")
                .append("</soap:Envelope>");

        return soapRequestXml.toString();
    }
}