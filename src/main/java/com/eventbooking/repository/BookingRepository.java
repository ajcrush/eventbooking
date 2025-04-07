package com.eventbooking.repository;

import com.eventbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByRazorpayOrderId(String orderId);
}
