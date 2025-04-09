// src/main/java/com/eventbooking/controller/EventController.java
package com.eventbooking.controller;

import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import com.eventbooking.service.CloudinaryService;
import com.eventbooking.service.EventService;
import com.eventbooking.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public EventController(EventService eventService,
                           UserService userService,
                           CloudinaryService cloudinaryService) {
        this.eventService = eventService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("ADMIN")
    public ResponseEntity<Event> createEvent(
            @RequestPart("event") String eventJson,
            @RequestPart("poster") MultipartFile poster,
            Principal principal) {

        System.out.println("📥 Raw JSON: " + eventJson);

        Event event;
        try {
            event = objectMapper.readValue(eventJson, Event.class);
        } catch (Exception e) {
            System.out.println("❌ Failed to parse event JSON: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        User user = userService.getUserFromPrincipal(principal);

        try {
            String imageUrl = cloudinaryService.uploadFile(poster);
            event.setPoster(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        Event saved = eventService.createEvent(event, user);
        return ResponseEntity.ok(saved);
    }




    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("ADMIN")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                             @RequestPart("event") String eventJson,  // Receive event as JSON string
                                             @RequestPart(value = "poster", required = false) MultipartFile poster) throws IOException {

        // Deserialize the event JSON string into the Event object
        ObjectMapper objectMapper = new ObjectMapper();
        Event updatedEvent = objectMapper.readValue(eventJson, Event.class);

        // If a new poster is uploaded, upload it and set the URL
        if (poster != null && !poster.isEmpty()) {
            String newPosterUrl = cloudinaryService.uploadFile(poster);
            updatedEvent.setPoster(newPosterUrl);
        }

        // Call the service layer to update the event
        Event updated = eventService.updateEvent(id, updatedEvent);

        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully.");
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Event>> getEventsByUser(Principal principal) {
        User user = userService.getUserFromPrincipal(principal);
        return ResponseEntity.ok(eventService.getEventsByUser(user));
    }
}
