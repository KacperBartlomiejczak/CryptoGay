package com.example.cryptogay.data.repository;

import androidx.annotation.NonNull;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.model.MarketChartResponse;
import com.example.cryptogay.data.remote.ApiClient;
import com.example.cryptogay.data.remote.CoinGeckoApiService;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class CoinRepository {

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    private final CoinGeckoApiService apiService;

    public CoinRepository() {
        this(ApiClient.getApiService());
    }

    public CoinRepository(CoinGeckoApiService apiService) {
        this.apiService = apiService;
    }

    public void fetchCoins(Callback<List<Coin>> callback) {
        Call<List<Coin>> call = apiService.getCoinsMarkets("usd", "market_cap_desc", 100, 1, false);
        call.enqueue(new retrofit2.Callback<List<Coin>>() {
            @Override
            public void onResponse(@NonNull Call<List<Coin>> call, @NonNull Response<List<Coin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Błąd serwera: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Coin>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Błąd sieci");
            }
        });
    }

    public void fetchCoinDetails(String coinId, Callback<Coin> callback) {
        Call<List<Coin>> call = apiService.getCoinDetails("usd", coinId);
        call.enqueue(new retrofit2.Callback<List<Coin>>() {
            @Override
            public void onResponse(@NonNull Call<List<Coin>> call, @NonNull Response<List<Coin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Coin> list = response.body();
                    if (!list.isEmpty()) {
                        callback.onSuccess(list.get(0));
                    } else {
                        callback.onError("Nie znaleziono kryptowaluty");
                    }
                } else {
                    callback.onError("Błąd serwera: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Coin>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Błąd sieci");
            }
        });
    }

    public void fetchMarketChart(String coinId, String days, Callback<List<Entry>> callback) {
        Call<MarketChartResponse> call = apiService.getMarketChart(coinId, "usd", days);
        call.enqueue(new retrofit2.Callback<MarketChartResponse>() {
            @Override
            public void onResponse(@NonNull Call<MarketChartResponse> call, @NonNull Response<MarketChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<List<Double>> rawPrices = response.body().getPrices();
                    List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < rawPrices.size(); i++) {
                        List<Double> point = rawPrices.get(i);
                        if (point != null && point.size() >= 2) {
                            Double timestamp = point.get(0);
                            Double price = point.get(1);
                            if (price != null) {
                                Long time = timestamp != null ? timestamp.longValue() : 0L;
                                entries.add(new Entry(i, price.floatValue(), time));
                            }
                        }
                    }
                    callback.onSuccess(entries);
                } else {
                    callback.onError("Błąd pobierania wykresu: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MarketChartResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Błąd sieci wykresu");
            }
        });
    }

    public List<Coin> filterCoins(List<Coin> coins, String query) {
        if (coins == null) {
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(coins);
        }
        String lowerQuery = query.trim().toLowerCase();
        List<Coin> filtered = new ArrayList<>();
        for (Coin coin : coins) {
            boolean nameMatches = coin.getName() != null && coin.getName().toLowerCase().contains(lowerQuery);
            boolean symbolMatches = coin.getSymbol() != null && coin.getSymbol().toLowerCase().contains(lowerQuery);
            if (nameMatches || symbolMatches) {
                filtered.add(coin);
            }
        }
        return filtered;
    }
}
