package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.model.MarketChartResponse;
import com.example.cryptogay.data.remote.CoinGeckoApiService;
import com.example.cryptogay.data.repository.CoinRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class CoinRepositoryTest {

    @Mock
    private CoinGeckoApiService mockApiService;

    @Mock
    private Call<List<Coin>> mockCall;

    private CoinRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new CoinRepository(mockApiService);
        when(mockApiService.getCoinsMarkets(anyString(), anyString(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(mockCall);
    }

    @Test
    public void fetchCoins_success_returnsDataToCallback() {
        List<Coin> mockCoins = Arrays.asList(
                new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 60000.0, 2.5, 1),
                new Coin("ethereum", "eth", "Ethereum", "http://eth.png", 3000.0, -1.2, 2)
        );

        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(mockCoins));
            return null;
        }).when(mockCall).enqueue(any());

        List<Coin> resultList = new ArrayList<>();
        repository.fetchCoins(new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
                resultList.addAll(data);
            }

            @Override
            public void onError(String message) {
            }
        });

        assertEquals(2, resultList.size());
        assertEquals("Bitcoin", resultList.get(0).getName());
    }

    @Test
    public void fetchCoins_httpError_returnsErrorToCallback() {
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"rate limit\"}");
        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.error(429, errorBody));
            return null;
        }).when(mockCall).enqueue(any());

        final String[] receivedError = new String[1];
        repository.fetchCoins(new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
            }

            @Override
            public void onError(String message) {
                receivedError[0] = message;
            }
        });

        assertTrue(receivedError[0] != null && !receivedError[0].isEmpty());
    }

    @Test
    public void fetchCoins_networkFailure_returnsErrorToCallback() {
        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onFailure(mockCall, new IOException("No internet connection"));
            return null;
        }).when(mockCall).enqueue(any());

        final String[] receivedError = new String[1];
        repository.fetchCoins(new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
            }

            @Override
            public void onError(String message) {
                receivedError[0] = message;
            }
        });

        assertTrue(receivedError[0] != null && !receivedError[0].isEmpty());
    }

    @Test
    public void filterCoins_byNameOrSymbol_returnsMatchingSubset() {
        List<Coin> list = Arrays.asList(
                new Coin("bitcoin", "btc", "Bitcoin", "", 60000.0, 1.0, 1),
                new Coin("ethereum", "eth", "Ethereum", "", 3000.0, 1.0, 2),
                new Coin("solana", "sol", "Solana", "", 150.0, 1.0, 3),
                new Coin("dogecoin", "doge", "Dogecoin", "", 0.12, 1.0, 4)
        );

        List<Coin> resultByName = repository.filterCoins(list, "bit");
        assertEquals(1, resultByName.size());
        assertEquals("Bitcoin", resultByName.get(0).getName());

        List<Coin> resultBySymbol = repository.filterCoins(list, "eth");
        assertEquals(1, resultBySymbol.size());
        assertEquals("Ethereum", resultBySymbol.get(0).getName());

        List<Coin> resultCaseInsensitive = repository.filterCoins(list, "SOL");
        assertEquals(1, resultCaseInsensitive.size());
        assertEquals("Solana", resultCaseInsensitive.get(0).getName());

        List<Coin> resultEmptyQuery = repository.filterCoins(list, "");
        assertEquals(4, resultEmptyQuery.size());

        List<Coin> resultNullQuery = repository.filterCoins(list, null);
        assertEquals(4, resultNullQuery.size());

        List<Coin> resultNoMatch = repository.filterCoins(list, "nonexistent");
        assertEquals(0, resultNoMatch.size());
    }

    @Test
    public void fetchCoinDetails_success_returnsCoin() {
        Coin mockCoin = new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 60000.0, 2.5, 1);
        Call<List<Coin>> detailCall = mock(Call.class);
        when(mockApiService.getCoinDetails(anyString(), anyString())).thenReturn(detailCall);

        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(detailCall, Response.success(Collections.singletonList(mockCoin)));
            return null;
        }).when(detailCall).enqueue(any());

        final Coin[] result = new Coin[1];
        repository.fetchCoinDetails("bitcoin", new CoinRepository.Callback<Coin>() {
            @Override
            public void onSuccess(Coin data) {
                result[0] = data;
            }

            @Override
            public void onError(String message) {
            }
        });

        assertTrue(result[0] != null);
        assertEquals("bitcoin", result[0].getId());
    }

    @Test
    public void fetchMarketChart_success_returnsMappedEntries() {
        MarketChartResponse response = new MarketChartResponse(Arrays.asList(
                Arrays.asList(1000.0, 50000.0),
                Arrays.asList(2000.0, 51000.0),
                Arrays.asList(3000.0, 52000.0)
        ));

        Call<MarketChartResponse> chartCall = mock(Call.class);
        when(mockApiService.getMarketChart(anyString(), anyString(), anyString())).thenReturn(chartCall);

        doAnswer(invocation -> {
            retrofit2.Callback<MarketChartResponse> callback = invocation.getArgument(0);
            callback.onResponse(chartCall, Response.success(response));
            return null;
        }).when(chartCall).enqueue(any());

        List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        repository.fetchMarketChart("bitcoin", "7", new CoinRepository.Callback<List<com.github.mikephil.charting.data.Entry>>() {
            @Override
            public void onSuccess(List<com.github.mikephil.charting.data.Entry> data) {
                entries.addAll(data);
            }

            @Override
            public void onError(String message) {
            }
        });

        assertEquals(3, entries.size());
        assertEquals(50000.0f, entries.get(0).getY(), 0.01f);
        assertEquals(52000.0f, entries.get(2).getY(), 0.01f);
    }
}

