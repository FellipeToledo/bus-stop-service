package com.azvtech.bus_stop_service.service;

import com.azvtech.bus_stop_service.dto.Feature;
import com.azvtech.bus_stop_service.dto.GeoJsonResponse;
import com.azvtech.bus_stop_service.model.BusStop;
import com.azvtech.bus_stop_service.model.GeoJsonPoint;
import com.azvtech.bus_stop_service.repository.BusStopRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusStopService {


    private static final String BASE_URL = "https://pgeo3.rio.rj.gov.br/arcgis/rest/services/Hosted/" +
            "Pontos_de_Parada_da_rede_de_transporte_público_por_ônibus_(SPPO)/FeatureServer/0/query";
    private static final int PAGE_SIZE = 1000;
    private final MongoTemplate mongoTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final BusStopRepository repository;

    public BusStopService(MongoTemplate mongoTemplate, BusStopRepository repository) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
    }

    @PostConstruct
    public void loadInitialData() {
        if (repository.count() > 0) {
            System.out.println("[INFO] Pontos de parada já existem no MongoDB. Pulando carga.");
            return;
        }

        System.out.println("[INFO] Carregando pontos de parada da API pública...");
        List<Feature> features = loadAllPoints();

        List<BusStop> busStops = features.stream().map(feature -> {
            String stopId = feature.getProperties().getStop_id();
            String stopName = feature.getProperties().getStop_name();
            List<Double> coords = feature.getGeometry().getCoordinates(); // [lon, lat]

            GeoJsonPoint location = new GeoJsonPoint(coords);

            return new BusStop(stopId, stopName, location);
        }).toList();

        repository.saveAll(busStops);

        System.out.printf("[INFO] %d pontos de parada salvos no MongoDB.%n", busStops.size());
    }

    private List<Feature> loadAllPoints() {
        List<Feature> allFeatures = new ArrayList<>();
        int offset = 0;

        while (true) {
            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("outFields", "*")
                    .queryParam("where", "1=1")
                    .queryParam("f", "geojson")
                    .queryParam("resultRecordCount", PAGE_SIZE)
                    .queryParam("resultOffset", offset)
                    .build()
                    .toUri();

            GeoJsonResponse page = restTemplate.getForObject(uri, GeoJsonResponse.class);

            if (page == null || page.getFeatures() == null || page.getFeatures().isEmpty()) {
                break;
            }

            allFeatures.addAll(page.getFeatures());

            if (page.getFeatures().size() < PAGE_SIZE) {
                break; // última página
            }

            offset += PAGE_SIZE;
        }

        return allFeatures;
    }

    public void update() {
        System.out.println("[INFO] Recarregando dados da API...");
        repository.deleteAll();
        loadInitialData();
    }

    public List<BusStop> searchInsideBoundingBoxPaginated(
            double minLat, double minLon, double maxLat, double maxLon, int page, int size) {

        List<Point> polygonPoints = List.of(
                new Point(minLon, minLat),
                new Point(minLon, maxLat),
                new Point(maxLon, maxLat),
                new Point(maxLon, minLat),
                new Point(minLon, minLat)
        );

        GeoJsonPolygon polygon = new GeoJsonPolygon(polygonPoints);

        Query query = new Query();
        query.addCriteria(Criteria.where("location").within(polygon));

        // Adiciona paginação
        query.skip(page * size);
        query.limit(size);

        return mongoTemplate.find(query, BusStop.class);
    }
}
