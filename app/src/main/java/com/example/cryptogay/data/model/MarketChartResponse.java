package com.example.cryptogay.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class MarketChartResponse {

    @SerializedName("prices")
    private final List<List<Double>> prices;

    public MarketChartResponse(List<List<Double>> prices) {
        this.prices = prices;
    }

    public List<List<Double>> getPrices() {
        return prices != null ? prices : Collections.emptyList();
    }
}
