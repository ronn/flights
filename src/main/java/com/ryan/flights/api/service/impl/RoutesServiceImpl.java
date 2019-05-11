package com.ryan.flights.api.service.impl;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.RoutesService;
import com.ryan.flights.infrastructure.acl.routes.RoutesConsumer;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import io.vavr.collection.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return rutasValidas
                .filter(route -> route.getAirportTo().equals(arrival))
                .filter(route -> route.getAirportFrom().equals(departure));
    }

    public List<Route> getRoutesFromDeparture(String departure, List<Route> validRoutes) {
        return validRoutes
                    .filter(route -> departure.equals(route.getAirportFrom()));
    }

    public List<Route> getSecondLegsRoutes(List<Route> validRoutes, String departure, String arrival) {
        return validRoutes
                .filter(route -> departure.equals(route.getAirportFrom()) && arrival.equals(route.getAirportTo()));
    }

    public Boolean routeMatchesDepAndArr(Route route, Leg leg, String arrivalAirport) {
        return route.getAirportFrom().equals(leg.getArrivalAirport())
                &&
                route.getAirportTo().equals(arrivalAirport);
    }
}