package com.ryan.flights.api.service;

import com.ryan.flights.infrastructure.acl.routes.RoutesConsumer;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class RoutesService {

    private final RoutesConsumer routesConsumer;

    @Autowired
    public RoutesService(RoutesConsumer routesConsumer) {
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

    public List<Route> getRoutesFromDeparture(String departure, List<Route> validROutes) {
        return validROutes.stream()
                    .filter(route -> departure.equals(route.getAirportFrom()))
                    .collect(toList());
    }

    public List<Route> getSecondLegsRoutes(List<Route> validROutes, String departure, String arrival) {
        return validROutes.stream()
                .filter(route -> departure.equals(route.getAirportFrom()) && arrival.equals(route.getAirportTo()))
                .collect(toList());
    }
}