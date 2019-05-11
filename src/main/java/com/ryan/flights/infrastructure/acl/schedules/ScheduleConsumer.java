package com.ryan.flights.infrastructure.acl.schedules;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import io.vavr.control.Option;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

@Service
public class ScheduleConsumer {

    private final ConsumerService consumerService;
    private static final Logger LOGGER = Logger.getLogger(ScheduleConsumer.class);

    public ScheduleConsumer(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public Option<Schedule> getSchedule(String departure, String arrival, LocalDateTime departureDateTime){
        try {
            return Option.of(consumerService.getSchedule(getUrlSche(departure, arrival, departureDateTime)));
        }catch (HttpClientErrorException httpcee){
            LOGGER.info("Schedule for " + departure + " -> " + arrival + " Not found");
            return Option.none();
        }catch (HttpServerErrorException httpsee){
            LOGGER.error("Schedule web service not available, try later");
            return Option.none();
        }
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