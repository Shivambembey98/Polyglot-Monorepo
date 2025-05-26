package com.ktt.service;

import com.ktt.dto.*;
import com.ktt.entities.*;
import com.ktt.repository.*;
import com.ktt.util.BookingServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingManagementService {

    private final BookingRepository bookingRepository;
    private final BookingServiceRepository bookingServiceRepository;
    private final BookingGuestRepository bookingGuestRepository;

    // BookingService functions
    @Transactional
    public void createBookingService(BookingServiceDTO dto) {
        try {
            BookingServiceEntity service = new BookingServiceEntity();
            service.setFromOrigin(dto.getFromOrigin());
            service.setDestination(dto.getDestination());
            service.setTripType(dto.getTripType());
            service.setStartDatetime(dto.getStartDatetime());
            service.setEndDatetime(dto.getEndDatetime());
            service.setLuggageWeight(dto.getLuggageWeight());
            service.setTripFareAmount(dto.getTripFareAmount());

             bookingServiceRepository.save(service);
        } catch (DataAccessException e) {
            throw new BookingServiceException("Database error while creating booking service", e);
        }
    }

    public BookingServiceEntity getBookingServiceById(Integer id) {
        return bookingServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookingService not found with id: " + id));
    }

    public List<BookingServiceEntity> getAllBookingServices() {
        return bookingServiceRepository.findAll();
    }

    // BookingGuest functions
    @Transactional
    public BookingGuest createBookingGuest(BookingGuestDTO dto) {
        try {
            BookingGuest guest = new BookingGuest();
            guest.setBookingSerId(dto.getBookingSerId());
            guest.setFirstName(dto.getFirstName());
            guest.setLastName(dto.getLastName());
            guest.setTitle(dto.getTitle());
            guest.setGender(dto.getGender());
            guest.setTravelerType(dto.getTravelerType());
            guest.setPhoneNumber(dto.getPhoneNumber());
            guest.setEmailAddress(dto.getEmailAddress());
            guest.setDob(dto.getDob());
            guest.setAddress(dto.getAddress());
            guest.setIdentificationId(dto.getIdentificationId());

            return bookingGuestRepository.save(guest);
        } catch (DataAccessException e) {
            throw new BookingServiceException("Database error while creating booking service", e);
        }
    }

    public BookingGuest getBookingGuestById(Long id) {
        return bookingGuestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookingGuest not found with id: " + id));
    }

    public List<BookingGuest> getGuestsByServiceId(Integer serviceId) {
        return bookingGuestRepository.findByBookingSerId(serviceId);
    }

    // Bookings functions
    @Transactional
    public Bookings createBooking(BookingsDTO dto) {
        try {
            Bookings booking = new Bookings();
            booking.setProvider(dto.getProvider());
            booking.setTicketNumber(dto.getTicketNumber());
            booking.setCurrency(dto.getCurrency());
            booking.setFareAmount(dto.getFareAmount());
            booking.setBookingStatus(dto.getBookingStatus());
            booking.setPaymentType(dto.getPaymentType());
            booking.setPnrNo(dto.getPnrNo());
            booking.setPaymentTransactionId(dto.getPaymentTransactionId());
            booking.setGuestId(dto.getGuestId());
            booking.setServiceId(dto.getServiceId());

            return bookingRepository.save(booking);
        } catch (DataAccessException e) {
            throw new BookingServiceException("Database error while creating booking service", e);
        }
    }

    public Bookings getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Bookings> getBookingsByServiceId(Integer serviceId) {
        return bookingRepository.findByServiceId(serviceId);
    }

    public List<Bookings> getBookingsByGuestId(Long guestId) {
        return bookingRepository.findByGuestId(guestId);
    }
}