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
    public List<Route> prueba(
                                         @RequestParam("departure") String departureAirport,
                                         @RequestParam("arrival") String arrivalAirport,
                                         @RequestParam("departureDateTime") String departureDateTimeStr,
                                         @RequestParam("arrivalDateTime") String arrivalDateTimeStr
    ){
        return routesService.getValidRoutes(departureAirport, arrivalAirport);

 /*       if (getValidRoutes(departureAirport, arrivalAirport).isEmpty()){
            return new ResponseEntity<>(
                    "Ruta no válida",
                    HttpStatus.FORBIDDEN
            );
        }

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeStr);

        Schedule schedule = schedulesService.getSchedule(departureAirport, arrivalAirport, departureDateTime);

        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeStr);

        return new ResponseEntity<>(
                getInterconnections(departureAirport, arrivalAirport, schedule, departureDateTime, arrivalDateTime),
                HttpStatus.OK
        );*/
    }

    private List<Interconnection> getInterconnections(String departureAirport, String arrivalAirport, Schedule schedule, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return getValidDays(schedule, departureDateTime, arrivalDateTime.toLocalTime())
                .stream()
                .map(day -> build(departureAirport, arrivalAirport, day, schedule.getMonth(), departureDateTime.getYear()))
                .collect(toList());
    }


    private List<Day> getValidDays(Schedule schedule, LocalDateTime departure, LocalTime arrival) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> day.getDay().equals(departure.getDayOfMonth()))
                .filter(getValidDay(departure.toLocalTime(), arrival))
                .collect(toList());
    }

    private Predicate<Day> getValidDay(LocalTime departure, LocalTime arrival) {
        return day -> day.getFlights()
                .stream()
                .anyMatch(getValidFlight(departure, arrival)
                );
    }

    private Predicate<Flight> getValidFlight(LocalTime departure, LocalTime arrival) {
        return flight ->
                !flight.getDepartureTime().isBefore(departure)
                &&
                !flight.getArrivalTime().isAfter(arrival);
    }



    private Interconnection build(String departureAirport, String arrivalAirport, Day day, Integer month, Integer year){
        return new Interconnection(
                day.getFlights().size() - 1,
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