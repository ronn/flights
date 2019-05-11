package com.ryan.flights.api.service.impl;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.api.service.ScheduleService;
import com.ryan.flights.infrastructure.acl.schedules.ScheduleConsumer;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleConsumer scheduleConsumer;

    @Autowired
    public ScheduleServiceImpl(ScheduleConsumer scheduleConsumer) {
        this.scheduleConsumer = scheduleConsumer;
    }

    public Option<Schedule> getSchedule(String departureAirport, String arrivalAirport, LocalDateTime departureDateTime) {
        return scheduleConsumer.getSchedule(departureAirport, arrivalAirport, departureDateTime);
    }

    public io.vavr.collection.List<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure) {
        return schedule
                .getDays()
                .filter(day -> isDepartureDay(day, departure))
         ;
    }

    public List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime) {
        return schedule
                .getDays()
                .filter(day -> isDepartureDay(day, firstLeg.getArrivalDateTime()))
                .filter(day -> getValidDaySecondLeg(day, firstLeg.getArrivalDateTime().toLocalTime(), arrivalDateTime.toLocalTime()));
    }

    private Boolean isDepartureDay(Day day, LocalDateTime arrivalDateTime) {
        return day.getDay().equals(arrivalDateTime.getDayOfMonth());
    }

    private boolean getValidDaySecondLeg(Day day, LocalTime arrivalDateTimeFirstLeg, LocalTime arrivalEntireTrip) {
        return day.getFlights()
                .exists(flight -> getValidFlightByArrivalTime(flight, arrivalDateTimeFirstLeg, arrivalEntireTrip));
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
        return !flight.getDepartureTime().isBefore(arrivalTimeFirstLeg.plusHours(2));
    }
}