package com.ktt.repository;

import com.ktt.entities.BookingGuest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingGuestRepository extends JpaRepository<BookingGuest,Long> {
    List<BookingGuest> findByBookingSerId(Integer serviceId);
}
