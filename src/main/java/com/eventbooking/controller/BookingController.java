package com.eventbooking.controller;

import com.eventbooking.model.*;
import com.eventbooking.repository.*;
import com.eventbooking.service.RazorpayService;
import com.eventbooking.payload.RazorpayOrderResponse;
import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestParam Long eventId,
                                           @RequestParam int seats,
                                           Principal principal) throws Exception {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();

        if (event.getSeatsLeft() < seats) {
            return ResponseEntity.badRequest().body("Not enough seats available");
        }

        int amount = (int) (event.getPrice() * seats);
        Order order = razorpayService.createOrder(amount);

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setSeatsBooked(seats);
        booking.setRazorpayOrderId(order.get("id"));
        booking.setStatus("CREATED");
        booking.setCreatedAt(LocalDateTime.now());

        event.setSeatsLeft(event.getSeatsLeft() - seats);
        eventRepository.save(event);
        bookingRepository.save(booking);

        RazorpayOrderResponse response = new RazorpayOrderResponse();
        response.setOrderId(order.get("id"));
        response.setAmount(order.get("amount"));
        response.setCurrency(order.get("currency"));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/success")
    public ResponseEntity<?> paymentSuccess(@RequestParam String razorpayOrderId,
                                            @RequestParam String paymentId,
                                            @RequestParam String signature) {
        boolean isValid = razorpayService.verifySignature(razorpayOrderId, paymentId, signature);

        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid payment signature");
        }

        Booking booking = bookingRepository.findByRazorpayOrderId(razorpayOrderId).orElseThrow();
        booking.setStatus("PAID");
        booking.setRazorpayPaymentId(paymentId);
        bookingRepository.save(booking);
        return ResponseEntity.ok("Payment successful and verified!");
    }

    @PostMapping("/fail")
    public ResponseEntity<?> paymentFail(@RequestParam String razorpayOrderId) {

        System.out.println(razorpayOrderId);
        Booking booking = bookingRepository.findByRazorpayOrderId(razorpayOrderId).orElseThrow();

        // Release the seats
        Event event = booking.getEvent();
        event.setSeatsLeft(event.getSeatsLeft() + booking.getSeatsBooked());
        eventRepository.save(event);

        booking.setStatus("FAILED");
        bookingRepository.save(booking);

        return ResponseEntity.ok("Payment failed and seats released");
    }
}
