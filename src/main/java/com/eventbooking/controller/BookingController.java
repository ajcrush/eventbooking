package com.eventbooking.controller;

import com.eventbooking.dto.BookingDTO;
import com.eventbooking.model.*;
import com.eventbooking.repository.*;
import com.eventbooking.service.RazorpayService;
import com.eventbooking.payload.RazorpayOrderResponse;
import com.razorpay.Order;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

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
    @Autowired
    private JavaMailSender javaMailSender;

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
        Order order = razorpayService.createOrder(amount*100);

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
    public ResponseEntity<?> paymentSuccess(@RequestParam(required = false) String razorpayOrderId,
                                            @RequestParam(required = false) String paymentId,
                                            @RequestParam(required = false) String signature) {

        if (razorpayOrderId == null || paymentId == null || signature == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        boolean isValid = razorpayService.verifySignature(razorpayOrderId, paymentId, signature);

        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid payment signature");
        }

        Booking booking = bookingRepository.findByRazorpayOrderId(razorpayOrderId).orElseThrow();
        booking.setStatus("PAID");
        booking.setRazorpayPaymentId(paymentId);
        bookingRepository.save(booking);

        // Send email
        String userEmail = booking.getUser().getEmail();

        try {
            sendTicketEmailWithQR(userEmail, booking);
        } catch (MessagingException | IOException | WriterException e) {
            e.printStackTrace(); // or log it
            return ResponseEntity.status(500).body("Payment saved, but failed to send ticket email");
        }

        return ResponseEntity.ok("Payment successful, verified, and ticket sent via email!");
    }


    public void sendTicketEmailWithQR(String toEmail, Booking booking) throws MessagingException, IOException, WriterException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Your Event Ticket - Booking Confirmed");

        // Email content
        String content = String.format(
                "Hi %s,\n\nYour booking for the event '%s' is confirmed!\n\nDetails:\nEvent: %s\nDate: %s\nSeats: %d\n\nBooking ID: %s\nPayment ID: %s\n\nYour ticket QR code is attached. Thank you for booking with us!",
                booking.getUser().getName(),
                booking.getEvent().getName(),
                booking.getEvent().getName(),
                booking.getEvent().getDate().toString(),
                booking.getSeatsBooked(),
                booking.getId(),
                booking.getRazorpayPaymentId()
        );
        helper.setText(content);

        // JSON content for QR
        String qrContent = String.format(
                "{ \"name\": \"%s\", \"event\": \"%s\", \"date\": \"%s\", \"seats\": %d, \"bookingId\": \"%s\", \"paymentId\": \"%s\" }",
                booking.getUser().getName(),
                booking.getEvent().getName(),
                booking.getEvent().getDate().toString(),
                booking.getSeatsBooked(),
                booking.getId(),
                booking.getRazorpayPaymentId()
        );

        byte[] qrImage = generateQRCodeImage(qrContent, 250, 250);

        ByteArrayResource qrAttachment = new ByteArrayResource(qrImage);
        helper.addAttachment("booking-qr.png", qrAttachment, "image/png");

        javaMailSender.send(message);
    }


    private byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
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

    @GetMapping("/my-booked-events")
    public ResponseEntity<?> getBookedEventsForUser(Principal principal) {
        try {
            System.out.println("Fetching booked events for user: " + principal.getName());

            // Fetch user by email
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("User found: " + user.getEmail());

            // Fetch bookings for the user
            List<Booking> bookings = bookingRepository.findByUser(user);
            System.out.println("Bookings found for user: " + bookings.size());

            // Check if bookings list is empty and return appropriate response
            if (bookings.isEmpty()) {
                System.out.println("No bookings found for user.");
                return ResponseEntity.noContent().build();
            } else {
                // Returning all booking details along with event data
                System.out.println("Returning all booked tickets for user.");

                // Mapping bookings to a DTO (Data Transfer Object) or directly returning Booking objects if needed
                List<BookingDTO> bookingDTOs = bookings.stream().map(booking -> {
                    // Create a DTO with required information
                    BookingDTO dto = new BookingDTO();
                    dto.setEvent(booking.getEvent());  // Assuming Event class has necessary details
                    dto.setSeatsBooked(booking.getSeatsBooked());
                    dto.setStatus(booking.getStatus());
                    dto.setCreatedAt(booking.getCreatedAt());
                    dto.setRazorpayOrderId(booking.getRazorpayOrderId());
                    dto.setRazorpayPaymentId(booking.getRazorpayPaymentId());
                    return dto;
                }).collect(Collectors.toList());

                return ResponseEntity.ok(bookingDTOs);
            }
        } catch (Exception e) {
            System.out.println("Error occurred while fetching booked events: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching your booked events.");
        }
    }



}