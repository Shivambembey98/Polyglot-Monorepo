package com.ktt.repository;

import com.ktt.entities.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Bookings,Integer> {
    List<Bookings> findByServiceId(Integer serviceId);
    List<Bookings> findByGuestId(Long guestId);
}
