package com.ktt.requestBuilder;

import com.ktt.dto.AirCreateReservationDto;
import com.ktt.dto.SearchPassengerRef;
import com.ktt.dto.TravelDto;
import com.ktt.util.AirReservationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AirCreateReservationBuilder {
    private static final Logger logger = LoggerFactory.getLogger(AirReservationUtility.class);

    public String buildAirCreateReservationRequest(AirCreateReservationDto reservationDTo, String airPricingSolutionXml, String airSegmentListXml, List<String> airPricingInfoXml, String fairNoteXml, String passengerDetails) {
        StringBuilder requestXml = new StringBuilder();


// add > for close the tag
        requestXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">");
//                .append("xmlns:univ=\"http://www.travelport.com/schema/universal_v52_0\" ")
//                .append("xmlns:air=\"http://www.travelport.com/schema/air_v52_0\" ")
//                .append("xmlns:com=\"http://www.travelport.com/schema/common_v52_0\" ")
//                .append("xmlns:common=\"http://www.travelport.com/schema/common_v52_0\" ")
//                .append("xmlns:common_v52_0=\"http://www.travelport.com/schema/common_v52_0\" ")
//                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

        requestXml.append("<soapenv:Header/>")
                .append("<soapenv:Body>")
                .append("<univ:AirCreateReservationReq xmlns:univ=\"http://www.travelport.com/schema/universal_v52_0\" ")
                .append("xmlns:air=\"http://www.travelport.com/schema/air_v52_0\" ")
                .append("xmlns:com=\"http://www.travelport.com/schema/common_v52_0\" ")
                .append("xmlns:common=\"http://www.travelport.com/schema/common_v52_0\" ")
                .append("xmlns:common_v52_0=\"http://www.travelport.com/schema/common_v52_0\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("AuthorizedBy=\"APIUser\" RetainReservation=\"Both\" ")
                .append("TargetBranch=\"P7232098\" TraceId=\"a0a168a8-1ad3-44d5-b70b-1ccec46b81e8\" RestrictWaitlist=\"true\">");

        requestXml.append("<common_v52_0:BillingPointOfSaleInfo OriginApplication=\"UAPI\"/>");
        List<SearchPassengerRef> refs = extractPassengerRefs(passengerDetails);

        List<SearchPassengerRef> refer = extractPassengerXmlElements(passengerDetails);
        String updatedAirPricingInfo = correctAirPrice(airPricingInfoXml, refer);

        Map<String, List<String>> travelerRefMap = refs.stream()
                .collect(Collectors.groupingBy(
                        SearchPassengerRef::getCode,
                        Collectors.mapping(SearchPassengerRef::getBookingTravelerRef, Collectors.toList())
                ));

        boolean addressAdded = false;
        int i = 0;
        // add > for close the tag BookingTraveler
        for (TravelDto travelDto : reservationDTo.getTravelDtoList()) {
            String travelerType = travelDto.getTravelerType();

            if (travelerRefMap.containsKey(travelerType) && !travelerRefMap.get(travelerType).isEmpty()) {
                String bookingTravelerRef = travelerRefMap.get(travelerType).remove(0);

                requestXml.append("<common_v52_0:BookingTraveler Gender=\"").append(travelDto.getGender()).append("\" ")
                        .append("TravelerType=\"").append(travelerType).append("\" ")
                        .append("Key=\"").append(bookingTravelerRef).append("\">");

                requestXml.append("<common_v52_0:BookingTravelerName First=\"").append(travelDto.getFirstName())
                        .append("\" Last=\"").append(travelDto.getLastName())
                        .append("\" Prefix=\"").append(travelDto.getPrefix()).append("\"/>");

                requestXml.append("<common_v52_0:PhoneNumber Number=\"").append(travelDto.getPhoneNumber())
                        .append("\" Type=\"Mobile\"/>");

                requestXml.append("<common_v52_0:Email EmailID=\"").append(travelDto.getEmail()).append("\"/>");

                // SSR - DOCS
                requestXml.append("<common_v52_0:SSR Type=\"DOCS\" Status=\"HK\" Carrier=\"")
                        .append(reservationDTo.getAirLine()).append("\" ")
                        .append("FreeText=\"").append(travelDto.getDocText()).append("\"/>");

                // Add Wheelchair SSR if needed (only for ADT or CNN)
                if (travelDto.isWheelchairNeeded() && ("ADT".equals(travelerType) || "CNN".equals(travelerType))) {
                    int ssrKeyWCHR = generateRandomIntegerKey();
                    requestXml.append("<common_v52_0:SSR Carrier=\"")
                            .append(reservationDTo.getAirLine()).append("\" Key=\"")
                            .append(ssrKeyWCHR).append("\" SegmentRef=\"")
                            .append(getSegmentRef(airSegmentListXml)) // New method to extract segment reference
                            .append("\" Status=\"NN\" Type=\"WCHR\" FreeText=\"SICK PASSENGER\"/>");
                }

                int ssrKeyCTCM = generateRandomIntegerKey();
                int ssrKeyCTCE = generateRandomIntegerKey();

                requestXml.append("<common_v52_0:SSR Key=\"").append(ssrKeyCTCM).append("\" Type=\"CTCM\" Status=\"HK\" Carrier=\"")
                        .append(reservationDTo.getAirLine()).append("\" ")
                        .append("FreeText=\"").append(travelDto.getPhoneNumber()).append("\"/>");

                requestXml.append("<common_v52_0:SSR Key=\"").append(ssrKeyCTCE).append("\" Type=\"CTCE\" Status=\"HK\" Carrier=\"")
                        .append(reservationDTo.getAirLine()).append("\" ")
                        .append("FreeText=\"").append(travelDto.getEmail().replace("@", "//")).append("\"/>");

                if (!addressAdded) {
                    requestXml.append("<common_v52_0:Address>")
                            .append("<common_v52_0:AddressName>Home</common_v52_0:AddressName>")
                            .append("<common_v52_0:Street>").append("123 Main Street").append("</common_v52_0:Street>")
                            .append("<common_v52_0:City>").append("Jaipur").append("</common_v52_0:City>")
                            .append("<common_v52_0:State>").append("RJ").append("</common_v52_0:State>")
                            .append("<common_v52_0:PostalCode>").append("302001").append("</common_v52_0:PostalCode>")
                            .append("<common_v52_0:Country>").append("IN").append("</common_v52_0:Country>")
                            .append("</common_v52_0:Address>");
                    addressAdded = true;
                }

                // For Child/Infant/CNN Traveler DOB as NameRemark
                if (travelerType.equals("CNN") || travelerType.equals("INF") || travelerType.equals("CHD")) {
                    requestXml.append("<common_v52_0:NameRemark>")
                            .append("<common_v52_0:RemarkData>").append(travelDto.getDob()).append("</common_v52_0:RemarkData>")
                            .append("</common_v52_0:NameRemark>");
                }


                requestXml.append("</common_v52_0:BookingTraveler>");
            }
            i++;


        }
        requestXml.append("<common_v52_0:ContinuityCheckOverride>true</common_v52_0:ContinuityCheckOverride>");
        requestXml.append("<common_v52_0:AgencyContactInfo>")
                // Agency info
                .append("<common_v52_0:PhoneNumber CountryCode=\"91\" AreaCode=\"0744\" Number=\"9829036162\" ")
                .append("Location=\"KTU\" Type=\"Agency\"/>")
                .append("</common_v52_0:AgencyContactInfo>");
        //
        requestXml.append("<common_v52_0:EmailNotification Recipients=\"All\"/>");
        requestXml.append("<common_v52_0:FormOfPayment Type=\"Cash\">")
                .append("</common_v52_0:FormOfPayment>");

        // Append AirSegmentList and AirPricingSolution
        requestXml.append(airPricingSolutionXml);
        requestXml.append(airSegmentListXml);
        requestXml.append(updatedAirPricingInfo);
        requestXml.append(fairNoteXml);
        requestXml.append("</air:AirPricingSolution>");

        requestXml.append("<common_v52_0:ActionStatus Type=\"ACTIVE\" TicketDate=\"T*\" ProviderCode=\"1G\"/>");

        requestXml.append("</univ:AirCreateReservationReq>")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");
        System.out.println(airPricingInfoXml.size());


        return requestXml.toString();

    }

    private int generateRandomIntegerKey() {
        Random random = new Random();
        return 100 + random.nextInt(900); // generates a number between 100 and 999
    }

    private List<SearchPassengerRef> extractPassengerRefs(String passengerDetailsXml) {
        List<SearchPassengerRef> passengerRefs = new ArrayList<>();
        if (passengerDetailsXml == null) return passengerRefs;

        try {
            Pattern pattern = Pattern.compile("Code\\s*=\\s*\"(\\w+)\"[^>]*?BookingTravelerRef\\s*=\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(passengerDetailsXml);
            while (matcher.find()) {
                passengerRefs.add(new SearchPassengerRef(matcher.group(1), matcher.group(2)));
            }
        } catch (Exception e) {
            logger.warn("Error extracting passenger refs: {}", e.getMessage());
        }
        return passengerRefs;
    }


    private String correctAirPrice(List<String> airPricingInfoXmlList, List<SearchPassengerRef> refs) {
        if (airPricingInfoXmlList == null || airPricingInfoXmlList.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (String xmlBlock : airPricingInfoXmlList) {
            // Find end of FareCalc
            int endTagIndex = xmlBlock.lastIndexOf("</air:FareCalc>");
            if (endTagIndex == -1) {
                // No closing tag, append as-is
                result.append(xmlBlock);
                continue;
            }

            // Keep everything up to and including </air:FareCalc>
            int afterFareCalc = endTagIndex + "</air:FareCalc>".length();
            String beforeInsert = xmlBlock.substring(0, afterFareCalc);
            String afterInsert = xmlBlock.substring(afterFareCalc);

            StringBuilder blockWithRefs = new StringBuilder(beforeInsert);

            // Extract passengerTypeCode from the block
            String passengerType = extractPassengerTypeCode(xmlBlock);

            // Append all matching ref entries
            for (SearchPassengerRef ref : refs) {
                if (passengerType != null && passengerType.equals(ref.getCode())) {
                    blockWithRefs.append(convertToXml(ref));
                    blockWithRefs.append("<air:AirPricingModifiers FaresIndicator=\"AllFares\" ETicketability=\"Yes\" />");

                }
            }

            // Remove duplicate </air:AirPricingInfo> if it's in afterInsert
            String remaining = afterInsert.replace("</air:AirPricingInfo>", "");
            blockWithRefs.append(remaining);

            // Always append closing AirPricingInfo tag
            blockWithRefs.append("</air:AirPricingInfo>");
            result.append(blockWithRefs.toString());
        }

        return result.toString();
    }

    /**
     * Extracts the value of PassengerTypeCode="..." from an AirPricingInfo fragment.
     */
    private String extractPassengerTypeCode(String xml) {
        String marker = "PassengerTypeCode=\"";
        int start = xml.indexOf(marker);
        if (start == -1) return null;
        start += marker.length();
        int end = xml.indexOf('"', start);
        if (end == -1) return null;
        return xml.substring(start, end);
    }

    //
    private String convertToXml(SearchPassengerRef ref) {
        StringBuilder xml = new StringBuilder();

        xml.append("<air:PassengerType Code=\"")
                .append(ref.getCode())
                .append("\" BookingTravelerRef=\"")
                .append(ref.getBookingTravelerRef())
                .append("\"");

        // Add Age attribute for INF and CNN if age is present
        if (("INF".equals(ref.getCode()) || "CNN".equals(ref.getCode())) && ref.getAge() != null) {
            xml.append(" Age=\"").append(ref.getAge()).append("\"");
        }

        xml.append("/>");
        return xml.toString();
    }

//


    private List<SearchPassengerRef> extractPassengerXmlElements(String passengerDetailsXml) {
        List<SearchPassengerRef> passengerRefs = new ArrayList<>();
        if (passengerDetailsXml == null) return passengerRefs;

        try {
            Pattern mainPattern = Pattern.compile("<com:SearchPassenger\\b[^>]*?/>");
            Matcher mainMatcher = mainPattern.matcher(passengerDetailsXml);

            while (mainMatcher.find()) {
                String tag = mainMatcher.group(); // the full <com:SearchPassenger ... />

                String code = null;
                String ref = null;
                String age = null;

                Pattern attrPattern = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]*)\"");
                Matcher attrMatcher = attrPattern.matcher(tag);

                while (attrMatcher.find()) {
                    String attrName = attrMatcher.group(1);
                    String attrValue = attrMatcher.group(2);

                    switch (attrName) {
                        case "Code":
                            code = attrValue;
                            break;
                        case "BookingTravelerRef":
                            ref = attrValue;
                            break;
                        case "Age":
                            age = attrValue;
                            break;
                    }
                }

                if (code != null && ref != null) {
                    passengerRefs.add(new SearchPassengerRef(code, ref, age));
                }
            }

        } catch (Exception e) {
            logger.warn("Error extracting SearchPassengerRef from XML: {}", e.getMessage());
        }

        return passengerRefs;
    }
    private String getSegmentRef(String airSegmentListXml) {
        if (airSegmentListXml == null || airSegmentListXml.isEmpty()) {
            return "";
        }

        // Pattern to extract the first segment key
        Pattern pattern = Pattern.compile("<air:AirSegment Key=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(airSegmentListXml);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

}
