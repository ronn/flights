package com.ryan.flights;

import com.ryan.flights.api.model.Interconnection;
import com.ryan.flights.api.service.InterconnectionService;
import io.vavr.collection.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FlightsController {
    private final InterconnectionService interconnectionService;

    @Autowired
    public FlightsController(InterconnectionService interconnectionService) {
        this.interconnectionService = interconnectionService;
    }

    @RequestMapping("/interconnections")
    public List<Interconnection> searchInterconnections(
                                         @RequestParam("departure") String departureAirport,
                                         @RequestParam("arrival") String arrivalAirport,
                                         @RequestParam("departureDateTime") String departureDateTimeStr,
                                         @RequestParam("arrivalDateTime") String arrivalDateTimeStr
    ){
        return interconnectionService.getInterconnections(departureAirport, arrivalAirport, departureDateTimeStr, arrivalDateTimeStr);
    }
}