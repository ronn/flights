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
        List<Route> validRoutes = routesService.getValidRoutes(departureAirport, arrivalAirport);

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeStr);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeStr);

        List<Leg> legs = validRoutes.stream()
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
                                                ).toString(),
                                                getOf(
                                                        day,
                                                        schedule.getMonth(),
                                                        departureDateTime.getYear(),
                                                        flight.getArrivalTime()
                                                ).toString()
                                                )
                                        )
                                )
                        ).orElse(null)
                ).collect(toList());

        List<Interconnection> collect = legs.stream()
                //TODO consultar si la ruta existe primero...
                .flatMap(leg -> schedulesService.getSchedule(leg.getArrivalAirport(), arrivalAirport, departureDateTime)
                        .map(schedule -> getValidDaysSecondLeg(schedule, leg, arrivalDateTime)// TODO aquí tomar validDays para 2ds stop
                                .stream()
                                .flatMap(day -> day.getFlights()
                                        .stream()
                                        .map(flight -> {

                                                    Leg leg1 = new Leg(
                                                            leg.getArrivalAirport(),
                                                            arrivalAirport,
                                                            getOf(
                                                                    day,
                                                                    schedule.getMonth(),
                                                                    departureDateTime.getYear(),
                                                                    flight.getDepartureTime()
                                                            ).toString(),
                                                            getOf(
                                                                    day,
                                                                    schedule.getMonth(),
                                                                    departureDateTime.getYear(),
                                                                    flight.getArrivalTime()
                                                            ).toString()
                                                    );
                                                    return new Interconnection(Arrays.asList(leg, leg1));
                                                }
                                        )
                                )
                        )
                        .orElse(null)
                ).collect(toList());
        return collect;
    }

    private List<Interconnection> getInterconnections(String departureAirport, String arrivalAirport, Schedule schedule, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return getValidDaysFirstLeg(schedule, departureDateTime)
                .stream()
                .map(day -> build(departureAirport, arrivalAirport, day, schedule.getMonth(), departureDateTime.getYear()))
                .collect(toList());
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
        LocalDateTime arrivalDateTimeFirstLeg = LocalDateTime.parse(firstLeg.getArrivalDateTime());
        return schedule
                .getDays()
                .stream()
                .filter(day -> day.getDay().equals(arrivalDateTimeFirstLeg.getDayOfMonth()))
                .filter(getValidDaySecondLeg(arrivalDateTimeFirstLeg.toLocalTime(), arrivalDateTime.toLocalTime()))
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
                System.out.println(arrivalTimeFirstLeg + " :: " + flight.getDepartureTime() + " => " + (arrivalTimeFirstLeg.plusHours(2).isBefore(flight.getDepartureTime()) &&
                        !flight.getArrivalTime().isAfter(arrivalEntireTrip)));
                return arrivalTimeFirstLeg.plusHours(2).isBefore(flight.getDepartureTime())
                        &&
                        !flight.getArrivalTime().isAfter(arrivalEntireTrip);
            }
            return false;
        };
    }

    private Interconnection build(String departureAirport, String arrivalAirport, Day day, Integer month, Integer year){
        return new Interconnection(
                day.getFlights().stream()
                        .map(flight -> buildLeg(
                                departureAirport,
                                arrivalAirport,
                                getOf(day, month, year, flight.getDepartureTime()),
                                getOf(day, month, year, flight.getArrivalTime())
                        ))
                        .collect(toList())
                );
    }

    private LocalDateTime getOf(Day day, Integer month, Integer year, LocalTime time) {
        return LocalDateTime.of(
                year, month, day.getDay(),
                time.getHour(),
                time.getMinute()
        );
    }

    private Leg buildLeg(String departureAirport, String arrivalAirport, LocalDateTime departure, LocalDateTime arrival){
        return new Leg(
                departureAirport,
                arrivalAirport,
                departure.toString(),
                arrival.toString()
        );
    }
}