package com.ktt.controller;

import com.ktt.entities.AirportList;
import com.ktt.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    BookingService bookingService;

    @GetMapping("/search")
    public ResponseEntity<List<AirportList>> searchAirports(@RequestParam(required = false) String query) {
        logger.info("Received airport search request with query: {}", query);

        try {
            List<AirportList> results = bookingService.searchAirports(query);
            logger.debug("Found {} airports matching query '{}'", results.size(), query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error searching airports for query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
