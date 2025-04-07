// src/main/java/com/eventbooking/service/impl/EventServiceImpl.java
package com.eventbooking.service.impl;

import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.service.EventService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepo;

    public EventServiceImpl(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Override
    public Event createEvent(Event event, User user) {
        event.setCreatedBy(user);
        if (event.getSeats() != null) {
            // initialize seatsLeft to the total seats
            event.setSeatsLeft(event.getSeats());
        }
        return eventRepo.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event updatedEvent) {
        Event existing = eventRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        // preserve id and creator
        updatedEvent.setId(existing.getId());
        updatedEvent.setCreatedBy(existing.getCreatedBy());
        // you may also adjust seatsLeft here if seats changed
        return eventRepo.save(updatedEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepo.existsById(id)) {
            throw new EntityNotFoundException("Event not found");
        }
        eventRepo.deleteById(id);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }

    @Override
    public List<Event> getEventsByUser(User user) {
        return eventRepo.findByCreatedBy(user);
    }
}
