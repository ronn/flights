package com.ryan.flights.api.service;

import com.ryan.flights.api.model.Interconnection;
import io.vavr.collection.List;

public interface InterconnectionService {

    List<Interconnection> getInterconnections(
            String departureAirport,
            String arrivalAirport,
            String departureDateTimeStr,
            String arrivalDateTimeStr
    );
}