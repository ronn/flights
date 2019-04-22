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

    //TODO Return an Optional!!!!!
    public Schedule getSchedule(String departure, String arrival, LocalDateTime departureDateTime){
        try {
            return consumerService.getSchedule(getUrlSche(departure, arrival, departureDateTime.getYear(), departureDateTime.getMonthValue()));
        }catch (HttpClientErrorException hcee){
            System.out.println(departure + " to " + arrival + " Not Found :::  exception: la cogí" + hcee.getClass().getTypeName());
            return null;
        }
    }

    private String getUrlSche(String departure, String arrival, Integer year, Integer month) {
        return "https://services-api.ryanair.com/timtbl/3/schedules/"
                + departure
                + "/"
                + arrival
                + "/years/"
                + year
                + "/months/"
                + month;
    }
}