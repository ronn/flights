package com.ryan.flights.api.service;

import com.ryan.flights.api.model.Interconnection;
import com.ryan.flights.api.model.Leg;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class InterconnectionService {

    private final ScheduleService scheduleService;
    private final RoutesService routesService;

    @Autowired
    public InterconnectionService(ScheduleService scheduleService, RoutesService routesService) {
        this.scheduleService = scheduleService;
        this.routesService = routesService;
    }

    public List<Interconnection> getInterconnections(
            String departureAirport,
            String arrivalAirport,
            String departureDateTimeStr,
            String arrivalDateTimeStr
    ) {
        List<Route> validRoutes = routesService.getValidRoutes();

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeStr);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeStr);

        List<Interconnection> directFlights = getDirectInterconections(departureAirport, arrivalAirport, validRoutes, departureDateTime);

        List<Interconnection> collectedInterconnections = routesService.getRoutesFromDeparture(departureAirport, validRoutes)
                .stream()
                .flatMap(route -> getFirstLegs(route, departureDateTime))
                .filter(leg -> leg.getDepartureDateTime().isAfter(departureDateTime))
                .filter(firstLeg -> legHasAValidRoute(firstLeg, arrivalAirport, validRoutes))
                .flatMap(firstLeg -> getNonDirectInterconections(firstLeg, arrivalAirport, departureDateTime, arrivalDateTime))
                .collect(toList());

        collectedInterconnections.addAll(directFlights);

        return collectedInterconnections;
    }

    private List<Interconnection> getDirectInterconections(String departureAirport, String arrivalAirport, List<Route> validRoutes, LocalDateTime departureDateTime) {
        return getDirectLegs(departureAirport, arrivalAirport, validRoutes, departureDateTime)
                .stream()
                .map(leg -> new Interconnection(Collections.singletonList(leg)))
                .collect(toList());
    }

    private List<Leg> getDirectLegs(String departureAirport, String arrivalAirport, List<Route> validRoutes, LocalDateTime departureDateTime) {
        return routesService.getDirectRoutes(departureAirport, arrivalAirport, validRoutes)
                .stream()
                .flatMap(route -> getFirstLegs(route, departureDateTime))
                .collect(toList());
    }

    private Stream<Interconnection> getNonDirectInterconections(Leg firstLeg, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return scheduleService.getSchedule(firstLeg.getArrivalAirport(), arrivalAirport, departureDateTime)
                .map(schedule ->
                        mapScheduleToNonDirectInterconnection(schedule, arrivalAirport, departureDateTime, arrivalDateTime, firstLeg))
                .orElse(null);
    }

    private Stream<Interconnection> mapScheduleToNonDirectInterconnection(Schedule schedule, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Leg firstLeg) {
        return scheduleService.getValidDaysSecondLeg(schedule, firstLeg, arrivalDateTime)
                .stream()
                .flatMap(day -> flatDayToInterconnections(day, arrivalAirport, departureDateTime, firstLeg, schedule));
    }

    private Stream<Interconnection> flatDayToInterconnections(Day day, String arrivalAirport, LocalDateTime departureDateTime, Leg firstLeg, Schedule schedule) {
        return day.getFlights()
                .stream()
                .filter(flight -> scheduleService.isValidSecondFlight(flight, firstLeg))
                .map(flight -> getInterconnection(flight, arrivalAirport, departureDateTime, firstLeg, schedule, day));
    }

    private Interconnection getInterconnection(Flight flight, String arrivalAirport, LocalDateTime departureDateTime, Leg firstLeg, Schedule schedule, Day day) {
        return new Interconnection(Arrays.asList(
                firstLeg,
                getLegFromFlight(departureDateTime, schedule, day, flight, firstLeg.getArrivalAirport(), arrivalAirport)
        ));
    }

    private Boolean legHasAValidRoute(Leg firstLeg, String arrivalAirport, List<Route> validRoutes) {
        return routesService.getSecondLegsRoutes(validRoutes, firstLeg.getArrivalAirport(), arrivalAirport)
                .stream()
                .anyMatch(route -> routesService.routeMatchesDepAndArr(route, arrivalAirport, firstLeg));
    }

    private Stream<Leg> getFirstLegs(Route route, LocalDateTime departureDateTime) {
        return scheduleService.getSchedule(route.getAirportFrom(), route.getAirportTo(), departureDateTime)
                .map(schedule -> mapScheduleToLeg(route, departureDateTime, schedule))
                .orElse(null);
    }

    private Stream<Leg> mapScheduleToLeg(Route route, LocalDateTime departureDateTime, Schedule schedule) {
        return scheduleService.getValidDaysFirstLeg(schedule, departureDateTime)
                .stream()
                .flatMap(day -> flatMapDayToLegs(route, departureDateTime, schedule, day));
    }

    private Stream<Leg> flatMapDayToLegs(Route route, LocalDateTime departureDateTime, Schedule schedule, Day day) {
        return day.getFlights()
                .stream()
                .map(flight ->
                        getLegFromFlight(departureDateTime, schedule, day, flight, route.getAirportFrom(), route.getAirportTo()));
    }

    private Leg getLegFromFlight(LocalDateTime departureDateTime, Schedule schedule, Day day, Flight flight, String airportFrom, String airportTo) {
        return buildLeg(
                airportFrom,
                airportTo,
                departureDateTime,
                schedule,
                day,
                flight
        );
    }

    private Leg buildLeg(String airportFrom, String airportTo, LocalDateTime departureDateTime, Schedule schedule, Day day, Flight flight) {
        return new Leg(
                airportFrom,
                airportTo,
                getOf(
                        day,
                        schedule.getMonth(),
                        departureDateTime.getYear(),
                        flight.getDepartureTime()
                ),
                getOf(
                        day,
                        schedule.getMonth(),
                        departureDateTime.getYear(),
                        flight.getArrivalTime()
                )
        );
    }

    private LocalDateTime getOf(Day day, Integer month, Integer year, LocalTime time) {
        return LocalDateTime.of(
                year, month, day.getDay(),
                time.getHour(),
                time.getMinute()
        );
    }
}