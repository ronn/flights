package com.ryan.flights.infrastructure.acl.schedules.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vavr.collection.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Day {

    private Integer day;
    private List<Flight> flights;

    public Day() {
    }

    public Day(Integer day, List<Flight> flighs) {
        this.day = day;
        this.flights = flighs;
    }

    public Integer getDay() {
        return day;
    }

    public List<Flight> getFlights() {
        return flights;
    }
}