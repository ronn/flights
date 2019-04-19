package com.ryan.flights.infrastructure.acl.routes;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class RoutesConsumer {

    private final ConsumerService consumerService;

    @Autowired
    public RoutesConsumer(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public List<Route> getRyanAirRoutes(){
        return consumerService.getAllRoutes()
                .stream()
                .filter(route -> null == route.getConnectingAirport())
                .filter(route -> route.getOperator().equals("RYANAIR"))
                .collect(toList());
    }
}