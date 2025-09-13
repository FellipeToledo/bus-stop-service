package com.azvtech.bus_stop_service.repository;

import com.azvtech.bus_stop_service.model.BusStop;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BusStopRepository extends MongoRepository<BusStop, String> {
}
