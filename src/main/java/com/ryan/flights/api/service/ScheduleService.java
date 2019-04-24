package com.ryan.flights.api.service;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.infrastructure.acl.schedules.ScheduleConsumer;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ScheduleService {

    private final ScheduleConsumer scheduleConsumer;

    @Autowired
    public ScheduleService(ScheduleConsumer scheduleConsumer) {
        this.scheduleConsumer = scheduleConsumer;
    }

    Optional<Schedule> getSchedule(String departureAirport, String arrivalAirport, LocalDateTime departureDateTime) {
        return scheduleConsumer.getSchedule(departureAirport, arrivalAirport, departureDateTime);
    }

    Boolean isValidSecondFlight(Flight flight, Leg leg) {
        return secondFlightisAfter(leg.getArrivalDateTime().toLocalTime(), flight);
    }

    List<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> isDepartureDay(day, departure))
                .filter(day -> getValidDayFirstLeg(day, departure.toLocalTime()))
                .collect(toList());
    }

    private Boolean getValidDayFirstLeg(Day day, LocalTime departure) {
        return day.getFlights()
                .stream()
                .anyMatch(flight -> getValidFlightByDepartureTime(flight, departure));
    }

    List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime) {
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
                (
                        secondFlightisAfter(arrivalTimeFirstLeg, flight)
                        &&
                        !flight.getArrivalTime().isAfter(arrivalEntireTrip)
                );
    }

    private Boolean secondFlightisAfter(LocalTime arrivalTimeFirstLeg, Flight flight) {
        return flight.getDepartureTime().isAfter(arrivalTimeFirstLeg.plusHours(2));
    }
}