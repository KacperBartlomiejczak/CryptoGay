package com.example.cryptogay.ui.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.CoinRepository;
import com.example.cryptogay.data.repository.FavoritesRepository;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class DetailsViewModel extends AndroidViewModel {

    private final CoinRepository coinRepository;
    private final FavoritesRepository favoritesRepository;

    private final MutableLiveData<DetailsUiState> uiState = new MutableLiveData<>(DetailsUiState.loading());
    private final MediatorLiveData<Boolean> isFavorite = new MediatorLiveData<>();

    private LiveData<Boolean> currentFavoriteSource;
    private String currentCoinId;
    private String currentTimeSpan = "7";
    private Coin currentCoin;
    private List<Entry> currentEntries = new ArrayList<>();

    public DetailsViewModel(@NonNull Application application) {
        this(application, new CoinRepository(), new FavoritesRepository(application));
    }

    public DetailsViewModel(CoinRepository coinRepository, FavoritesRepository favoritesRepository) {
        super(new Application());
        this.coinRepository = coinRepository;
        this.favoritesRepository = favoritesRepository;
        this.isFavorite.setValue(false);
    }

    public DetailsViewModel(@NonNull Application application, CoinRepository coinRepository, FavoritesRepository favoritesRepository) {
        super(application);
        this.coinRepository = coinRepository;
        this.favoritesRepository = favoritesRepository;
        this.isFavorite.setValue(false);
    }

    public LiveData<DetailsUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getIsFavorite() {
        return isFavorite;
    }

    public void loadCoinDetails(String coinId) {
        if (coinId == null || coinId.trim().isEmpty()) {
            uiState.setValue(DetailsUiState.empty());
            return;
        }

        this.currentCoinId = coinId;
        observeFavorite(coinId);
        uiState.setValue(DetailsUiState.loading());

        coinRepository.fetchCoinDetails(coinId, new CoinRepository.Callback<Coin>() {
            @Override
            public void onSuccess(Coin coin) {
                currentCoin = coin;
                loadChart(coinId, currentTimeSpan);
            }

            @Override
            public void onError(String message) {
                uiState.setValue(DetailsUiState.error(message));
            }
        });
    }

    public void setTimeSpan(String days) {
        if (days == null || days.equals(currentTimeSpan)) {
            return;
        }
        this.currentTimeSpan = days;
        if (currentCoinId != null && currentCoin != null) {
            uiState.setValue(DetailsUiState.chartLoading(currentCoin, currentEntries, currentTimeSpan));
            loadChart(currentCoinId, currentTimeSpan);
        }
    }

    private void loadChart(String coinId, String days) {
        coinRepository.fetchMarketChart(coinId, days, new CoinRepository.Callback<List<Entry>>() {
            @Override
            public void onSuccess(List<Entry> entries) {
                currentEntries = entries != null ? entries : new ArrayList<>();
                uiState.setValue(DetailsUiState.success(currentCoin, currentEntries, currentTimeSpan));
            }

            @Override
            public void onError(String message) {
                // If chart fails, still show coin details if available
                if (currentCoin != null) {
                    uiState.setValue(DetailsUiState.success(currentCoin, currentEntries, currentTimeSpan));
                } else {
                    uiState.setValue(DetailsUiState.error(message));
                }
            }
        });
    }

    private void observeFavorite(String coinId) {
        if (currentFavoriteSource != null) {
            isFavorite.removeSource(currentFavoriteSource);
        }
        currentFavoriteSource = favoritesRepository.isFavorite(coinId);
        if (currentFavoriteSource != null) {
            isFavorite.addSource(currentFavoriteSource, fav -> isFavorite.setValue(Boolean.TRUE.equals(fav)));
        }
    }

    public void toggleFavorite() {
        if (currentCoin == null) return;
        favoritesRepository.toggleFavorite(currentCoin, null);
    }

    public void retry() {
        if (currentCoinId != null) {
            loadCoinDetails(currentCoinId);
        }
    }
}
