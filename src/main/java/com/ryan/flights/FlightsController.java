package com.ryan.flights;

import com.ryan.flights.infrastructure.routes.Route;
import com.ryan.flights.infrastructure.routes.RoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FlightsController {

    private final RoutesService routesService;

    @Autowired
    public FlightsController(RoutesService routesService) {
        this.routesService = routesService;
    }

    @RequestMapping("/routes")
    public List<Route> getRoutes(){
        return routesService.getValidRoutes();
    }
}