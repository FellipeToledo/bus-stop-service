package com.azvtech.bus_stop_service.model;

import java.util.List;

public class GeoJsonPoint {

    private String type = "Point";
    private List<Double> coordinates;

    public GeoJsonPoint() {
    }

    public GeoJsonPoint(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
