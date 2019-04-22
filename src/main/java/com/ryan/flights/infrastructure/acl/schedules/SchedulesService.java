package com.ryan.flights.infrastructure.acl.schedules;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;

@Service
public class SchedulesService {

    private final ConsumerService consumerService;

    public SchedulesService(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public Schedule getSchedule(String departure, String arrival, LocalDateTime departureDateTime){
        try {
            return consumerService.getSchedule(getUrlSche(departure, arrival, departureDateTime));
        }catch (HttpClientErrorException httpcee){
            System.out.println("Schedule for " + departure + " -> " + arrival + " Not found");
            return null;
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