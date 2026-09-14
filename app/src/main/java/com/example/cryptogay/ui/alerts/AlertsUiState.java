package com.example.cryptogay.ui.alerts;

import com.example.cryptogay.data.local.PriceAlert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertsUiState {

    public enum Status {
        LOADING,
        SUCCESS,
        EMPTY,
        ERROR
    }

    private final Status status;
    private final List<PriceAlert> alerts;
    private final String errorMessage;

    public AlertsUiState(Status status, List<PriceAlert> alerts, String errorMessage) {
        this.status = status;
        this.alerts = alerts != null ? Collections.unmodifiableList(new ArrayList<>(alerts)) : Collections.emptyList();
        this.errorMessage = errorMessage;
    }

    public static AlertsUiState loading() {
        return new AlertsUiState(Status.LOADING, Collections.emptyList(), null);
    }

    public static AlertsUiState success(List<PriceAlert> alerts) {
        return new AlertsUiState(Status.SUCCESS, alerts, null);
    }

    public static AlertsUiState empty() {
        return new AlertsUiState(Status.EMPTY, Collections.emptyList(), null);
    }

    public static AlertsUiState error(String message) {
        return new AlertsUiState(Status.ERROR, Collections.emptyList(), message);
    }

    public Status getStatus() {
        return status;
    }

    public List<PriceAlert> getAlerts() {
        return alerts;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
