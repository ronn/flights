package com.ryan.flights;

import com.ryan.flights.api.model.Interconnection;
import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.RoutesService;
import com.ryan.flights.infrastructure.acl.routes.RoutesConsumer;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import com.ryan.flights.infrastructure.acl.schedules.SchedulesService;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class FlightsController {

    private final RoutesConsumer routesConsumer;
    private final SchedulesService schedulesService;
    private final RoutesService routesService;

    @Autowired
    public FlightsController(RoutesConsumer routesConsumer, SchedulesService schedulesService, RoutesService routesService) {
        this.routesConsumer = routesConsumer;
        this.schedulesService = schedulesService;
        this.routesService = routesService;
    }

    @RequestMapping("/interconnections")
    public List<Interconnection> prueba(
                                         @RequestParam("departure") String departureAirport,
                                         @RequestParam("arrival") String arrivalAirport,
                                         @RequestParam("departureDateTime") String departureDateTimeStr,
                                         @RequestParam("arrivalDateTime") String arrivalDateTimeStr
    ){
        List<Route> validRoutes = routesService.getValidRoutes();

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeStr);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeStr);

        List<Leg> legs = routesService.getRoutesFromDeparture(departureAirport, validRoutes)
                .stream()
                .flatMap(route -> schedulesService.getSchedule(route.getAirportFrom(), route.getAirportTo(), departureDateTime)
                        .map(schedule -> getValidDaysFirstLeg(schedule, departureDateTime)
                                .stream()
                                .flatMap(day -> day.getFlights()
                                        .stream()
                                        .map(flight -> new Leg(
                                                route.getAirportFrom(),
                                                route.getAirportTo(),
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
                                                )
                                        )
                                )
                        ).orElse(null)
                ).collect(toList());

        List<Interconnection> collect = legs.stream()
                //TODO consultar si la ruta existe primero...
                .filter(leg -> routesService.getSecondLegsRoutes(validRoutes, leg.getArrivalAirport(), arrivalAirport)
                        .stream()
                        .anyMatch(route -> route.getAirportFrom().equals(leg.getArrivalAirport())
                                &&
                                route.getAirportTo().equals(arrivalAirport))
                )
                .flatMap(leg -> schedulesService.getSchedule(leg.getArrivalAirport(), arrivalAirport, departureDateTime)
                        .map(schedule -> getValidDaysSecondLeg(schedule, leg, arrivalDateTime)
                                        .stream()
                                        .flatMap(day -> day.getFlights()
                                                .stream()
                                                .filter(flight ->flight.getDepartureTime()
                                                        .isAfter(leg.getArrivalDateTime().toLocalTime().plusHours(2))
                                                )
                                                .map(flight -> {
                                                            Leg leg2 = new Leg(
                                                                    leg.getArrivalAirport(),
                                                                    arrivalAirport,
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
                                                            return new Interconnection(Arrays.asList(leg, leg2));
                                                        }
                                                )
                                        )
                        )
                        .orElse(null)
                ).collect(toList());

        return collect;
    }

    private List<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> day.getDay().equals(departure.getDayOfMonth()))
                .filter(getValidDayFirstLeg(departure.toLocalTime()))
                .collect(toList());
    }

    private List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> day.getDay().equals(firstLeg.getArrivalDateTime().getDayOfMonth()))
                .filter(getValidDaySecondLeg(firstLeg.getArrivalDateTime().toLocalTime(), arrivalDateTime.toLocalTime()))
                .collect(toList());
    }

    private Predicate<Day> getValidDayFirstLeg(LocalTime departure) {
        return day -> day.getFlights()
                .stream()
                .anyMatch(getValidFlightByDepartureTime(departure)
                );
    }

    private Predicate<Day> getValidDaySecondLeg(LocalTime arrivalDateTimeFirstLeg, LocalTime arrivalEntireTrip) {
        return day -> day.getFlights()
                .stream()
                .anyMatch(getValidFlightByArrivalTime(arrivalDateTimeFirstLeg, arrivalEntireTrip));
    }

    private Predicate<Flight> getValidFlightByDepartureTime(LocalTime departure) {
        return flight ->
                !flight.getDepartureTime().isBefore(departure);
    }

    private Predicate<Flight> getValidFlightByArrivalTime(LocalTime arrivalTimeFirstLeg, LocalTime arrivalEntireTrip) {
        return flight -> {
            if (arrivalTimeFirstLeg.getHour() < 22){
                return flight.getDepartureTime().isAfter(arrivalTimeFirstLeg.plusHours(2))
                        &&
                        !flight.getArrivalTime().isAfter(arrivalEntireTrip);
            }
            return false;
        };
    }

    private LocalDateTime getOf(Day day, Integer month, Integer year, LocalTime time) {
        return LocalDateTime.of(
                year, month, day.getDay(),
                time.getHour(),
                time.getMinute()
        );
    }
}