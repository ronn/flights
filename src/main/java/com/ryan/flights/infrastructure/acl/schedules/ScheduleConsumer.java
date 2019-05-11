package com.ryan.flights.infrastructure.acl.schedules;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Service
public class ScheduleConsumer {

    private final ConsumerService consumerService;
    private static final Logger LOGGER = Logger.getLogger(ScheduleConsumer.class);

    public ScheduleConsumer(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public Option<Schedule> getSchedule(String departure, String arrival, LocalDateTime departureDateTime){


        return Try.of(() -> consumerService.getSchedule(getUrlSche(departure, arrival, departureDateTime)))
                .recover(exeption -> Match(exeption).of(
                        Case($(instanceOf(HttpClientErrorException.class)), () -> manageExeption("Schedule for " + departure + " -> " + arrival + " Not found")),
                        Case($(instanceOf(HttpServerErrorException.class)), () -> manageExeption("Schedule web service not available, try later"))
                )).getOrElse(Option.none());
    }

    private Option<Schedule> manageExeption(String msgToPrint) {
        LOGGER.info(msgToPrint);
        return Option.none();
    }

    private String getUrlSche(String departure, String arrival, LocalDateTime departureDateTime) {
        return "https://services-api.ryanair.com/timtbl/3/schedules/"
                + departure
                + "/"
                + arrival
                + "/years/"
                + departureDateTime.getYear()
                + "/months/"
                + departureDateTime.getMonthValue();
    }
}