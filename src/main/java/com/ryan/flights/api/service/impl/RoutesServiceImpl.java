package com.ryan.flights.api.service.impl;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.RoutesService;
import com.ryan.flights.infrastructure.acl.routes.RoutesConsumer;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class RoutesServiceImpl implements RoutesService {

    private final RoutesConsumer routesConsumer;

    @Autowired
    public RoutesServiceImpl(RoutesConsumer routesConsumer) {
        this.routesConsumer = routesConsumer;
    }

    public List<Route> getValidRoutes() {
        return routesConsumer.getRyanAirRoutes();
    }

    public List<Route> getDirectRoutes(String departure, String arrival, List<Route> rutasValidas){
        return rutasValidas.stream()
                .filter(route -> route.getAirportTo().equals(arrival))
                .filter(route -> route.getAirportFrom().equals(departure))
                .collect(toList());
    }

    public List<Route> getRoutesFromDeparture(String departure, List<Route> validRoutes) {
        return validRoutes.stream()
                    .filter(route -> departure.equals(route.getAirportFrom()))
                    .collect(toList());
    }

    public List<Route> getSecondLegsRoutes(List<Route> validROutes, String departure, String arrival) {
        return validROutes.stream()
                .filter(route -> departure.equals(route.getAirportFrom()) && arrival.equals(route.getAirportTo()))
                .collect(toList());
    }

    public Boolean routeMatchesDepAndArr(Route route, String arrivalAirport, Leg leg) {
        return route.getAirportFrom().equals(leg.getArrivalAirport())
                &&
                route.getAirportTo().equals(arrivalAirport);
    }
}