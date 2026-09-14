package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cryptogay.data.local.CachedCoin;
import com.example.cryptogay.data.local.CachedCoinDao;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.model.MarketChartResponse;
import com.example.cryptogay.data.remote.CoinGeckoApiService;
import com.example.cryptogay.data.repository.CoinRepository;
import com.github.mikephil.charting.data.Entry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class RateLimitingCacheTest {

    @Mock
    private CoinGeckoApiService mockApiService;

    @Mock
    private CachedCoinDao mockCacheDao;

    @Mock
    private Call<List<Coin>> mockCoinsCall;

    private Executor immediateExecutor = Runnable::run;
    private CoinRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        CoinRepository.clearMemoryCache();
        when(mockApiService.getCoinsMarkets(anyString(), anyString(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(mockCoinsCall);

        repository = new CoinRepository(mockApiService, mockCacheDao, immediateExecutor);
    }

    @Test
    public void fetchCoins_cacheValid_returnsCachedDataWithoutNetworkCall() {
        // Given valid cache (< 10 minutes old)
        long now = System.currentTimeMillis();
        CachedCoin cachedBitcoin = new CachedCoin("bitcoin", "btc", "Bitcoin", "http://btc.png",
                60000.0, 2.5, 0.0, 1, 1000000.0, 50000.0,
                61000.0, 59000.0, 69000.0, -10.0, 19000000.0, 21000000.0, now - 60_000L); // 1 minute old

        when(mockCacheDao.getAllCachedCoins()).thenReturn(Collections.singletonList(cachedBitcoin));
        when(mockCacheDao.getOldestCacheTimestamp()).thenReturn(now - 60_000L);
        when(mockCacheDao.getCachedCoinsCount()).thenReturn(1);

        List<Coin> result = new ArrayList<>();
        repository.fetchCoins(false, new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
                result.addAll(data);
            }

            @Override
            public void onError(String message) {
            }
        });

        // Network call should NEVER be initiated
        verify(mockApiService, never()).getCoinsMarkets(anyString(), anyString(), anyInt(), anyInt(), anyBoolean());
        assertEquals(1, result.size());
        assertEquals("Bitcoin", result.get(0).getName());
    }

    @Test
    public void fetchCoins_cacheExpired_makesNetworkCall() {
        // Given expired cache (> 10 minutes old)
        long now = System.currentTimeMillis();
        long expiredTime = now - (15 * 60 * 1000L); // 15 minutes ago
        CachedCoin oldCoin = new CachedCoin("bitcoin", "btc", "Bitcoin", "http://btc.png",
                50000.0, 1.0, 0.0, 1, 1000000.0, 50000.0,
                51000.0, 49000.0, 69000.0, -10.0, 19000000.0, 21000000.0, expiredTime);

        when(mockCacheDao.getAllCachedCoins()).thenReturn(Collections.singletonList(oldCoin));
        when(mockCacheDao.getOldestCacheTimestamp()).thenReturn(expiredTime);
        when(mockCacheDao.getCachedCoinsCount()).thenReturn(1);

        List<Coin> freshNetworkCoins = Collections.singletonList(
                new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 62000.0, 3.0, 1)
        );

        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(mockCoinsCall, Response.success(freshNetworkCoins));
            return null;
        }).when(mockCoinsCall).enqueue(any());

        List<Coin> result = new ArrayList<>();
        repository.fetchCoins(false, new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
                result.addAll(data);
            }

            @Override
            public void onError(String message) {
            }
        });

        // Network call MUST be triggered
        verify(mockApiService, times(1)).getCoinsMarkets(anyString(), anyString(), anyInt(), anyInt(), anyBoolean());
        assertEquals(1, result.size());
        assertEquals(Double.valueOf(62000.0), result.get(0).getCurrentPrice());
        // Verify cache update
        verify(mockCacheDao, times(1)).insertAll(any());
    }

    @Test
    public void fetchCoins_forceRefresh_makesNetworkCallEvenWhenCacheValid() {
        // Cache is fresh (1 min old)
        long now = System.currentTimeMillis();
        CachedCoin cachedCoin = new CachedCoin("ethereum", "eth", "Ethereum", "http://eth.png",
                3000.0, 1.0, 0.0, 2, 500000.0, 20000.0,
                3100.0, 2900.0, 4800.0, -20.0, 120000000.0, null, now - 60_000L);

        when(mockCacheDao.getAllCachedCoins()).thenReturn(Collections.singletonList(cachedCoin));
        when(mockCacheDao.getOldestCacheTimestamp()).thenReturn(now - 60_000L);
        when(mockCacheDao.getCachedCoinsCount()).thenReturn(1);

        List<Coin> freshCoins = Collections.singletonList(
                new Coin("ethereum", "eth", "Ethereum", "http://eth.png", 3200.0, 5.0, 2)
        );

        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(mockCoinsCall, Response.success(freshCoins));
            return null;
        }).when(mockCoinsCall).enqueue(any());

        List<Coin> result = new ArrayList<>();
        // forceRefresh = true
        repository.fetchCoins(true, new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
                result.addAll(data);
            }

            @Override
            public void onError(String message) {
            }
        });

        // Network call should be made because of forceRefresh
        verify(mockApiService, times(1)).getCoinsMarkets(anyString(), anyString(), anyInt(), anyInt(), anyBoolean());
        assertEquals(1, result.size());
        assertEquals(Double.valueOf(3200.0), result.get(0).getCurrentPrice());
    }

    @Test
    public void fetchCoins_networkError429_fallsBackToCachedData() {
        long now = System.currentTimeMillis();
        CachedCoin cachedCoin = new CachedCoin("solana", "sol", "Solana", "http://sol.png",
                140.0, 4.0, 0.0, 5, 600000.0, 10000.0,
                145.0, 135.0, 260.0, -40.0, 450000000.0, null, now - (20 * 60 * 1000L)); // expired

        when(mockCacheDao.getAllCachedCoins()).thenReturn(Collections.singletonList(cachedCoin));
        when(mockCacheDao.getOldestCacheTimestamp()).thenReturn(now - (20 * 60 * 1000L));
        when(mockCacheDao.getCachedCoinsCount()).thenReturn(1);

        // Network returns 429 Too Many Requests
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), "{\"status\":{\"error_code\":429}}");
        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(mockCoinsCall, Response.error(429, errorBody));
            return null;
        }).when(mockCoinsCall).enqueue(any());

        List<Coin> result = new ArrayList<>();
        final String[] errorHolder = new String[1];

        repository.fetchCoins(true, new CoinRepository.Callback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> data) {
                result.addAll(data);
            }

            @Override
            public void onError(String message) {
                errorHolder[0] = message;
            }
        });

        // Should gracefully fall back to cached data instead of failing completely
        assertEquals(1, result.size());
        assertEquals("Solana", result.get(0).getName());
    }

    @Test
    public void fetchCoinDetails_cachesResultFor10Minutes() {
        Coin coin = new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 60000.0, 2.5, 1);
        Call<List<Coin>> detailCall = mock(Call.class);
        when(mockApiService.getCoinDetails(anyString(), anyString())).thenReturn(detailCall);

        doAnswer(invocation -> {
            retrofit2.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onResponse(detailCall, Response.success(Collections.singletonList(coin)));
            return null;
        }).when(detailCall).enqueue(any());

        // First call
        repository.fetchCoinDetails("bitcoin", new CoinRepository.Callback<Coin>() {
            @Override
            public void onSuccess(Coin data) {}
            @Override
            public void onError(String message) {}
        });

        // Second call immediately after (e.g. user revisited details)
        repository.fetchCoinDetails("bitcoin", new CoinRepository.Callback<Coin>() {
            @Override
            public void onSuccess(Coin data) {
                assertEquals("bitcoin", data.getId());
            }
            @Override
            public void onError(String message) {}
        });

        // API should only be queried once, 2nd call served from RAM cache
        verify(mockApiService, times(1)).getCoinDetails(anyString(), anyString());
    }

    @Test
    public void fetchMarketChart_cachesResultFor10Minutes() {
        MarketChartResponse response = new MarketChartResponse(Arrays.asList(
                Arrays.asList(1000.0, 50000.0),
                Arrays.asList(2000.0, 51000.0)
        ));

        Call<MarketChartResponse> chartCall = mock(Call.class);
        when(mockApiService.getMarketChart(anyString(), anyString(), anyString())).thenReturn(chartCall);

        doAnswer(invocation -> {
            retrofit2.Callback<MarketChartResponse> callback = invocation.getArgument(0);
            callback.onResponse(chartCall, Response.success(response));
            return null;
        }).when(chartCall).enqueue(any());

        // First call
        repository.fetchMarketChart("bitcoin", "7", new CoinRepository.Callback<List<Entry>>() {
            @Override
            public void onSuccess(List<Entry> data) {}
            @Override
            public void onError(String message) {}
        });

        // Second call immediately after
        List<Entry> secondCallResult = new ArrayList<>();
        repository.fetchMarketChart("bitcoin", "7", new CoinRepository.Callback<List<Entry>>() {
            @Override
            public void onSuccess(List<Entry> data) {
                secondCallResult.addAll(data);
            }
            @Override
            public void onError(String message) {}
        });

        // API should only be queried once
        verify(mockApiService, times(1)).getMarketChart(anyString(), anyString(), anyString());
        assertEquals(2, secondCallResult.size());
    }
}
