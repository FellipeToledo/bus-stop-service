package com.azvtech.bus_stop_service.controller;

import com.azvtech.bus_stop_service.dto.Feature;
import com.azvtech.bus_stop_service.dto.GeoJsonResponse;
import com.azvtech.bus_stop_service.dto.Geometry;
import com.azvtech.bus_stop_service.dto.Properties;
import com.azvtech.bus_stop_service.model.BusStop;
import com.azvtech.bus_stop_service.service.BusStopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BusStopController {

    private final BusStopService service;

    public BusStopController(BusStopService service) {
        this.service = service;
    }

    @GetMapping("/stops/paginated")
    public GeoJsonResponse searchByBoundingBoxPaginated(
            @RequestParam double minLat,
            @RequestParam double minLon,
            @RequestParam double maxLat,
            @RequestParam double maxLon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size
    ) {
        System.out.println("Recebida requisição para bbox: " +
                minLat + ", " + minLon + ", " + maxLat + ", " + maxLon);
        List<BusStop> busStops = service.searchInsideBoundingBoxPaginated(
                minLat, minLon, maxLat, maxLon, page, size);
        System.out.println("Encontrados " + busStops.size() + " pontos");
        GeoJsonResponse response = convertToGeoJson(busStops);
        System.out.println("Response com " + response.getFeatures().size() + " features");

        return response;
    }

    private GeoJsonResponse convertToGeoJson(List<BusStop> busStops) {
        GeoJsonResponse response = new GeoJsonResponse();
        List<Feature> features = new ArrayList<>();

        for (BusStop stop : busStops) {
            Feature feature = new Feature();
            Properties properties = new Properties();
            Geometry geometry = new Geometry();

            // Configurar properties
            properties.setStop_id(stop.getStopId());
            properties.setStop_name(stop.getStopName());

            // Configurar geometry
            geometry.setType("Point");
            geometry.setCoordinates(stop.getLocation().getCoordinates());

            feature.setProperties(properties);
            feature.setGeometry(geometry);

            features.add(feature);
        }

        response.setFeatures(features);
        return response;
    }
}
