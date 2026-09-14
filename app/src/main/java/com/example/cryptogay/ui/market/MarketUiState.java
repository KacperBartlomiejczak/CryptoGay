package com.example.cryptogay.ui.market;

import com.example.cryptogay.data.model.Coin;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MarketUiState {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY
    }

    private final Status status;
    private final List<Coin> coins;
    private final String errorMessage;

    public MarketUiState(Status status, List<Coin> coins, String errorMessage) {
        this.status = status;
        this.coins = coins != null ? coins : Collections.emptyList();
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }

    public static MarketUiState loading() {
        return new MarketUiState(Status.LOADING, Collections.emptyList(), null);
    }

    public static MarketUiState success(List<Coin> coins) {
        return new MarketUiState(Status.SUCCESS, coins, null);
    }

    public static MarketUiState error(String errorMessage) {
        return new MarketUiState(Status.ERROR, Collections.emptyList(), errorMessage);
    }

    public static MarketUiState empty() {
        return new MarketUiState(Status.EMPTY, Collections.emptyList(), null);
    }

    public Status getStatus() {
        return status;
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketUiState that = (MarketUiState) o;
        return status == that.status &&
                Objects.equals(coins, that.coins) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, coins, errorMessage);
    }
}
