package com.ryan.flights.infrastructure.schedules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {

    private LocalDate number;
    private LocalDate departureTime;
    private LocalDate arrivalTime;

    public Flight() {
    }

    public Flight(LocalDate number, LocalDate departureTime, LocalDate arrivalTime) {
        this.number = number;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public LocalDate getNumber() {
        return number;
    }

    public LocalDate getDepartureTime() {
        return departureTime;
    }

    public LocalDate getArrivalTime() {
        return arrivalTime;
    }
}