package com.example.cryptogay.ui.favorites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cryptogay.data.model.Coin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesUiState {

    public enum Status {
        LOADING,
        SUCCESS,
        EMPTY,
        ERROR
    }

    private final Status status;
    private final List<Coin> favoriteCoins;
    private final String errorMessage;

    private FavoritesUiState(@NonNull Status status,
                             @Nullable List<Coin> favoriteCoins,
                             @Nullable String errorMessage) {
        this.status = status;
        this.favoriteCoins = favoriteCoins != null ? favoriteCoins : Collections.emptyList();
        this.errorMessage = errorMessage;
    }

    public static FavoritesUiState loading() {
        return new FavoritesUiState(Status.LOADING, Collections.emptyList(), null);
    }

    public static FavoritesUiState success(List<Coin> favoriteCoins) {
        return new FavoritesUiState(Status.SUCCESS, favoriteCoins, null);
    }

    public static FavoritesUiState empty() {
        return new FavoritesUiState(Status.EMPTY, Collections.emptyList(), null);
    }

    public static FavoritesUiState error(String message) {
        return new FavoritesUiState(Status.ERROR, Collections.emptyList(), message);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @NonNull
    public List<Coin> getFavoriteCoins() {
        return favoriteCoins;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public int getCount() {
        return favoriteCoins.size();
    }
}
