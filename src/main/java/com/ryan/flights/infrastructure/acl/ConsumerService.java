package com.ryan.flights.infrastructure.acl;

import com.ryan.flights.infrastructure.acl.routes.model.Route;
import com.ryan.flights.infrastructure.acl.schedules.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ConsumerService {

    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public ConsumerService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    private RestTemplate getRestTemplate() {
        return restTemplateBuilder.build();
    }

    public <T> T consumeService(String url){
        return getRestTemplate()
                .exchange(
                        url,
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<T>() {}
                ).getBody();
    }

    public List<Route> getAllRoutes(){
        return getRestTemplate()
                .exchange(
                        "https://services-api.ryanair.com/locate/3/routes",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Route>>() {}
                ).getBody();
    }

    public Schedule getSchedule(String url){
        return getRestTemplate()
                .exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Schedule>() {}
                ).getBody();
    }
}