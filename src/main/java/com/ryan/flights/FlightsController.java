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
import java.util.Objects;
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
        List<List<Route>> validRoutes = routesService.getValidRoutes(departureAirport, arrivalAirport);

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateTimeStr);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateTimeStr);

        List<Interconnection> collect = validRoutes.stream()
                .map(routes -> {
                    return getLegs(routes, departureDateTime, arrivalDateTime);
                    /*List<Schedule> legs = */

                    //return new Interconnection(legs);
                })
                //.filter(interconnection -> !interconnection.getLegs().isEmpty())

                //.flatMap(List::stream)
                .collect(toList());

        return collect;
/*


        //Schedule schedule = schedulesService.getSchedule(departureAirport, arrivalAirport, departureDateTime);

        return validRoutes.stream()
                .map(route -> {
                    try {
                        Schedule schedule = schedulesService.getSchedule(departureAirport, route.getAirportTo(), departureDateTime);
                        List<Leg> legs = schedule.getDays().stream()
                                .flatMap(day -> day.getFlights().stream()
                                        .map(flight -> new Leg(route.getAirportFrom(), route.getAirportTo(), departureDateTimeStr, arrivalDateTimeStr))).collect(toList());

                        return new Interconnection(legs);

                        //return new Leg(route.getAirportFrom(), route.getAirportTo(), departureDateTimeStr, arrivalDateTimeStr);
                    } catch (Exception e) {
                        System.out.println("Fallo: " + route.getAirportFrom() + " => " + route.getAirportTo());
                        e.printStackTrace();
                        return null;
                    }
                }).collect(toList());*/

 /*       if (getValidRoutes(departureAirport, arrivalAirport).isEmpty()){
            return new ResponseEntity<>(
                    "Ruta no válida",
                    HttpStatus.FORBIDDEN
            );
        }

        return new ResponseEntity<>(
                getInterconnections(departureAirport, arrivalAirport, schedule, departureDateTime, arrivalDateTime),
                HttpStatus.OK
        );*/
    }

    private Interconnection getLegs(List<Route> routes, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        System.out.println(routes.size());
        List<Leg> legs = routes.stream()
                .map(route -> {
                    Schedule schedule = schedulesService.getSchedule(
                            route.getAirportFrom(),
                            route.getAirportTo(),
                            departureDateTime
                    );

                    if (schedule != null) {
                        return getValidDays(schedule, departureDateTime, arrivalDateTime.toLocalTime(), route)
                                .stream()
                                .map(Day::getFlights)
                                .map(flights -> new Leg(route.getAirportFrom(), route.getAirportTo(), departureDateTime.toString(), arrivalDateTime.toString()))
                                .collect(toList());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(toList());

        routes.stream()
                .map(route -> new Leg(
                        route.getAirportFrom(), route.getAirportTo(), "Hoy", "Mañana"
                )).collect(toList());

        return new Interconnection(legs);
    }

    private List<Interconnection> getInterconnections(String departureAirport, String arrivalAirport, Schedule schedule, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return getValidDays(schedule, departureDateTime, arrivalDateTime.toLocalTime(), null)
                .stream()
                .map(day -> build(departureAirport, arrivalAirport, day, schedule.getMonth(), departureDateTime.getYear()))
                .collect(toList());
    }


    private List<Day> getValidDays(Schedule schedule, LocalDateTime departure, LocalTime arrival, Route route) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> day.getDay().equals(departure.getDayOfMonth()))
                .filter(getValidDay(departure.toLocalTime(), arrival, route))
                .collect(toList());
    }

    private Predicate<Day> getValidDay(LocalTime departure, LocalTime arrival, Route route) {
        return day -> day.getFlights()
                .stream()
                .anyMatch(getValidFlight(departure, arrival));
    }

    private Predicate<Flight> getValidFlight(LocalTime departure, LocalTime arrival) {
        return flight ->
                !flight.getDepartureTime().isBefore(departure)
                &&
                !flight.getArrivalTime().isAfter(arrival);
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