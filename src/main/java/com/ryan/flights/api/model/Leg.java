package com.ryan.flights.api.model;

public class Leg {

    private final String departureAirport;
    private final String arrivalAirport;
    private final String departureDateTime;
    private final String arrivalDateTime;

    public Leg(String departureAirport, String arrivalAirport, String departureDateTime, String arrivalDateTime) {
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public String getDepartureDateTime() {
        return departureDateTime;
    }

    public String getArrivalDateTime() {
        return arrivalDateTime;
    }
}