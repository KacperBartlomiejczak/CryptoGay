package com.example.cryptogay.ui.market;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.CoinRepository;

import java.util.ArrayList;
import java.util.List;

public class MarketViewModel extends ViewModel {

    private final CoinRepository repository;
    private final MutableLiveData<MarketUiState> uiState = new MutableLiveData<>();
    private final List<Coin> allCoins = new ArrayList<>();
    private String currentQuery = "";

    public MarketViewModel() {
        this(new CoinRepository());
    }

    public MarketViewModel(CoinRepository repository) {
        this.repository = repository;
    }

    public LiveData<MarketUiState> getUiState() {
        return uiState;
    }

    public void loadCoins() {
        uiState.setValue(MarketUiState.loading());
        repository.fetchCoins(new CoinRepository.Callback<List<Coin>>() {
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
                uiState.setValue(MarketUiState.error(message));
            }
        });
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
        loadCoins();
    }
}
