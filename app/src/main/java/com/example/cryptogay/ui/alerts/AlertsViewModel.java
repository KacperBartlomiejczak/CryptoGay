package com.example.cryptogay.ui.alerts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.data.repository.AlertsRepository;

import java.util.List;

public class AlertsViewModel extends AndroidViewModel {

    private final AlertsRepository alertsRepository;
    private final MediatorLiveData<AlertsUiState> uiState = new MediatorLiveData<>();

    public AlertsViewModel(@NonNull Application application) {
        this(application, new AlertsRepository(application));
    }

    public AlertsViewModel(@NonNull Application application, @NonNull AlertsRepository alertsRepository) {
        super(application);
        this.alertsRepository = alertsRepository;
        uiState.setValue(AlertsUiState.loading());

        LiveData<List<PriceAlert>> alertsSource = alertsRepository.getAllAlerts();
        uiState.addSource(alertsSource, alerts -> {
            if (alerts == null || alerts.isEmpty()) {
                uiState.setValue(AlertsUiState.empty());
            } else {
                uiState.setValue(AlertsUiState.success(alerts));
            }
        });
    }

    public LiveData<AlertsUiState> getUiState() {
        return uiState;
    }

    public void deleteAlert(PriceAlert alert) {
        if (alert != null) {
            alertsRepository.deleteAlert(alert, null);
        }
    }

    public void toggleActive(PriceAlert alert) {
        if (alert != null) {
            alertsRepository.toggleActive(alert.getCoinId(), !alert.isActive(), null);
        }
    }
}
