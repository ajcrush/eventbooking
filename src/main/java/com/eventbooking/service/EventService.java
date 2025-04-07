// src/main/java/com/eventbooking/service/EventService.java
package com.eventbooking.service;

import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import java.util.List;

public interface EventService {
    Event createEvent(Event event, User user);
    Event updateEvent(Long id, Event updatedEvent);
    void deleteEvent(Long id);
    List<Event> getAllEvents();
    Event getEventById(Long id);
    List<Event> getEventsByUser(User user);
}
