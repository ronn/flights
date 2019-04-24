package com.ryan.flights.api.service;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Flight;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {

    Optional<Schedule> getSchedule(String departureAirport, String arrivalAirport, LocalDateTime departureDateTime);

    Boolean isValidSecondFlight(Flight flight, Leg leg);

    List<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure);

    List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime);
}