package com.ryan.flights.infrastructure.routes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Route {

    private String airportFrom;
    private String airportTo;
    private String connectingAirport;
    private Boolean newRoute;
    private Boolean seasonalRoute;
    private String operator;
    private String group;
    private List<String> similarArrivalAirportCodes = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public Route() {
    }

    public Route(String airportFrom, String airportTo, String connectingAirport, Boolean newRoute, Boolean seasonalRoute, String operator, String group, List<String> similarArrivalAirportCodes, List<String> tags) {
        this.airportFrom = airportFrom;
        this.airportTo = airportTo;
        this.connectingAirport = connectingAirport;
        this.newRoute = newRoute;
        this.seasonalRoute = seasonalRoute;
        this.operator = operator;
        this.group = group;
        this.similarArrivalAirportCodes = similarArrivalAirportCodes;
        this.tags = tags;
    }

    public String getAirportFrom() {
        return airportFrom;
    }

    public String getAirportTo() {
        return airportTo;
    }

    public String getConnectingAirport() {
        return connectingAirport;
    }

    public Boolean getNewRoute() {
        return newRoute;
    }

    public Boolean getSeasonalRoute() {
        return seasonalRoute;
    }

    public String getOperator() {
        return operator;
    }

    public String getGroup() {
        return group;
    }

    public List<String> getSimilarArrivalAirportCodes() {
        return similarArrivalAirportCodes;
    }

    public List<String> getTags() {
        return tags;
    }
}