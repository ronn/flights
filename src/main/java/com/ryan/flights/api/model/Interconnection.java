package com.ryan.flights.api.model;

import io.vavr.collection.List;

public class Interconnection {

    private final Integer stops;
    private final List<Leg> legs;

    public Interconnection(List<Leg> legs) {
        this.stops = legs.size() - 1;
        this.legs = legs;
    }

    public Integer getStops() {
        return stops;
    }

    public List<Leg> getLegs() {
        return legs;
    }
}