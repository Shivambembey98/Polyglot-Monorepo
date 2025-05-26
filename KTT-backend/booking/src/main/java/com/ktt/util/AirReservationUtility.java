package com.ktt.util;

import com.ktt.config.AirLowFairCacheStore;
import com.ktt.config.AirPriceCacheStore;
import com.ktt.dto.AirCreateReservationDto;
import com.ktt.repository.ReservationRepository;
import com.ktt.repository.TravelerRepository;
import com.ktt.requestBuilder.AirCreateReservationBuilder;
import com.ktt.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AirReservationUtility {
    private static final Logger logger = LoggerFactory.getLogger(AirReservationUtility.class);

    private final AirCreateReservationBuilder airCreateReservationBuilder;
    private final AirPriceCacheStore airPriceCacheStore;
    private final AirLowFairCacheStore airLowFairCacheStore;
    private final SearchService searchService;
    private final ReservationRepository reservationRepository;
    private final TravelerRepository travelerRepository;
  //  private final BookingGuestRepository  bookingGuestRepository;

    public AirReservationUtility(AirCreateReservationBuilder airCreateReservationBuilder,
                                 AirPriceCacheStore airPriceCacheStore,
                                 AirLowFairCacheStore airLowFairCacheStore,
                                 SearchService searchService,
                                 ReservationRepository reservationRepository,
                                 TravelerRepository travelerRepository) {
        this.airCreateReservationBuilder = airCreateReservationBuilder;
        this.airPriceCacheStore = airPriceCacheStore;
        this.airLowFairCacheStore = airLowFairCacheStore;
        this.searchService = searchService;
        this.reservationRepository = reservationRepository;
        this.travelerRepository = travelerRepository;
    }

//    public Mono<String> createAndSaveReservation(AirCreateReservationDto dto) {
//        return validateRequest(dto)
//                .flatMap(this::buildAndExecuteReservationRequest)
//                .flatMap(response -> saveReservationData(dto, response))
//                .onErrorResume(e -> {
//                    logger.error("Error creating reservation: {}", e.getMessage());
//                    return Mono.error(e);
//                });
//    }

    private Mono<AirCreateReservationDto> validateRequest(AirCreateReservationDto dto) {
        if (dto == null) {
            return Mono.error(new IllegalArgumentException("Reservation data cannot be null"));
        }
        if (dto.getSessionIdAirPrice() == null || dto.getSessionIdLfs() == null) {
            return Mono.error(new IllegalArgumentException("Session IDs must be provided"));
        }
        if (dto.getTravelDtoList() == null || dto.getTravelDtoList().isEmpty()) {
            return Mono.error(new IllegalArgumentException("At least one traveler must be provided"));
        }
        return Mono.just(dto);
    }

    private Mono<String> buildAndExecuteReservationRequest(AirCreateReservationDto dto) {
        return Mono.fromSupplier(() -> {
            String cachedXml = airPriceCacheStore.get(dto.getSessionIdAirPrice());
            String cachedLfsXml = airLowFairCacheStore.get(dto.getSessionIdLfs());

            if (cachedXml == null || cachedLfsXml == null) {
                throw new IllegalStateException("Cached data not found for the provided session IDs");
            }

            String airSegmentListXml = extractElementXml(cachedXml, "air:AirSegment");
            String airPricingSolutionXml = extractOpeningTag(cachedXml, "air:AirPricingSolution");
            List<String> airPricingInfoXml = extractAllAirPricingInfo(cachedXml);
            String fairNoteXml = extractFareNotesAndHostTokens(cachedXml);
            String passengerDetails = extractPassengerAndPricingXml(cachedLfsXml);


            if (airSegmentListXml == null || airPricingSolutionXml == null) {
                throw new IllegalStateException("Required XML segments not found in cached data");
            }

            return airCreateReservationBuilder.buildAirCreateReservationRequest(
                    dto, airPricingSolutionXml, airSegmentListXml,
                    airPricingInfoXml, fairNoteXml, passengerDetails
            );
        }).flatMap(requestXml -> {
            logger.info("Generated Reservation Request XML:\n{}", requestXml); // Optional: log it also
            return Mono.just(requestXml); // Return the XML directly for testing
        });
    }



//                .flatMap(requestXml ->
//                searchService.streamSearchResponse(requestXml)
//                        .collectList()
//                        .map(responseList -> String.join("", responseList))
//                        .doOnSuccess(response -> logger.debug("Reservation request successful"))
//                        .doOnError(e -> logger.error("Error in reservation request: {}", e.getMessage())));
//    }

//    private Mono<String> saveReservationData(AirCreateReservationDto dto, String response) {
//        return Mono.fromCallable(() -> {
//            String reservationCode = generateReservationCode();
//            Reservation reservation = createReservationEntity(dto, reservationCode);
//            Reservation savedReservation = reservationRepository.save(reservation);
//
//            List<Traveler> travelers = createTravelerEntities(dto.getTravelDtoList(), savedReservation);
//            travelerRepository.saveAll(travelers);
//
//            logger.info("Reservation {} saved successfully with {} travelers",
//                    reservationCode, travelers.size());
//            return response;
//        }).onErrorResume(e -> {
//            logger.error("Error saving reservation data: {}", e.getMessage());
//            return Mono.error(new RuntimeException("Failed to save reservation data", e));
//        });
//    }

//    private void createReservationEntity(AirCreateReservationDto dto, String reservationCode) {
//        BookingService bookingService = new BookingService();
//        bookingService.setReservationCode(reservationCode);
//        bookingService.setStreet(dto.getStreet());
//        bookingService.setCity(dto.getCity());
//        bookingService.setState(dto.getState());
//        bookingService.setPostalCode(dto.getPostalCode());
//        bookingService.setCountry(dto.getCountry());
//        bookingService.setAirline(dto.getAirLine());
//        bookingService.setSessionIdAirPrice(dto.getSessionIdAirPrice());
//        bookingService.setSessionIdLfs(dto.getSessionIdLfs());
//
//    }
  /// / use this function and update as per requirement
//    @Transactional
//    private void createTravelerEntities(List<TravelDto> travelDtos) {
//            List<BookingGuest>  traveler1 = new ArrayList<>();
//            for(TravelDto travelDto : travelDtos) {
//                BookingGuest traveler = new BookingGuest();
//                traveler.setFirstName(travelDto.getFirstName());
//                traveler.setLastName(travelDto.getLastName());
//                traveler.setTitle(travelDto.getPrefix());
//                traveler.setGender(travelDto.getGender());
//                traveler.setTravelerType(travelDto.getTravelerType());
//                traveler.setPhoneNumber(travelDto.getPhoneNumber());
//                traveler.setEmailAddress(travelDto.getEmail());
//                traveler.setIdentificationId(travelDto.getDocText());
//                traveler.setDob(travelDto.getDob());
//                traveler1.add(traveler);
//            }
//        bookingGuestRepository.saveAll(traveler1);
//    }
//@Transactional
//public void createReservation(AirCreateReservationDto dto, String reservationCode) {
//
//    for (TravelDto travelDto : dto.getTravelDtoList()) {
//
//        // 1️⃣ Save BookingGuest first
//        BookingGuest traveler = new BookingGuest();
//        traveler.setFirstName(travelDto.getFirstName());
//        traveler.setLastName(travelDto.getLastName());
//        traveler.setTitle(travelDto.getPrefix());
//        traveler.setGender(travelDto.getGender());
//        traveler.setTravelerType(travelDto.getTravelerType());
//        traveler.setPhoneNumber(travelDto.getPhoneNumber());
//        traveler.setEmailAddress(travelDto.getEmail());
//        traveler.setIdentificationId(travelDto.getDocText());
//        traveler.setDob(travelDto.getDob());
//        traveler.setCreationDate(LocalDateTime.now());
//        traveler.setUpdateDate(LocalDateTime.now());
//
//        // Save and get generated guestId
//        BookingGuest savedGuest = bookingGuestRepository.save(traveler);
//
//        // 🔥 2️⃣ Now save BookingService with guest reference
//        BookingService bookingService = new BookingService();
//        bookingService.setReservationCode(reservationCode);
//        bookingService.setStreet(dto.getStreet());
//        bookingService.setCity(dto.getCity());
//        bookingService.setState(dto.getState());
//        bookingService.setPostalCode(dto.getPostalCode());
//        bookingService.setCountry(dto.getCountry());
//        bookingService.setAirline(dto.getAirLine());
//        bookingService.setSessionIdAirPrice(dto.getSessionIdAirPrice());
//        bookingService.setSessionIdLfs(dto.getSessionIdLfs());
//        bookingService.setCreationDate(LocalDateTime.now());
//        bookingService.setUpdateDate(LocalDateTime.now());
//
//        // ✅ Link saved guest
//        bookingService.setGuest(savedGuest);
//
//        bookingServiceRepository.save(bookingService);
//    }
//}

    private String generateReservationCode() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // XML extraction methods with improved null safety
    private String extractElementXml(String xml, String tagName) {
        if (xml == null || tagName == null) return "";

        try {
            String escapedTag = tagName.replace(":", "\\:");
            String regex = "<" + escapedTag + "\\b[^>]*?(/>|>.*?</" + escapedTag + ">)";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            return matcher.find() ? matcher.group() : "";
        } catch (Exception e) {
            logger.warn("Error extracting XML element {}: {}", tagName, e.getMessage());
            return "";
        }
    }

    private String extractOpeningTag(String xml, String tagName) {
        if (xml == null || tagName == null) return null;

        try {
            String escapedTag = tagName.replace(":", "\\:");
            Pattern pattern = Pattern.compile("<" + escapedTag + "[^>]*>");
            Matcher matcher = pattern.matcher(xml);
            return matcher.find() ? matcher.group() : null;
        } catch (Exception e) {
            logger.warn("Error extracting opening tag {}: {}", tagName, e.getMessage());
            return null;
        }
    }
//
//    private String extractAllAirPricingInfo(String xml) {
//        if (xml == null) return "";
//
//        try {
//            String tagName = "air:AirPricingInfo";
//            String escapedTag = Pattern.quote(tagName);
//            String regex = "<" + escapedTag + "\\b[^>]*?>.*?</" + escapedTag + ">";
//            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
//            Matcher matcher = pattern.matcher(xml);
//
//            StringBuilder result = new StringBuilder();
//            while (matcher.find()) {
//                result.append(matcher.group());
//            }
//            return result.toString();
//        } catch (Exception e) {
//            logger.warn("Error extracting air pricing info: {}", e.getMessage());
//            return "";
//        }
//    }

    private String extractFareNotesAndHostTokens(String xml) {
        if (xml == null) return "";

        try {
            String pricingSolutionTag = "air:AirPricingSolution";
            String pricingRegex = "<" + Pattern.quote(pricingSolutionTag) + "\\b[^>]*?>.*?</" + Pattern.quote(pricingSolutionTag) + ">";
            Pattern pricingPattern = Pattern.compile(pricingRegex, Pattern.DOTALL);
            Matcher pricingMatcher = pricingPattern.matcher(xml);

            if (!pricingMatcher.find()) return "";
            String pricingBlock = pricingMatcher.group();

            StringBuilder result = new StringBuilder();

            String fareNoteRegex = "<" + Pattern.quote("air:FareNote") + "\\b[^>]*?>.*?</" + Pattern.quote("air:FareNote") + ">";
            Pattern fareNotePattern = Pattern.compile(fareNoteRegex, Pattern.DOTALL);
            Matcher fareNoteMatcher = fareNotePattern.matcher(pricingBlock);
            while (fareNoteMatcher.find()) {
                result.append(fareNoteMatcher.group()).append("\n");
            }

            String hostTokenRegex = "<" + Pattern.quote("common_v52_0:HostToken") + "\\b[^>]*?>.*?</" + Pattern.quote("common_v52_0:HostToken") + ">";
            Pattern hostTokenPattern = Pattern.compile(hostTokenRegex, Pattern.DOTALL);
            Matcher hostTokenMatcher = hostTokenPattern.matcher(pricingBlock);
            while (hostTokenMatcher.find()) {
                result.append(hostTokenMatcher.group()).append("\n");
            }

            return result.toString().trim();
        } catch (Exception e) {
            logger.warn("Error extracting fare notes and host tokens: {}", e.getMessage());
            return "";
        }
    }

    private String extractPassengerAndPricingXml(String xml) {
        if (xml == null) return "";

        try {
            StringBuilder extracted = new StringBuilder();
            Pattern pricingModifiersPattern = Pattern.compile("<air:AirPricingModifiers[^>]*>\\s*</air:AirPricingModifiers>");
            Matcher pricingMatcher = pricingModifiersPattern.matcher(xml);
            if (pricingMatcher.find()) {
                extracted.append(pricingMatcher.group()).append("\n");
            }

            Pattern passengerPattern = Pattern.compile("<com:SearchPassenger[^>]*/>");
            Matcher passengerMatcher = passengerPattern.matcher(xml);
            while (passengerMatcher.find()) {
                extracted.append(passengerMatcher.group()).append("\n");
            }
            return extracted.toString();
        } catch (Exception e) {
            logger.warn("Error extracting passenger and pricing XML: {}", e.getMessage());
            return "";
        }
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