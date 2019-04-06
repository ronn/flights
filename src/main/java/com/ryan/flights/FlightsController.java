package com.ryan.flights;

import com.ryan.flights.domain.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class FlightsController {

    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public FlightsController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    private RestTemplate getRestTemplate() {
        return restTemplateBuilder.build();
    }

    @RequestMapping("routes")
    public List<Route> getRoutes(){
        return getRestTemplate()
                .exchange("https://services-api.ryanair.com/locate/3/routes",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Route>>() {}
                        ).getBody()
                .stream().filter(route -> null == route.getConnectingAirport())
                .filter(route -> route.getOperator().equals("RYANAIR"))
                .collect(toList());
    }
}