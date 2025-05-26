package com.ktt.repository;

import com.ktt.entities.AirportList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportListRepository extends JpaRepository<AirportList,Long> {

    @Query("SELECT a FROM AirportList a WHERE UPPER(a.iataCode) = UPPER(:searchTerm) ORDER BY a.iataCode")
    List<AirportList> searchByIataCode(@Param("searchTerm") String searchTerm);

    // Search based on airport name or city if the search term is more than 3 characters
    @Query("SELECT a FROM AirportList a WHERE " +
            "(LOWER(a.airportCity) LIKE LOWER(CONCAT(:searchTerm, '%')) OR " +  // Match based on city starting with the searchTerm
            "LOWER(a.airportName) LIKE LOWER(CONCAT(:searchTerm, '%'))) " +  // Match based on name starting with the searchTerm
            "ORDER BY " +
            "CASE " +
            "  WHEN LOWER(a.airportCity) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 1 " +  // Priority 1: City starting with searchTerm
            "  WHEN LOWER(a.airportName) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2 " +  // Priority 2: Name starting with searchTerm
            "  ELSE 3 " +
            "END")
    List<AirportList> searchAirports(@Param("searchTerm") String searchTerm);

}
