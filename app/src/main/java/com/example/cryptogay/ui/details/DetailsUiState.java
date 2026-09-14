package com.example.cryptogay.ui.details;

import com.example.cryptogay.data.model.Coin;
import com.github.mikephil.charting.data.Entry;

import java.util.Collections;
import java.util.List;

public class DetailsUiState {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY
    }

    private final Status status;
    private final Coin coin;
    private final List<Entry> chartEntries;
    private final boolean isChartLoading;
    private final String errorMessage;
    private final String selectedTimeSpan;

    public DetailsUiState(Status status, Coin coin, List<Entry> chartEntries,
                          boolean isChartLoading, String errorMessage, String selectedTimeSpan) {
        this.status = status;
        this.coin = coin;
        this.chartEntries = chartEntries != null ? chartEntries : Collections.emptyList();
        this.isChartLoading = isChartLoading;
        this.errorMessage = errorMessage;
        this.selectedTimeSpan = selectedTimeSpan;
    }

    public static DetailsUiState loading() {
        return new DetailsUiState(Status.LOADING, null, Collections.emptyList(), false, null, "7");
    }

    public static DetailsUiState success(Coin coin, List<Entry> entries, String timeSpan) {
        return new DetailsUiState(Status.SUCCESS, coin, entries, false, null, timeSpan);
    }

    public static DetailsUiState chartLoading(Coin coin, List<Entry> currentEntries, String timeSpan) {
        return new DetailsUiState(Status.SUCCESS, coin, currentEntries, true, null, timeSpan);
    }

    public static DetailsUiState error(String message) {
        return new DetailsUiState(Status.ERROR, null, Collections.emptyList(), false, message, "7");
    }

    public static DetailsUiState empty() {
        return new DetailsUiState(Status.EMPTY, null, Collections.emptyList(), false, null, "7");
    }

    public Status getStatus() {
        return status;
    }

    public Coin getCoin() {
        return coin;
    }

    public List<Entry> getChartEntries() {
        return chartEntries;
    }

    public boolean isChartLoading() {
        return isChartLoading;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSelectedTimeSpan() {
        return selectedTimeSpan != null ? selectedTimeSpan : "7";
    }
}
