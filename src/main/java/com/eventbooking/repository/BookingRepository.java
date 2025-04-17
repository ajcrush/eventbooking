package com.eventbooking.repository;

import com.eventbooking.model.Booking;
import com.eventbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByRazorpayOrderId(String orderId);
    List<Booking> findByUser(User user);
}