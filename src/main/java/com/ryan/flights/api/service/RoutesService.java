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

    public List<List<Route>> getValidRoutes(String departure, String arrival) {
        List<Route> ryanAirRoutes = routesConsumer.getRyanAirRoutes();

        List<Route> routes = getDirectRoutes(departure, arrival, ryanAirRoutes);

        routes.addAll(getIndirectRoutes(departure, arrival, ryanAirRoutes));

        routes.sort((route, nextRoute) -> route.getAirportFrom().compareTo(arrival));

        return groupRoutes(departure, arrival, routes);
    }

    private List<Route> getDirectRoutes(String departure, String arrival, List<Route> rutasValidas){
        return rutasValidas.stream()
                .filter(route -> route.getAirportTo().equals(arrival))
                .filter(route -> route.getAirportFrom().equals(departure))
                .collect(toList());
    }

    private List<Route> getIndirectRoutes(String departure, String arrival, List<Route> rutasValidas) {
        List<Route> routesFromDeparture = getRoutesFromDeparture(departure, rutasValidas);

        List<Route> routesToArrival = getRoutesToArrival(arrival, rutasValidas);

        List<Route> routesFromDepartureToArrivalAirport = getRoutesFromDepartureToArrivalAirport(routesFromDeparture, routesToArrival);

        List<Route> indirectRoutes = getRoutesToArrivalFromDepartureAirport(routesFromDeparture, routesToArrival);

        indirectRoutes.addAll(routesFromDepartureToArrivalAirport);

        return indirectRoutes;
    }

    private List<Route> getRoutesFromDepartureToArrivalAirport(List<Route> routesFromDeparture, List<Route> routesToArrival) {
        return routesFromDeparture.stream()
                .filter(from -> routesToArrival.stream()
                        .anyMatch(to -> to.getAirportFrom().equals(from.getAirportTo()))
                ).collect(toList());
    }

    private List<Route> getRoutesToArrivalFromDepartureAirport(List<Route> routesFromDeparture, List<Route> routesToArrival) {
        return routesToArrival.stream()
                .filter(to -> routesFromDeparture.stream()
                        .anyMatch(from -> from.getAirportTo().equals(to.getAirportFrom()))
                ).collect(toList());
    }

    private List<Route> getRoutesFromDeparture(String departure, List<Route> rutasValidas) {
        return rutasValidas.stream()
                .filter(route -> departure.equals(route.getAirportFrom()))
                .collect(toList());
    }

    private List<Route> getRoutesToArrival(String arrival, List<Route> rutasValidas) {
        return rutasValidas.stream()
                .filter(route -> arrival.equals(route.getAirportTo()))
                .collect(toList());
    }

    private List<List<Route>> groupRoutes(String departure, String arrival, List<Route> validRoutes){
        return validRoutes.stream()
                .filter(route -> route.getAirportFrom().equals(departure))
                .map(route -> validRoutes.stream()
                        .filter(validRoute -> validRoute.equals(route) || validRoute.getAirportFrom().equals(route.getAirportTo()))
                        .collect(toList())
                ).collect(toList());
    }
}