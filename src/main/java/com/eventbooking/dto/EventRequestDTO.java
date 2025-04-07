package com.eventbooking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EventRequestDTO {
    private String name;
    private String poster;
    private String address;
    private int seats;
    private LocalDate date;
    private String description;
}
