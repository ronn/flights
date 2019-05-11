package com.ryan.flights.api.service.impl;

import com.ryan.flights.api.model.Interconnection;
import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.InterconnectionService;
import com.ryan.flights.api.service.RoutesService;
import com.ryan.flights.api.service.ScheduleService;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import io.vavr.collection.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class InterconnectionServiceImpl implements InterconnectionService{

    private final ScheduleService scheduleService;
    private final RoutesService routesService;

    @Autowired
    public InterconnectionServiceImpl(ScheduleService scheduleService, RoutesService routesService) {
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

        return routesService.getRoutesFromDeparture(departureAirport, validRoutes)
                .flatMap(route -> getFirstLegs(route, departureDateTime, arrivalDateTime))
                .filter(firstLeg -> legHasAValidRoute(firstLeg, arrivalAirport, validRoutes))
                .flatMap(firstLeg -> getNonDirectInterconections(firstLeg, arrivalAirport, departureDateTime, arrivalDateTime))
                .appendAll(getDirectInterconections(departureAirport, arrivalAirport, validRoutes, departureDateTime, arrivalDateTime));
    }

    private List<Interconnection> getDirectInterconections(String departureAirport, String arrivalAirport, List<Route> validRoutes, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return getDirectLegs(departureAirport, arrivalAirport, validRoutes, departureDateTime, arrivalDateTime)
                .map(leg -> new Interconnection(List.of(leg)));
    }

    private List<Leg> getDirectLegs(String departureAirport, String arrivalAirport, List<Route> validRoutes, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return routesService.getDirectRoutes(departureAirport, arrivalAirport, validRoutes)
                .flatMap(route -> getFirstLegs(route, departureDateTime, arrivalDateTime));
    }

    private List<Interconnection> getNonDirectInterconections(Leg firstLeg, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return scheduleService.getSchedule(firstLeg.getArrivalAirport(), arrivalAirport, departureDateTime)
                .map(schedule ->
                        mapScheduleToNonDirectInterconnection(schedule, arrivalAirport, arrivalDateTime, firstLeg))
                .getOrElse(List.empty());
    }

    private List<Interconnection> mapScheduleToNonDirectInterconnection(Schedule schedule, String arrivalAirport, LocalDateTime arrivalDateTime, Leg firstLeg) {
        return scheduleService.getValidDaysSecondLeg(schedule, firstLeg, arrivalDateTime)
                .flatMap(day -> flatDayToInterconnections(day, arrivalAirport, firstLeg, schedule));
    }

    private List<Interconnection> flatDayToInterconnections(Day day, String arrivalAirport, Leg firstLeg, Schedule schedule) {
        return day.getFlights()
                .map(flight -> getInterconnection(flight, arrivalAirport, firstLeg, schedule, day));
    }

    private Interconnection getInterconnection(Flight flight, String arrivalAirport, Leg firstLeg, Schedule schedule, Day day) {
        return new Interconnection(List.of(
                firstLeg,
                buildLeg(firstLeg.getDepartureDateTime(), schedule, day, flight, firstLeg.getArrivalAirport(), arrivalAirport)
        ));
    }

    private Boolean legHasAValidRoute(Leg firstLeg, String arrivalAirport, List<Route> validRoutes) {
        return routesService.getSecondLegsRoutes(validRoutes, firstLeg.getArrivalAirport(), arrivalAirport)
                .asJava()
                .stream()
                .anyMatch(routes -> routesService.routeMatchesDepAndArr(routes, firstLeg, arrivalAirport));
    }

    private List<Leg> getFirstLegs(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return scheduleService.getSchedule(route.getAirportFrom(), route.getAirportTo(), departureDateTime)
                .map(schedule -> mapScheduleToLegs(route, schedule, departureDateTime, arrivalDateTime))
                .getOrElse(List.empty());
    }

    private List<Leg> mapScheduleToLegs(Route route, Schedule schedule, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return scheduleService
                .getValidDaysFirstLeg(schedule, departureDateTime)
                .flatMap(day -> flatMapDayToLegs(route, schedule, day, departureDateTime, arrivalDateTime));
    }

    private List<Leg> flatMapDayToLegs(Route route, Schedule schedule, Day day, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return day.getFlights()
                .filter(flight -> isAValidFlight(departureDateTime, arrivalDateTime, flight))
                .map(flight -> buildLeg(departureDateTime, schedule, day, flight, route.getAirportFrom(), route.getAirportTo()));
    }

    private boolean isAValidFlight(LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Flight flight) {
        return !flight.getDepartureTime().isBefore(departureDateTime.toLocalTime())
                && !flight.getArrivalTime().isAfter(arrivalDateTime.toLocalTime());
    }

    private Leg buildLeg(LocalDateTime departureDateTime, Schedule schedule, Day day, Flight flight, String airportFrom, String airportTo) {
        return new Leg(
                airportFrom,
                airportTo,
                getOf(
                        departureDateTime.getYear(),
                        schedule.getMonth(),
                        day,
                        flight.getDepartureTime()
                ),
                getOf(
                        departureDateTime.getYear(),
                        schedule.getMonth(),
                        day,
                        flight.getArrivalTime()
                )
        );
    }

    private LocalDateTime getOf(Integer year, Integer month, Day day, LocalTime time) {
        return LocalDateTime.of(
                year, month, day.getDay(),
                time.getHour(),
                time.getMinute()
        );
    }
}