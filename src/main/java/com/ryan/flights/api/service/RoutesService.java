package com.ryan.flights.api.service;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.infrastructure.acl.routes.model.Route;

import java.util.List;

public interface RoutesService {

    List<Route> getValidRoutes();

    List<Route> getDirectRoutes(String departure, String arrival, List<Route> rutasValidas);

    List<Route> getRoutesFromDeparture(String departure, List<Route> validROutes);

    List<Route> getSecondLegsRoutes(List<Route> validRoutes, String departure, String arrival);

    Boolean routeMatchesDepAndArr(Route route, Leg leg, String arrivalAirport);
}