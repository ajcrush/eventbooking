package com.eventbooking.service;

import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import com.eventbooking.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepo;

    // Constructor injection
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

        // Preserve id and createdBy fields
        updatedEvent.setId(existing.getId());
        updatedEvent.setCreatedBy(existing.getCreatedBy());

        // Optionally adjust seatsLeft if seats have changed
        if (updatedEvent.getSeatsLeft() != existing.getSeatsLeft()) {
            // Logic to adjust seatsLeft (for example, ensure it doesn't go below 0)
            updatedEvent.setSeatsLeft(updatedEvent.getSeatsLeft());
        }

        // Save updated event
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
