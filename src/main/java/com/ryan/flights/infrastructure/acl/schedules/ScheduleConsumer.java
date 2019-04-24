package com.ryan.flights.infrastructure.acl.schedules;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SchedulesService {

    private final ConsumerService consumerService;

    public SchedulesService(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public Optional<Schedule> getSchedule(String departure, String arrival, LocalDateTime departureDateTime){
        try {
            return Optional.ofNullable(consumerService.getSchedule(getUrlSche(departure, arrival, departureDateTime)));
        }catch (HttpClientErrorException httpcee){
            System.out.println("Schedule for " + departure + " -> " + arrival + " Not found");
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