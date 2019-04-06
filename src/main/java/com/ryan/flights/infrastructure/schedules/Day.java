package com.ryan.flights.infrastructure.schedules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Day {

    private Integer day;
    private List<Flight> flighs;

    public Day() {
    }

    public Day(Integer day, List<Flight> flighs) {
        this.day = day;
        this.flighs = flighs;
    }

    public Integer getDay() {
        return day;
    }

    public List<Flight> getFlighs() {
        return flighs;
    }
}