package com.ryan.flights.infrastructure.acl.schedules;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
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