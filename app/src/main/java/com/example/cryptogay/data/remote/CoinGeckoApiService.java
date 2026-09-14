package com.example.cryptogay.data.remote;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.model.MarketChartResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoinGeckoApiService {

    @GET("api/v3/coins/markets")
    Call<List<Coin>> getCoinsMarkets(
            @Query("vs_currency") String vsCurrency,
            @Query("order") String order,
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sparkline") boolean sparkline
    );

    @GET("api/v3/coins/markets")
    Call<List<Coin>> getCoinDetails(
            @Query("vs_currency") String vsCurrency,
            @Query("ids") String ids
    );

    @GET("api/v3/coins/{id}/market_chart")
    Call<MarketChartResponse> getMarketChart(
            @Path("id") String coinId,
            @Query("vs_currency") String vsCurrency,
            @Query("days") String days
    );
}
