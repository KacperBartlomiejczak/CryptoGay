package com.example.cryptogay.ui.market;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.CoinRepository;

import java.util.ArrayList;
import java.util.List;

public class MarketViewModel extends AndroidViewModel {

    private final CoinRepository repository;
    private final MutableLiveData<MarketUiState> uiState = new MutableLiveData<>();
    private final List<Coin> allCoins = new ArrayList<>();
    private String currentQuery = "";

    public MarketViewModel() {
        this(new Application(), new CoinRepository());
    }

    public MarketViewModel(@NonNull Application application) {
        this(application, CoinRepository.getInstance(application));
    }

    public MarketViewModel(CoinRepository repository) {
        this(new Application(), repository);
    }

    public MarketViewModel(@NonNull Application application, CoinRepository repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<MarketUiState> getUiState() {
        return uiState;
    }

    public void loadCoins() {
        loadCoins(false);
    }

    public void refreshCoins() {
        loadCoins(true);
    }

    public void loadCoins(boolean forceRefresh) {
        if (allCoins.isEmpty()) {
            uiState.setValue(MarketUiState.loading());
        }

        CoinRepository.Callback<List<Coin>> callback = new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
                allCoins.clear();
                if (data != null) {
                    allCoins.addAll(data);
                }
                applyFilter();
            }

            @Override
            public void onError(String message) {
                if (allCoins.isEmpty()) {
                    uiState.setValue(MarketUiState.error(message));
                } else {
                    applyFilter();
                }
            }
        };

        if (forceRefresh) {
            repository.fetchCoins(true, callback);
        } else {
            repository.fetchCoins(callback);
        }
    }

    public void searchCoins(String query) {
        this.currentQuery = query != null ? query : "";
        if (uiState.getValue() != null && uiState.getValue().getStatus() == MarketUiState.Status.LOADING) {
            return;
        }
        applyFilter();
    }

    private void applyFilter() {
        List<Coin> filtered = repository.filterCoins(allCoins, currentQuery);
        if (filtered == null) {
            filtered = new ArrayList<>(allCoins);
        }
        if (filtered.isEmpty() && currentQuery != null && !currentQuery.trim().isEmpty()) {
            uiState.setValue(MarketUiState.empty());
        } else {
            uiState.setValue(MarketUiState.success(filtered));
        }
    }

    public void retry() {
        loadCoins(true);
    }
}
