package com.ryan.flights.infrastructure.acl.schedules;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ScheduleConsumer {

    private final ConsumerService consumerService;
    private static final Logger LOGGER = Logger.getLogger(ScheduleConsumer.class);

    public ScheduleConsumer(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public Optional<Schedule> getSchedule(String departure, String arrival, LocalDateTime departureDateTime){
        try {
            return Optional.ofNullable(consumerService.getSchedule(getUrlSche(departure, arrival, departureDateTime)));
        }catch (HttpClientErrorException httpcee){
            LOGGER.info("Schedule for " + departure + " -> " + arrival + " Not found");
            return Optional.empty();
        }catch (HttpServerErrorException httpsee){
            LOGGER.error("Schedule web service not available, try later");
            return Optional.empty();
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