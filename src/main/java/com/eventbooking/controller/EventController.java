// src/main/java/com/eventbooking/controller/EventController.java
package com.eventbooking.controller;

import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import com.eventbooking.service.EventService;
import com.eventbooking.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService,
                           UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @PostMapping
    @RolesAllowed("ADMIN")
    public ResponseEntity<Event> createEvent(@RequestBody Event event,
                                             Principal principal) {
        User user = userService.getUserFromPrincipal(principal);
        System.out.println("Creating event by user: " + user.getEmail());
        Event saved = eventService.createEvent(event, user);
        System.out.println("Saved event with ID: " + saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                             @RequestBody Event event) {
        return ResponseEntity.ok(eventService.updateEvent(id, event));
    }

    @DeleteMapping("/{id}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully.");
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        System.out.println("Found events: " + events.size());
        return ResponseEntity.ok(events);
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
