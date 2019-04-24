package com.ryan.flights;

import com.ryan.flights.api.model.Interconnection;
import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.RoutesService;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import com.ryan.flights.infrastructure.acl.schedules.ScheduleConsumer;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class FlightsController {
    private final ScheduleConsumer scheduleConsumer;
    private final RoutesService routesService;

    @Autowired
    public FlightsController(RoutesService routesService, ScheduleConsumer scheduleConsumer) {
        this.scheduleConsumer = scheduleConsumer;
        this.routesService = routesService;
    }

    @RequestMapping("/interconnections")
    public List<Interconnection> searchInterconnections(
                                         @RequestParam("departure") String departureAirport,
                                         @RequestParam("arrival") String arrivalAirport,
                                         @RequestParam("departureDateTime") String departureDateTimeStr,
                                         @RequestParam("arrivalDateTime") String arrivalDateTimeStr
    ){
        return getInterconnections(departureAirport, arrivalAirport, departureDateTimeStr, arrivalDateTimeStr);
    }

    private List<Interconnection> getInterconnections(
            String departureAirport,
            String arrivalAirport,
            String departureDateTimeStr,
            String arrivalDateTimeStr
    ) {
        List<Route> validRoutes = routesService.getValidRoutes();

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeStr);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeStr);

        //routesService.getDirectRoutes(departureAirport, arrivalAirport, validRoutes);
        return routesService.getRoutesFromDeparture(departureAirport, validRoutes)
                .stream()
                .flatMap(route -> getFirstLegs(route, departureDateTime))
                .filter(firstLeg -> legHasAValidRoute(firstLeg, arrivalAirport, validRoutes))
                .flatMap(firstLeg -> getNonDirectInterconections( firstLeg, arrivalAirport, departureDateTime, arrivalDateTime))
                .collect(toList());
    }

    private Stream<Interconnection> getNonDirectInterconections(Leg firstLeg, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return scheduleConsumer.getSchedule(firstLeg.getArrivalAirport(), arrivalAirport, departureDateTime)
                .map(schedule ->
                        mapScheduleToNonDirectInterconnection(schedule, arrivalAirport, departureDateTime, arrivalDateTime, firstLeg))
                .orElse(null);
    }

    private Stream<Interconnection> mapScheduleToNonDirectInterconnection(Schedule schedule, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Leg firstLeg) {
        return getValidDaysSecondLeg(schedule, firstLeg, arrivalDateTime)
                .stream()
                .flatMap(day -> flatDayToInterconnections(day, arrivalAirport, departureDateTime, firstLeg, schedule));
    }

    private Stream<Interconnection> flatDayToInterconnections(Day day, String arrivalAirport, LocalDateTime departureDateTime, Leg firstLeg, Schedule schedule) {
        return day.getFlights()
                .stream()
                .filter(flight -> isValidSecondFlight(flight, firstLeg))
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
                .anyMatch(route -> routeMatchesDepAndArr(route, arrivalAirport, firstLeg));
    }

    private Stream<Leg> getFirstLegs(Route route, LocalDateTime departureDateTime) {
        return scheduleConsumer.getSchedule(route.getAirportFrom(), route.getAirportTo(), departureDateTime)
                .map(schedule -> mapScheduleToLeg(route, departureDateTime, schedule))
                .orElse(null);
    }

    private Stream<Leg> mapScheduleToLeg(Route route, LocalDateTime departureDateTime, Schedule schedule) {
        return getValidDaysFirstLeg(schedule, departureDateTime)
                .stream()
                .flatMap(day -> flatMapDayToLeg(route, departureDateTime, schedule, day));
    }

    private Stream<Leg> flatMapDayToLeg(Route route, LocalDateTime departureDateTime, Schedule schedule, Day day) {
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

    private Boolean routeMatchesDepAndArr(Route route, String arrivalAirport, Leg leg) {
        return route.getAirportFrom().equals(leg.getArrivalAirport())
                &&
                route.getAirportTo().equals(arrivalAirport);
    }

    private Boolean isValidSecondFlight(Flight flight, Leg leg) {
        return secondFlightisAfter(leg.getArrivalDateTime().toLocalTime(), flight);
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

    private List<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> isDepartureDay(day, departure))
                .filter(day -> getValidDayFirstLeg(day, departure.toLocalTime()))
                .collect(toList());
    }

    private List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> isDepartureDay(day, firstLeg.getArrivalDateTime()))
                .filter(day -> getValidDaySecondLeg(day, firstLeg.getArrivalDateTime().toLocalTime(), arrivalDateTime.toLocalTime()))
                .collect(toList());
    }

    private Boolean isDepartureDay(Day day, LocalDateTime arrivalDateTime) {
        return day.getDay().equals(arrivalDateTime.getDayOfMonth());
    }

    private Boolean getValidDayFirstLeg(Day day, LocalTime departure) {
        return day.getFlights()
                .stream()
                .anyMatch(flight -> getValidFlightByDepartureTime(flight, departure));
    }

    private boolean getValidDaySecondLeg(Day day, LocalTime arrivalDateTimeFirstLeg, LocalTime arrivalEntireTrip) {
        return day.getFlights()
                .stream()
                .anyMatch(flight -> getValidFlightByArrivalTime(flight, arrivalDateTimeFirstLeg, arrivalEntireTrip));
    }

    private Boolean getValidFlightByDepartureTime(Flight flight, LocalTime departure) {
        return !flight.getDepartureTime().isBefore(departure);
    }

    private boolean getValidFlightByArrivalTime(Flight flight, LocalTime arrivalTimeFirstLeg, LocalTime arrivalEntireTrip) {
            return arrivalTimeFirstLeg.getHour() < 22
                    &&
                    (secondFlightisAfter(arrivalTimeFirstLeg, flight)
                            &&
                            !flight.getArrivalTime().isAfter(arrivalEntireTrip)
                    );
    }

    private Boolean secondFlightisAfter(LocalTime arrivalTimeFirstLeg, Flight flight) {
        return flight.getDepartureTime().isAfter(arrivalTimeFirstLeg.plusHours(2));
    }

    private LocalDateTime getOf(Day day, Integer month, Integer year, LocalTime time) {
        return LocalDateTime.of(
                year, month, day.getDay(),
                time.getHour(),
                time.getMinute()
        );
    }
}