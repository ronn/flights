package com.ryan.flights.api.service;

import com.ryan.flights.api.model.Leg;
import com.ryan.flights.infrastructure.acl.schedules.model.Day;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import io.vavr.collection.List;
import io.vavr.control.Option;

import java.time.LocalDateTime;

public interface ScheduleService {

    Option<Schedule> getSchedule(String departureAirport, String arrivalAirport, LocalDateTime departureDateTime);

    List<Day> getValidDaysFirstLeg(Schedule schedule, LocalDateTime departure);

    List<Day> getValidDaysSecondLeg(Schedule schedule, Leg firstLeg, LocalDateTime arrivalDateTime);
}