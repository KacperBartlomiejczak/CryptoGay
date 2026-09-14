package com.example.cryptogay.ui.favorites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.cryptogay.data.local.FavoriteCoin;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.FavoritesRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {

    private final FavoritesRepository favoritesRepository;
    private final MediatorLiveData<FavoritesUiState> uiState = new MediatorLiveData<>();

    public FavoritesViewModel(@NonNull Application application) {
        this(application, new FavoritesRepository(application));
    }

    public FavoritesViewModel(FavoritesRepository favoritesRepository) {
        this(new Application(), favoritesRepository);
    }

    public FavoritesViewModel(@NonNull Application application, FavoritesRepository favoritesRepository) {
        super(application);
        this.favoritesRepository = favoritesRepository;
        init();
    }

    private void init() {
        uiState.setValue(FavoritesUiState.loading());

        LiveData<List<FavoriteCoin>> favoritesSource = favoritesRepository.getAllFavorites();
        if (favoritesSource != null) {
            uiState.addSource(favoritesSource, favoriteList -> {
                if (favoriteList == null || favoriteList.isEmpty()) {
                    uiState.setValue(FavoritesUiState.empty());
                } else {
                    List<Coin> coins = new ArrayList<>(favoriteList.size());
                    for (FavoriteCoin fav : favoriteList) {
                        coins.add(fav.toCoin());
                    }
                    uiState.setValue(FavoritesUiState.success(coins));
                }
            });
        }
    }

    public LiveData<FavoritesUiState> getUiState() {
        return uiState;
    }

    public void removeFavorite(Coin coin) {
        if (coin != null) {
            favoritesRepository.toggleFavorite(coin, null);
        }
    }
}
