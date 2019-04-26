package com.ryan.flights.api.service.impl;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.ScheduleService;
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleConsumer scheduleConsumer;

    @Autowired
    public ScheduleServiceImpl(ScheduleConsumer scheduleConsumer) {
        this.scheduleConsumer = scheduleConsumer;
    }

    public Optional<Schedule> getSchedule(String departureAirport, String arrivalAirport, LocalDateTime departureDateTime) {
        return scheduleConsumer.getSchedule(departureAirport, arrivalAirport, departureDateTime);
    }

    public Boolean isValidSecondFlight(Flight flight, Leg leg) {
        return secondFlightisAfter(leg.getArrivalDateTime().toLocalTime(), flight);
    }

    public Stream<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure) {
        return schedule
                .getDays()
                .stream()
                .filter(day -> isDepartureDay(day, departure))
         ;
    }

    public List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime) {
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