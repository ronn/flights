package com.ryan.flights.infrastructure.schedules;

import com.ryan.flights.infrastructure.ConsumerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulesService {

    private final ConsumerService consumerService;

    public SchedulesService(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public List<Schedule> getSchedules(){
        return consumerService.consumeService("https://services-api.ryanair.com/locate/3/routes");
    }
}