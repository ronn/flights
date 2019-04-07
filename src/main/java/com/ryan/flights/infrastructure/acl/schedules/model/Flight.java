package com.ryan.flights.infrastructure.acl.schedules.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {

    private String number;
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    public Flight() {
    }

    public Flight(String number, LocalTime departureTime, LocalTime arrivalTime) {
        this.number = number;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getNumber() {
        return number;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
}