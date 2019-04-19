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

    public List<Route> getValidRoutes(String departure, String arrival) {
        List<Route> ryanAirRoutes = routesConsumer.getRyanAirRoutes();

        List<Route> directRoutes = getDirectRoutes(departure, arrival, ryanAirRoutes);

        directRoutes.addAll(getIndirectRoutes(departure, arrival, ryanAirRoutes));

        return directRoutes;
    }

    private List<Route> getDirectRoutes(String departure, String arrival, List<Route> rutasValidas){
        return rutasValidas.stream()
                .filter(route -> route.getAirportTo().equals(arrival))
                .filter(route -> route.getAirportFrom().equals(departure))
                .collect(toList());
    }

    private List<Route> getIndirectRoutes(String departure, String arrival, List<Route> rutasValidas) {
        List<Route> routesFromDeparture = rutasValidas.stream()
                .filter(route -> departure.equals(route.getAirportFrom()))
                .collect(toList());

        List<Route> routesToArrival = rutasValidas.stream()
                .filter(route -> arrival.equals(route.getAirportTo()))
                .collect(toList());

        List<Route> collect = routesFromDeparture.stream()
                .filter(desde -> routesToArrival.stream().anyMatch(hacia -> hacia.getAirportFrom().equals(desde.getAirportTo())))
                .collect(toList());

        List<Route> collect1 = routesToArrival.stream()
                .filter(hacia -> routesFromDeparture.stream().anyMatch(desde -> desde.getAirportTo().equals(hacia.getAirportFrom())))
                .collect(toList());

        collect1.addAll(collect);

        return collect1;
    }
}