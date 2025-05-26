package com.ktt.service;

import com.ktt.entities.AirportList;
import com.ktt.repository.AirportListRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final AirportListRepository airportListRepo;

    public BookingService(AirportListRepository airportListRepo) {
        this.airportListRepo = airportListRepo;
    }

    public List<AirportList> getAllAirports() {
        return airportListRepo.findAll();
    }

    public List<AirportList> searchAirports(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return airportListRepo.findAll(); // Fetch all airports if no search term is provided
        }

        searchTerm = searchTerm.trim(); // Trim spaces from search term

        List<AirportList> airports = new ArrayList<>();

        if (searchTerm.length() <= 3) {
            // Search based on IATA Code if the search term is 3 characters or less
            airports = airportListRepo.searchByIataCode(searchTerm.toUpperCase());

            // If no results found, fall back to Name or City search
            if (airports.isEmpty()) {
                airports = airportListRepo.searchAirports(searchTerm);
            }
        } else {
            // Search based on Name or City if the search term is more than 3 characters
            airports = airportListRepo.searchAirports(searchTerm);
        }

        return airports;
    }



//    String searchTermLower = searchTerm.toLowerCase().trim();
//
//        return airportListRepo.findAll().stream()
//                .filter(airport -> matchesSearchCriteria(airport, searchTermLower))
//                .collect(Collectors.toList());
//    }
//
//    private boolean matchesSearchCriteria(AirportList airport, String searchTerm) {
//        return (airport.getIataCode() != null && airport.getIataCode().toUpperCase().contains(searchTerm)) ||
//                (airport.getAirportName() != null && airport.getAirportName().toLowerCase().contains(searchTerm)) ||
//                (airport.getAirportCity() != null && airport.getAirportCity().toLowerCase().contains(searchTerm));
//    }
}
