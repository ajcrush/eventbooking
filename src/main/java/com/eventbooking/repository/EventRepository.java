package com.eventbooking.repository;

import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCreatedBy(User user);
}
