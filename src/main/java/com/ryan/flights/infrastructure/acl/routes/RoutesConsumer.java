package com.ryan.flights.infrastructure.acl.routes;

import com.ryan.flights.infrastructure.acl.ConsumerService;
import com.ryan.flights.infrastructure.acl.routes.model.Route;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class RoutesConsumer {

    private final ConsumerService consumerService;
    private static final Logger LOGGER = Logger.getLogger(RoutesConsumer.class);

    @Autowired
    public RoutesConsumer(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @Cacheable("routes")
    public List<Route> getRyanAirRoutes(){
        LOGGER.info("Fetching routes...");
        List<Route> ryanair = consumerService.getAllRoutes()
                .stream()
                .filter(route -> null == route.getConnectingAirport())
                .filter(route -> route.getOperator().equals("RYANAIR"))
                .collect(toList());
        LOGGER.info("All RyanAir routes fetched");
        return ryanair;
    }
}