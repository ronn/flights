package com.ryan.flights.infrastructure.acl.schedules.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vavr.collection.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {

    private Integer month;
    private List<Day> days;

    public Schedule() {
    }

    public Schedule(Integer month, List<Day> days) {
        this.month = month;
        this.days = days;
    }

    public Integer getMonth() {
        return month;
    }

    public List<Day> getDays() {
        return days;
    }
}