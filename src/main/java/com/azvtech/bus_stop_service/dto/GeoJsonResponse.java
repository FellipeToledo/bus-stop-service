package com.azvtech.bus_stop_service.dto;

import java.util.List;

public class GeoJsonResponse {

    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
