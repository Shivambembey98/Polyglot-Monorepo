package com.ktt.controller;

import com.ktt.config.AirLowFairCacheStore;
import com.ktt.config.AirPriceCacheStore;
import com.ktt.dto.AirCreateReservationDto;
import com.ktt.dto.AirPriceRequestDto;
import com.ktt.dto.SearchDto;
import com.ktt.dto.SearchPassengerRef;
import com.ktt.requestBuilder.*;

import com.ktt.service.SearchService;
import com.ktt.util.AirReservationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@RequestMapping("/booking")
@RestController
@CrossOrigin("*")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    AirPriceCacheStore airPriceCacheStore;

    @Autowired
    AirLowFairCacheStore airLowFairCacheStore;
//	@Autowired
//	BookingTravelerRefUtil  bookingTravelerRefUtil;


    private final AirReservationUtility airReservationUtility;

    private final SearchService searchService;
    private final LfsRequestBuilder lfsRequestBuilder;
    private final AirPriceRequestBuilder airPriceRequestBuilder;
    private final AirRetrieveRequestBuilder retriveRequestBuilder;
    private final AirTicketRequestBuilder airTicketRequestBuilder;
    private final AirCreateReservationBuilder airCreateReservationBuilder;
    private final SeatMapRequestBuilder seatMapRequestBuilder;


    @Autowired
    public SearchController(SearchService searchService, LfsRequestBuilder lfsRequestBuilder, AirPriceRequestBuilder airPriceRequestBuilder, AirRetrieveRequestBuilder retriveRequestBuilder, AirTicketRequestBuilder airTicketRequestBuilder, AirCreateReservationBuilder airCreateReservationBuilder, SeatMapRequestBuilder seatMapRequestBuilder, AirReservationUtility airReservationUtility) {
        this.searchService = searchService;
        this.lfsRequestBuilder = lfsRequestBuilder;
        this.airPriceRequestBuilder = airPriceRequestBuilder;
        this.retriveRequestBuilder = retriveRequestBuilder;
        this.airTicketRequestBuilder = airTicketRequestBuilder;
        this.airCreateReservationBuilder = airCreateReservationBuilder;
        this.seatMapRequestBuilder=seatMapRequestBuilder;
        this.airReservationUtility=airReservationUtility;

    }

    @PostMapping(value = "/low-fare-search", produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> getAirData(@RequestBody SearchDto searchDto) {
        logger.info("Received low fare search request: {}", searchDto);
        String soapRequestXml = lfsRequestBuilder.buildSoapRequestSearch(searchDto);
        String sessionId = UUID.randomUUID().toString();

        logger.debug("Generated SOAP Request XML: {}", soapRequestXml);

        return searchService.streamSearchResponse(soapRequestXml)
                .collectList()
                .map(responseList -> {
                    logger.info("Received {} responses for low fare search", responseList.size());

                    List<String> sortedResponses = responseList.stream()
                            .sorted(Comparator.comparingDouble(this::extractLowestTotalPriceFromResponse))
                            .collect(Collectors.toList());

                    StringBuilder xmlResponse = new StringBuilder();
                    xmlResponse.append("<LowFareSearchResponse>");
                    xmlResponse.append("<SessionIdLfs>").append(sessionId).append("</SessionIdLfs>");

                    for (String response : sortedResponses) {
                        xmlResponse.append(response);
                    }
                    xmlResponse.append("</LowFareSearchResponse>");

                    // Cache and log
                    String extractedXmlPart = extractPassengerAndPricingXml(soapRequestXml);
                    airLowFairCacheStore.store(sessionId, extractedXmlPart);
                    logger.debug("Cached extracted XML for sessionId: {}", sessionId);

                    return xmlResponse.toString();
                });
    }

    @CrossOrigin()
    @PostMapping(value = "/air-price", produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> getAirPrice(@RequestBody AirPriceRequestDto searchDto) {
        logger.info("Received air price request with sessionIdLfs: {}", searchDto.getSessionId());

        String sessionIdLfs = searchDto.getSessionId();
        String cachedXml = airLowFairCacheStore.get(sessionIdLfs);

        if (cachedXml == null) {
            logger.warn("No cached LFS XML found for sessionIdLfs: {}", sessionIdLfs);
        } else {
            logger.debug("Retrieved cached LFS XML for sessionIdLfs: {}", sessionIdLfs);
        }

        String soapRequestXml = airPriceRequestBuilder.buildAirPriceRequest(searchDto, cachedXml);
        logger.debug("Constructed AirPrice SOAP request for sessionIdLfs: {}", sessionIdLfs);

        String sessionIdAirPrice = UUID.randomUUID().toString(); // 🔑 Generate sessionId here
        logger.info("Generated new sessionIdAirPrice: {}", sessionIdAirPrice);

        return searchService.streamSearchResponse(soapRequestXml)
                .collectList()
                .map(responseList -> {
                    logger.info("Received {} response segment(s) for air price request", responseList.size());

                    StringBuilder xmlResponse = new StringBuilder();
                    xmlResponse.append("<AirPriceResponse>");
                    xmlResponse.append("<sessionIdAirPrice>").append(sessionIdAirPrice).append("</sessionIdAirPrice>");
                    xmlResponse.append("<SessionIdLfs>").append(sessionIdLfs).append("</SessionIdLfs>");

                    for (String response : responseList) {
                        xmlResponse.append(response);
                    }
                    xmlResponse.append("</AirPriceResponse>");

                    String fullXml = xmlResponse.toString();

                    // Extract and cache for reuse
                    String airPricingSolutionXml = extractFirstAirPricingSolution(fullXml);
                    String airSegmentListXml = extractElementXml(fullXml, "air:AirItinerary");
                    String cacheXml = "<cache>" + airSegmentListXml + airPricingSolutionXml + "</cache>";

                    airPriceCacheStore.store(sessionIdAirPrice, cacheXml);
                    logger.debug("Stored AirPrice cache for sessionIdAirPrice: {}", sessionIdAirPrice);

                    return fullXml;
                });
    }

    @CrossOrigin
    @PostMapping("/air-reservation")
    public Mono<String> createReservation(@RequestBody AirCreateReservationDto dto) {
        logger.info("Received reservation request for sessionIdAirPrice: {} and sessionIdLfs: {}", dto.getSessionIdAirPrice(), dto.getSessionIdLfs());

        String cachedXml = airPriceCacheStore.get(dto.getSessionIdAirPrice());
        String cachedLfsXml = airLowFairCacheStore.get(dto.getSessionIdLfs());

        if (cachedXml == null) {
            logger.warn("No cached AirPrice XML found for sessionIdAirPrice: {}", dto.getSessionIdAirPrice());
        }
        if (cachedLfsXml == null) {
            logger.warn("No cached LFS XML found for sessionIdLfs: {}", dto.getSessionIdLfs());
        }

        // Extract elements from cache
        String airSegmentListXml = extractElementXml(cachedXml, "air:AirSegment");
        String airPricingSolutionXml = extractOpeningTag(cachedXml, "air:AirPricingSolution");
        List<String> airPricingInfoXml = extractAllAirPricingInfo(cachedXml);
        String fairNoteXml = extractFareNotesAndHostTokens(cachedXml);
        String passengerDetails = extractPassengerAndPricingXml(cachedLfsXml);
        List<SearchPassengerRef> refs = extractPassengerRefs(passengerDetails);

        logger.debug("Extracted AirSegment count: {}", airSegmentListXml != null ? airSegmentListXml.length() : 0);
        logger.debug("Extracted AirPricingInfo size: {}", airPricingInfoXml.size());
        logger.debug("Extracted passenger reference count: {}", refs.size());

        // Build the SOAP request
        String requestXml = airCreateReservationBuilder.buildAirCreateReservationRequest(
                dto,
                airPricingSolutionXml,
                airSegmentListXml,
                airPricingInfoXml,
                fairNoteXml,
                passengerDetails
        );

        logger.debug("Built AirCreateReservation SOAP Request");

        return searchService.streamSearchResponse(requestXml)
                .collectList()
                .map(responseList -> {
                    logger.info("Received {} response segment(s) for reservation", responseList.size());

                    StringBuilder xmlResponse = new StringBuilder();
                    for (String response : responseList) {
                        xmlResponse.append(response);
                    }

                    // Optionally log a short version of the response
                    logger.debug("Reservation response starts with: {}", xmlResponse.substring(0, Math.min(200, xmlResponse.length())));

                    return xmlResponse.toString();
                });
    }

    @CrossOrigin()
    @PostMapping(value = "/retrieve-ticket", produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> retrieveTicket(@RequestParam String locatorCode, @RequestParam String providerCode) {
        logger.info("Received retrieve ticket request with locatorCode: {}, providerCode: {}", locatorCode, providerCode);

        String soapRequestXml = retriveRequestBuilder.buildRetrieveRequest(locatorCode, providerCode);
        logger.debug("Constructed SOAP RetrieveTicket request: {}", soapRequestXml);

        return searchService.AirRetrieveSearchResponse(soapRequestXml)
                .collectList()
                .map(responseList -> {
                    logger.info("Received {} response segment(s) for retrieve-ticket", responseList.size());

                    StringBuilder xmlResponse = new StringBuilder();
                    for (String response : responseList) {
                        xmlResponse.append(response);
                    }

                    logger.debug("RetrieveTicket response starts with: {}",
                            xmlResponse.substring(0, Math.min(200, xmlResponse.length())));

                    return xmlResponse.toString();
                });
    }


    @CrossOrigin()
    @PostMapping(value = "/air-ticket", produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> airTicket(@RequestParam String airReservationLocatorCode) {
        logger.info("Received air ticket request for locatorCode: {}", airReservationLocatorCode);

        String soapRequestXml = airTicketRequestBuilder.buildAirTicketRequest(airReservationLocatorCode);
        logger.debug("Constructed AirTicket SOAP request: {}", soapRequestXml);

        return searchService.streamSearchResponse(soapRequestXml)
                .collectList()
                .map(responseList -> {
                    logger.info("Received {} response segment(s) for air-ticket", responseList.size());

                    StringBuilder xmlResponse = new StringBuilder();
                    for (String response : responseList) {
                        xmlResponse.append(response);
                    }

                    logger.debug("AirTicket response starts with: {}",
                            xmlResponse.substring(0, Math.min(200, xmlResponse.length())));

                    return xmlResponse.toString();
                });
    }


    @CrossOrigin()
    @PostMapping(value = "/seat-map", produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> getSeatMap(@RequestParam String sessionIdAirPrice) {
        logger.info("Received seat map request for sessionIdAirPrice: {}", sessionIdAirPrice);

        // Get cached XML from air price response
        String cachedXml = airPriceCacheStore.get(sessionIdAirPrice);
        if (cachedXml == null) {
            logger.warn("No cached XML found for sessionIdAirPrice: {}", sessionIdAirPrice);
        } else {
            logger.debug("Successfully retrieved cached XML for sessionIdAirPrice");
        }

        // Extract AirSegment from cached XML
        String airSegmentXml = extractElementXml(cachedXml, "air:AirSegment");
        logger.debug("Extracted AirSegment XML length: {}", airSegmentXml != null ? airSegmentXml.length() : 0);

        // Build seat map request
        String soapRequestXml = seatMapRequestBuilder.buildSeatMapRequest(airSegmentXml);
        logger.debug("Constructed SeatMap SOAP request: {}", soapRequestXml);

        // Call the service to get seat map response
        return searchService.streamSearchResponse(soapRequestXml)
                .collectList()
                .map(responseList -> {
                    logger.info("Received {} response segment(s) for seat-map", responseList.size());

                    StringBuilder xmlResponse = new StringBuilder();
                    xmlResponse.append("<SeatMapResponse>");

                    for (String response : responseList) {
                        xmlResponse.append(response);
                    }

                    xmlResponse.append("</SeatMapResponse>");

                    logger.debug("SeatMap response starts with: {}",
                            xmlResponse.substring(0, Math.min(200, xmlResponse.length())));

                    return xmlResponse.toString();
                });
    }



    private List<Double> extractTotalPrices(String responseXml) {
        List<Double> prices = new ArrayList<>();
        Pattern pattern = Pattern.compile("<air:AirPricePoint[^>]+TotalPrice=\"INR(\\d+\\.\\d+)\"");
        Matcher matcher = pattern.matcher(responseXml);

        while (matcher.find()) {
            try {
                prices.add(Double.parseDouble(matcher.group(1)));
            } catch (NumberFormatException e) {
                System.err.println("Invalid price format: " + matcher.group(1));
            }
        }
        return prices;
    }

    private double extractLowestTotalPriceFromResponse(String responseXml) {
        return extractTotalPrices(responseXml).stream()
                .min(Double::compare)
                .orElse(Double.MAX_VALUE);
    }


    public String extractElementXml(String xml, String tagName) {

        StringBuilder result = new StringBuilder();

        // Escape colon for regex if tag includes namespace like air:Tag
        String escapedTag = tagName.replace(":", "\\:");

        // Match both self-closing and full tags
        String regex = "<" + escapedTag + "\\b[^>]*?(/>|>.*?</" + escapedTag + ">)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);

        while (matcher.find()) {
            result.append(matcher.group());
        }

        return result.toString();
    }




    public String extractOpeningTag(String xml, String tagName) {
        // Escape colon for regex if tag includes namespace like air:Tag
        String escapedTag = tagName.replace(":", "\\:");

        // Regex to match the opening tag (non-greedy until the first '>')
        Pattern pattern = Pattern.compile("<" + escapedTag + "[^>]*>");
        Matcher matcher = pattern.matcher(xml);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractPassengerAndPricingXml(String xml) {
        StringBuilder extracted = new StringBuilder();

        // Extract <air:AirPricingModifiers>
        Pattern pricingModifiersPattern = Pattern.compile("<air:AirPricingModifiers[^>]*>\\s*</air:AirPricingModifiers>");
        Matcher pricingMatcher = pricingModifiersPattern.matcher(xml);
        if (pricingMatcher.find()) {
            extracted.append(pricingMatcher.group()).append("\n");
        }

        // Extract all <com:SearchPassenger ... />
        Pattern passengerPattern = Pattern.compile("<com:SearchPassenger[^>]*/>");
        Matcher passengerMatcher = passengerPattern.matcher(xml);
        while (passengerMatcher.find()) {
            extracted.append(passengerMatcher.group()).append("\n");
        }
        return extracted.toString().trim();
    }


    public static String extractFareSectionWithRegex(String xml) {
        Set<String> hostTokenRefs = new HashSet<>();

        // Step 1: Extract HostTokenRef values from BookingInfo
        Pattern refPattern = Pattern.compile("<air:BookingInfo[^>]*?HostTokenRef=\"([^\"]+)\"");
        Matcher refMatcher = refPattern.matcher(xml);
        while (refMatcher.find()) {
            hostTokenRefs.add(refMatcher.group(1));
        }

        // Step 2: Extract matching HostToken blocks
        Pattern hostTokenPattern = Pattern.compile(
                "<common_v52_0:HostToken[^>]*Key=\"([^\"]+)\"[^>]*>.*?</common_v52_0:HostToken>",
                Pattern.DOTALL
        );
        Matcher hostTokenMatcher = hostTokenPattern.matcher(xml);
        StringBuilder result = new StringBuilder();

        while (hostTokenMatcher.find()) {
            String key = hostTokenMatcher.group(1);
            if (hostTokenRefs.contains(key)) {
                result.append(hostTokenMatcher.group()).append("\n");
            }
        }

        return result.toString().trim();
    }




    public List<SearchPassengerRef> extractPassengerRefs(String passengerDetailsXml) {
        List<SearchPassengerRef> passengerRefs = new ArrayList<>();

        // This pattern captures 'Code="..."' and 'BookingTravelerRef="..."' in any order with any spacing/attributes in between
        Pattern pattern = Pattern.compile("Code\\s*=\\s*\"(\\w+)\"[^>]*?BookingTravelerRef\\s*=\\s*\"([^\"]+)\"");

        Matcher matcher = pattern.matcher(passengerDetailsXml);

        while (matcher.find()) {
            String code = matcher.group(1);
            String bookingTravelerRef = matcher.group(2);
            passengerRefs.add(new SearchPassengerRef(code, bookingTravelerRef));
        }

        return passengerRefs;
    }
    public static String extractFirstAirPricingSolution(String xml) {
        if (xml == null || xml.isEmpty()) return null;

        String startTag = "<air:AirPricingSolution";
        String endTag = "</air:AirPricingSolution>";

        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) return null;

        int endIndex = xml.indexOf(endTag, startIndex);
        if (endIndex == -1) return null;

        // Include the full end tag in the output
        endIndex += endTag.length();

        return xml.substring(startIndex, endIndex);
    }
//
//    public static String extractAllAirPricingInfo(String xml) {
//        if (xml == null || xml.isEmpty()) {
//            return "";
//        }
//
//        // Escape the tag properly
//        String tagName = "air:AirPricingInfo";
//        String escapedTag = Pattern.quote(tagName);
//
//        // Match the full <air:AirPricingInfo>...</air:AirPricingInfo> block (not self-closing)
//        String regex = "<" + escapedTag + "\\b[^>]*?>.*?</" + escapedTag + ">";
//        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
//        Matcher matcher = pattern.matcher(xml);
//
//        StringBuilder result = new StringBuilder();
//        while (matcher.find()) {
//            result.append(matcher.group());
//        }
//
//        return result.toString();
//    }

    public static String extractFareNotesAndHostTokens(String xml) {
        if (xml == null || xml.isEmpty()) return "";

        // Step 1: Extract the first <air:AirPricingSolution>...</air:AirPricingSolution>
        String pricingSolutionTag = "air:AirPricingSolution";
        String pricingRegex = "<" + Pattern.quote(pricingSolutionTag) + "\\b[^>]*?>.*?</" + Pattern.quote(pricingSolutionTag) + ">";
        Pattern pricingPattern = Pattern.compile(pricingRegex, Pattern.DOTALL);
        Matcher pricingMatcher = pricingPattern.matcher(xml);

        if (!pricingMatcher.find()) {
            return "";
        }

        String pricingBlock = pricingMatcher.group(); // Only first match

        // Step 2: Extract all <air:FareNote> elements inside the block
        String fareNoteRegex = "<" + Pattern.quote("air:FareNote") + "\\b[^>]*?>.*?</" + Pattern.quote("air:FareNote") + ">";
        Pattern fareNotePattern = Pattern.compile(fareNoteRegex, Pattern.DOTALL);
        Matcher fareNoteMatcher = fareNotePattern.matcher(pricingBlock);

        StringBuilder result = new StringBuilder();

        while (fareNoteMatcher.find()) {
            result.append(fareNoteMatcher.group()).append("\n");
        }

        // Step 3: Extract all <common_v52_0:HostToken> elements inside the block
        String hostTokenRegex = "<" + Pattern.quote("common_v52_0:HostToken") + "\\b[^>]*?>.*?</" + Pattern.quote("common_v52_0:HostToken") + ">";
        Pattern hostTokenPattern = Pattern.compile(hostTokenRegex, Pattern.DOTALL);
        Matcher hostTokenMatcher = hostTokenPattern.matcher(pricingBlock);

        while (hostTokenMatcher.find()) {
            result.append(hostTokenMatcher.group()).append("\n");
        }

        return result.toString().trim();
    }

    private List<String> extractAllAirPricingInfo(String xml) {
        List<String> airPricingBlocks = new ArrayList<>();

        if (xml == null) return airPricingBlocks;

        try {
            String tagStart = "<air:AirPricingInfo\\b[^>]*>";
            String tagEnd = "</air:FareCalc>";
            String regex = tagStart + ".*?" + tagEnd;

            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);

            while (matcher.find()) {
                airPricingBlocks.add(matcher.group());
            }
        } catch (Exception e) {
            logger.warn("Error extracting air pricing info: {}", e.getMessage());
        }

        return airPricingBlocks;
    }


}
