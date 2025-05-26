package com.ktt.requestBuilder;

import com.ktt.dto.AirPriceRequestDto;
import com.ktt.dto.FlightDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
public class AirPriceRequestBuilder {



    public String buildAirPriceRequest(AirPriceRequestDto searchRequest,String passengerXml) {
        // Validate and sort flights by departure time
        validateAndSortFlights(searchRequest.getFlights());

        StringBuilder requestXml = new StringBuilder();


        // SOAP Envelope without header
        requestXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:air=\"http://www.travelport.com/schema/air_v52_0\" ")
                .append("xmlns:com=\"http://www.travelport.com/schema/common_v52_0\">")
                .append("<soapenv:Body>");

        // AirPriceReq with attributes (unchanged)
        requestXml.append("<air:AirPriceReq AuthorizedBy=\"APIUser\" ")
                .append("TargetBranch=\"P7232098\" ")
                .append("TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\" ")
                .append("CheckOBFees=\"All\" ")
                .append("FareRuleType=\"long\">");

        // Billing Point of Sale Info (unchanged)
        requestXml.append("<com:BillingPointOfSaleInfo OriginApplication=\"UAPI\"/>");

        // Air Itinerary (unchanged)
        requestXml.append("<air:AirItinerary>");
        requestXml.append(buildFlightSegment(searchRequest));
        requestXml.append("</air:AirItinerary>");

        // Air Pricing Modifiers (unchanged)
        requestXml.append("<air:AirPricingModifiers ETicketability=\"Yes\" FaresIndicator=\"AllFares\"/>");

        // Passenger Information (unchanged)
//        requestXml.append(buildPassengerSegment(searchRequest));
        requestXml.append(passengerXml);

        requestXml.append("</air:AirPriceReq>");
        requestXml.append("</soapenv:Body>");
        requestXml.append("</soapenv:Envelope>");

        return requestXml.toString();
    }

    // Keep all other methods exactly the same
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public void validateAndSortFlights(List<FlightDto> flights) {
        if (flights == null || flights.isEmpty()) {
            throw new IllegalArgumentException("At least one flight segment is required");
        }

        flights.sort(Comparator.comparing(f -> OffsetDateTime.parse(f.getDepartureTime(), DATE_FORMATTER)));

        for (int i = 1; i < flights.size(); i++) {
            OffsetDateTime prevDeparture = OffsetDateTime.parse(flights.get(i - 1).getDepartureTime(), DATE_FORMATTER);
            OffsetDateTime currDeparture = OffsetDateTime.parse(flights.get(i).getDepartureTime(), DATE_FORMATTER);

            if (currDeparture.isBefore(prevDeparture)) {
                throw new IllegalArgumentException(
                        "Invalid flight sequence: Flight " + flights.get(i).getFlightNumber() +
                                " departs before previous flight arrives");
            }
        }
    }


    private String buildFlightSegment(AirPriceRequestDto searchRequest) {
        StringBuilder segmentXml = new StringBuilder();
        List<FlightDto> flights = searchRequest.getFlights();

        // Identify hub airport (typically the first flight's destination)
        String hubAirport = flights.get(0).getDestination();

        for (int i = 0; i < flights.size(); i++) {
            FlightDto flight = flights.get(i);
         //   boolean isConnectingFlight = (i < flights.size() - 1);

            segmentXml.append("<air:AirSegment Key=\"").append(flight.getFlightKey()).append("\" ")
                    .append("Group=\"").append(flight.getGroup()).append("\" ")
                    .append("Carrier=\"").append(flight.getCarrier()).append("\" ")
                    .append("FlightNumber=\"").append(flight.getFlightNumber()).append("\" ")
                    .append("Origin=\"").append(flight.getOrigin()).append("\" ")
                    .append("Destination=\"").append(flight.getDestination()).append("\" ")
                    .append("DepartureTime=\"").append(flight.getDepartureTime()).append("\" ")
                    .append("ArrivalTime=\"").append(flight.getArrivalTime()).append("\" ")
                    .append("ETicketability=\"Yes\" ")
                    .append("ChangeOfPlane=\"false\" ")
                    .append("ParticipantLevel=\"Secure Sell\" ")
                    .append("LinkAvailability=\"true\" ")
                    .append("PolledAvailabilityOption=\"Polled avail used\" ")
                    .append("OptionalServicesIndicator=\"false\" ")
                    .append("AvailabilitySource=\"S\" ")
                    .append("AvailabilityDisplayType=\"Fare Shop/Optimal Shop\" ")
                    .append("ProviderCode=\"").append(flight.getProviderCode()).append("\">");

            // New connection logic
            boolean isFlightToHub = flight.getDestination().equals(hubAirport);
            boolean isFirstSegment = (i == 0);

            if (i < flights.size() - 1) {
                FlightDto nextFlight = flights.get(i + 1);
                if (flight.getGroup().equals(nextFlight.getGroup())) {
                    segmentXml.append("<air:Connection/>");
                }
            }

            segmentXml.append("</air:AirSegment>");
        }

        return segmentXml.toString();
    }

    private String calculateArrivalTime(FlightDto flight) {
        LocalDateTime departure = LocalDateTime.parse(flight.getDepartureTime(), DATE_FORMATTER);
        return departure.plusHours(2).plusMinutes(30).format(DATE_FORMATTER) + ":00.000+05:30";
    }

//    private String buildPassengerSegment(AirPriceRequestDto searchRequest) {
//        StringBuilder passengerSegment = new StringBuilder();
//        int passengerIndex = 1;
//
//        for (int i = 0; i < searchRequest.getAdults(); i++, passengerIndex++) {
//            passengerSegment.append("<com:SearchPassenger Code=\"ADT\" ")
//                    .append("BookingTravelerRef=\"PAX").append(passengerIndex).append("\"/>");
//        }
//
//        if (searchRequest.getChildren() > 0) {
//            passengerSegment.append("<com:SearchPassenger Code=\"CNN\" ")
//                    .append("Age=\"12\" ")
//                    .append("BookingTravelerRef=\"PAX").append(passengerIndex).append("\"/>");
//            passengerIndex++;
//        }
//
//        if (searchRequest.getInfants() > 0) {
//            passengerSegment.append("<com:SearchPassenger Code=\"INF\" ")
//                    .append("Age=\"1\" ")
//                    .append("PricePTCOnly=\"true\" ")
//                    .append("BookingTravelerRef=\"PAX").append(passengerIndex).append("\"/>");
//        }
//         if(searchRequest.getStudents()>0)
//         {
//             passengerSegment.append("<com:SearchPassenger Code=\"STU\" ")
//                     .append("BookingTravelerRef=\"PAX").append(passengerIndex).append("\"/>");
//         }
//              if(searchRequest.getSeniors()>0)
//        {
//            passengerSegment.append("<com:SearchPassenger Code=\"SRC\" ")
//                    .append("Age=\"60\"")
//                    .append("BookingTravelerRef=\"PAX").append(passengerIndex).append("\"/>");
//        }
//        return passengerSegment.toString();
//    }
}