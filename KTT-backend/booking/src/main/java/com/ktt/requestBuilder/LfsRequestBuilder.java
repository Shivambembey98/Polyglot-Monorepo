package com.ktt.requestBuilder;

import com.ktt.config.BookingTravelerRefUtil;
import com.ktt.dto.SearchDto;
import com.ktt.dto.SearchLegDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LfsRequestBuilder {
    @Autowired
    BookingTravelerRefUtil bookingTravelerRefUtil;

    public String
    buildSoapRequestSearch(SearchDto searchRequest) {
        StringBuilder soapRequestXml = new StringBuilder();

        soapRequestXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:air=\"http://www.travelport.com/schema/air_v51_0\" xmlns:com=\"http://www.travelport.com/schema/common_v51_0\">")
                .append("<soapenv:Body>")
                .append("<air:LowFareSearchReq TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\" TargetBranch=\"P7232098\" SolutionResult=\"false\" AuthorizedBy=\"APIUser\">")
                .append("<com:BillingPointOfSaleInfo OriginApplication=\"uAPI\" />");

        // Handle multi-city or single-leg searches
        // Handle multi-city search first
        if (searchRequest.getLegs() != null && !searchRequest.getLegs().isEmpty()) {
            for (SearchLegDto leg : searchRequest.getLegs()) {
                soapRequestXml.append("<air:SearchAirLeg>")
                        .append("<air:SearchOrigin><com:CityOrAirport Code=\"").append(leg.getOrigin()).append("\"/></air:SearchOrigin>")
                        .append("<air:SearchDestination><com:CityOrAirport Code=\"").append(leg.getDestination()).append("\"/></air:SearchDestination>")
                        .append("<air:SearchDepTime PreferredTime=\"").append(leg.getFromDate()).append("\"/>")
                        .append("</air:SearchAirLeg>");
            }
        } else {
            // One-way trip (default)
            soapRequestXml.append("<air:SearchAirLeg>")
                    .append("<air:SearchOrigin><com:CityOrAirport Code=\"").append(searchRequest.getOrigin()).append("\"/></air:SearchOrigin>")
                    .append("<air:SearchDestination><com:CityOrAirport Code=\"").append(searchRequest.getDestination()).append("\"/></air:SearchDestination>")
                    .append("<air:SearchDepTime PreferredTime=\"").append(searchRequest.getFromDate()).append("\"/>")
                    .append("</air:SearchAirLeg>");

            // ✅ Round-Trip Handling: Add a second leg if "toDate" exists
            if (searchRequest.getToDate() != null && !searchRequest.getToDate().isEmpty()) {
                soapRequestXml.append("<air:SearchAirLeg>")
                        .append("<air:SearchOrigin><com:CityOrAirport Code=\"").append(searchRequest.getDestination()).append("\"/></air:SearchOrigin>")
                        .append("<air:SearchDestination><com:CityOrAirport Code=\"").append(searchRequest.getOrigin()).append("\"/></air:SearchDestination>")
                        .append("<air:SearchDepTime PreferredTime=\"").append(searchRequest.getToDate()).append("\"/>")
                        .append("</air:SearchAirLeg>");
            }
        }
//		.append("<com:Provider Code=\"ACH\" />")
        if (Objects.equals(searchRequest.getProviderCode(), "ACH")) {
            soapRequestXml.append("<air:AirSearchModifiers>")
                    .append("<air:PreferredProviders>")
                    .append("<com:Provider Code=\"ACH\" />")
                    .append("</air:PreferredProviders>")
                    .append("<air:FlightType TripleInterlineCon=\"false\" DoubleInterlineCon=\"false\" SingleInterlineCon=\"true\" "
                            + "TripleOnlineCon=\"false\" DoubleOnlineCon=\"false\" SingleOnlineCon=\"true\" StopDirects=\"true\" NonStopDirects=\"true\"/>") // ✅ Correct placement
                    .append("</air:AirSearchModifiers>");

        } else {
            // Air Search Modifiers
            soapRequestXml.append("<air:AirSearchModifiers>")
                    .append("<air:PreferredProviders>")
                    .append("<com:Provider Code=\"1G\" />")
                    .append("</air:PreferredProviders>")
                    .append("<air:FlightType TripleInterlineCon=\"false\" DoubleInterlineCon=\"false\" SingleInterlineCon=\"true\" "
                            + "TripleOnlineCon=\"false\" DoubleOnlineCon=\"false\" SingleOnlineCon=\"true\" StopDirects=\"true\" NonStopDirects=\"true\"/>") // ✅ Correct placement
                    .append("</air:AirSearchModifiers>");
        }

        // Adding passenger details
        soapRequestXml.append(addPassengerDetails(searchRequest));

        // Pricing Modifiers
        soapRequestXml.append("<air:AirPricingModifiers FaresIndicator=\"AllFares\" ETicketability=\"Yes\" />")
                .append("<air:FareRulesFilterCategory><air:CategoryCode>CHG</air:CategoryCode></air:FareRulesFilterCategory>")
                .append("</air:LowFareSearchReq>")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");

        return soapRequestXml.toString();
    }

    private String addPassengerDetails(SearchDto searchRequest) {
        StringBuilder passengerDetails = new StringBuilder();
        passengerDetails.append(travellerSnippet("ADT", searchRequest.getAdults()));
        passengerDetails.append(travellerSnippet("INF", searchRequest.getInfants()));
        passengerDetails.append(travellerSnippet("CNN", searchRequest.getChildren()));
        passengerDetails.append(travellerSnippet("STU", searchRequest.getStudents()));
        passengerDetails.append(travellerSnippet("SRC", searchRequest.getSeniorCitizens()));
        return passengerDetails.toString();
    }

    private String travellerSnippet(String code, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String ref = BookingTravelerRefUtil.generateBase64UUID() + i;
            if (code.equals("ADT")) {
                stringBuilder.append("<com:SearchPassenger Code=\"ADT\" BookingTravelerRef=\"").append(ref).append(i).append("\" />");
            } else if (code.equals("STU")) {
                stringBuilder.append("<com:SearchPassenger Code=\"STU\" BookingTravelerRef=\"").append(ref).append("\" />");
            } else if (code.equals("SRC")) {
                stringBuilder.append("<com:SearchPassenger Code=\"SRC\" Age=\"62\" BookingTravelerRef=\"").append(ref).append("\" />");
            } else if (code.equals("INF")) {
                stringBuilder.append("<com:SearchPassenger Code=\"INF\" Age=\"01\" BookingTravelerRef=\"").append(ref)
                        .append(i).append("\" PricePTCOnly=\"true\" />");
            } else if (code.equals("CNN")) {
                stringBuilder.append("<com:SearchPassenger Code=\"CNN\" Age=\"10").append("\" BookingTravelerRef=\"").append(ref).append("\" />");
            }
        }
        return stringBuilder.toString();
    }
//
//	lowfair>>airprice>>aircreate>>airretrive>>airTicket

}


//
//package com.ktt.booking.requestBuilder;
//
//import com.ktt.booking.dto.SearchDto;
//import com.ktt.booking.dto.SearchLegDto;
//import org.springframework.stereotype.Component;
//
//@Component
//public class LfsRequestBuilder {
//
//	public String buildSoapRequest(SearchDto searchDto, String airlineCode) {
//		StringBuilder soapRequestXml = new StringBuilder();
//
//		// SOAP Envelope Start
//		soapRequestXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
//				.append("xmlns:air=\"http://www.travelport.com/schema/air_v51_0\" ")
//				.append("xmlns:com=\"http://www.travelport.com/schema/common_v51_0\">")
//				.append("<soapenv:Body>")
//				.append("<air:LowFareSearchReq TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\" ")
//				.append("TargetBranch=\"P7232098\" SolutionResult=\"false\" AuthorizedBy=\"TBO\">")
//				.append("<com:BillingPointOfSaleInfo OriginApplication=\"uAPI\" />");
//
//		// ✅ Handle multi-city or single-leg search
//		if (searchDto.getLegs() != null && !searchDto.getLegs().isEmpty()) {
//			// Multi-city search: Loop through each leg
//			for (SearchLegDto leg : searchDto.getLegs()) {
//				soapRequestXml.append(buildLegSegment(leg.getOrigin(), leg.getDestination(), leg.getFromDate()));
//			}
//		} else {
//			// Single-leg (one-way or round-trip) search
//			soapRequestXml.append(buildLegSegment(searchDto.getOrigin(), searchDto.getDestination(), searchDto.getFromDate()));
//
//			// Handle round-trip (return date)
//			if (searchDto.getToDate() != null && !searchDto.getToDate().isEmpty()) {
//				soapRequestXml.append(buildLegSegment(searchDto.getDestination(), searchDto.getOrigin(), searchDto.getToDate()));
//			}
//		}
//
//		// ✅ Air Search Modifiers
//		soapRequestXml.append("<air:AirSearchModifiers>")
//				.append("<air:PreferredProviders>")
//				.append("<com:Provider Code=\"ACH\" />")
//				.append("<com:Provider Code=\"1G\" />")
//				.append("</air:PreferredProviders>")
//
//				// ✅ FlightType is now correctly namespaced
//				.append("<air:FlightType TripleInterlineCon=\"false\" DoubleInterlineCon=\"false\" ")
//				.append("SingleInterlineCon=\"true\" TripleOnlineCon=\"false\" DoubleOnlineCon=\"false\" ")
//				.append("SingleOnlineCon=\"true\" StopDirects=\"true\" NonStopDirects=\"true\"/>")
//
//				.append("</air:AirSearchModifiers>");
//
//		// ✅ Adding Passenger Details
//		soapRequestXml.append(addPassengerDetails(searchDto));
//
//		// ✅ Pricing Modifiers
//		soapRequestXml.append("<air:AirPricingModifiers FaresIndicator=\"AllFares\" ETicketability=\"Required\" />")
//				.append("<air:FareRulesFilterCategory><air:CategoryCode>CHG</air:CategoryCode></air:FareRulesFilterCategory>")
//				.append("</air:LowFareSearchReq>")
//				.append("</soapenv:Body>")
//				.append("</soapenv:Envelope>");
//
//		return soapRequestXml.toString();
//	}
//
//	private String buildLegSegment(String origin, String destination, String fromDate) {
//		return "<air:SearchAirLeg>"
//				+ "<air:SearchOrigin><com:CityOrAirport Code=\"" + origin + "\"/></air:SearchOrigin>"
//				+ "<air:SearchDestination><com:CityOrAirport Code=\"" + destination + "\"/></air:SearchDestination>"
//				+ "<air:SearchDepTime PreferredTime=\"" + formatDate(fromDate) + "\"/>"
//				+ "</air:SearchAirLeg>";
//	}
//
//	private String addPassengerDetails(SearchDto searchRequest) {
//		StringBuilder passengerDetails = new StringBuilder();
//		passengerDetails.append(travellerSnippet("ADT", searchRequest.getAdults()));
//		passengerDetails.append(travellerSnippet("INF", searchRequest.getInfants()));
//		passengerDetails.append(travellerSnippet("CNN", searchRequest.getChildren()));
//		passengerDetails.append(travellerSnippet("STU", searchRequest.getStudents()));
//		passengerDetails.append(travellerSnippet("SRC", searchRequest.getSeniorCitizens()));
//		return passengerDetails.toString();
//	}
//
//	private String travellerSnippet(String code, int count) {
//		StringBuilder stringBuilder = new StringBuilder();
//		for (int i = 0; i < count; i++) {
//			if ("INF".equals(code)) {
//				// ✅ Fix: Move PricePTCOnly as an attribute instead of a child element
//				stringBuilder.append("<com:SearchPassenger Code=\"INF\" Age=\"1\" BookingTravelerRef=\"PAX")
//						.append(i).append("\" PricePTCOnly=\"true\" />");
//			} else if ("CNN".equals(code)) {
//				stringBuilder.append("<com:SearchPassenger Code=\"CNN\" Age=\"")
//						.append(i + 2).append("\" BookingTravelerRef=\"PAX").append(i).append("\" />");
//			} else {
//				stringBuilder.append("<com:SearchPassenger Code=\"").append(code)
//						.append("\" BookingTravelerRef=\"PAX").append(i).append("\" />");
//			}
//		}
//		return stringBuilder.toString();
//	}
//	private String formatDate(String date) {
//		if (date == null || date.isEmpty()) {
//			return ""; // Handle null case
//		}
//		if (date.length() == 10) {
//			return date;
//		}
//		return date.substring(0, 10);
//	}
//}



