package com.ryan.flights.api.model;

import java.util.List;

public class Inteconnection {

    private final Integer stops;
    private final List<Leg> legs;

    public Inteconnection(Integer stops, List<Leg> legs) {
        this.stops = stops;
        this.legs = legs;
    }

    public Integer getStops() {
        return stops;
    }

    public List<Leg> getLegs() {
        return legs;
    }
}