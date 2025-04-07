package com.eventbooking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EventDTO {
    private String name;
    private String poster;
    private String address;
    private Integer seats;
    private LocalDate date;
}
