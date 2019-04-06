package com.ryan.flights.infrastructure.routes;

import com.ryan.flights.infrastructure.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class RoutesService {

    private final ConsumerService consumerService;

    @Autowired
    public RoutesService(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public List<Route> getValidRoutes(){
        return consumerService.getAllRoutes()
                .stream()
                .filter(route -> null == route.getConnectingAirport())
                .filter(route -> route.getOperator().equals("RYANAIR"))
                .collect(toList());
    }
}