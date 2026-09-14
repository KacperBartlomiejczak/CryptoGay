package com.example.cryptogay.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.cryptogay.data.local.AppDatabase;
import com.example.cryptogay.data.local.CachedCoin;
import com.example.cryptogay.data.local.CachedCoinDao;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.model.MarketChartResponse;
import com.example.cryptogay.data.remote.ApiClient;
import com.example.cryptogay.data.remote.CoinGeckoApiService;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class CoinRepository {

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public static final long CACHE_DURATION_MS = 10 * 60 * 1000L; // 10 minutes
    public static final long MIN_REFRESH_INTERVAL_MS = 5 * 1000L; // 5 seconds cooldown between network calls

    private static class CachedItem<T> {
        final T data;
        final long timestamp;

        CachedItem(T data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    private static volatile CoinRepository instance;

    public static CoinRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (CoinRepository.class) {
                if (instance == null) {
                    instance = new CoinRepository(context != null ? context.getApplicationContext() : null);
                }
            }
        }
        return instance;
    }

    public static void setInstanceForTesting(CoinRepository testInstance) {
        instance = testInstance;
    }

    public static void clearAllInstances() {
        if (instance != null) {
            instance.clearCache();
            instance = null;
        }
    }

    public static void clearMemoryCache() {
        clearAllInstances();
    }

    private final Object LOCK = new Object();
    private List<Coin> inMemoryCoins;
    private long inMemoryCoinsTimestamp = 0;
    private long lastNetworkFetchTimestamp = 0;
    private final Map<String, CachedItem<Coin>> detailsCache = new ConcurrentHashMap<>();
    private final Map<String, CachedItem<List<Entry>>> chartCache = new ConcurrentHashMap<>();

    private final CoinGeckoApiService apiService;
    private final CachedCoinDao cachedCoinDao;
    private final Executor diskExecutor;

    public CoinRepository() {
        this((Context) null);
    }

    public CoinRepository(Context context) {
        this(ApiClient.getApiService(),
                context != null ? AppDatabase.getInstance(context).cachedCoinDao() : null,
                Executors.newSingleThreadExecutor());
    }

    public CoinRepository(CoinGeckoApiService apiService) {
        this(apiService, null, Executors.newSingleThreadExecutor());
    }

    public CoinRepository(CoinGeckoApiService apiService, CachedCoinDao cachedCoinDao, Executor diskExecutor) {
        this.apiService = apiService != null ? apiService : ApiClient.getApiService();
        this.cachedCoinDao = cachedCoinDao;
        this.diskExecutor = diskExecutor != null ? diskExecutor : Executors.newSingleThreadExecutor();
    }

    public void clearCache() {
        synchronized (LOCK) {
            inMemoryCoins = null;
            inMemoryCoinsTimestamp = 0;
            lastNetworkFetchTimestamp = 0;
            detailsCache.clear();
            chartCache.clear();
        }
    }

    public void fetchCoins(Callback<List<Coin>> callback) {
        fetchCoins(false, callback);
    }

    public void fetchCoins(boolean forceRefresh, Callback<List<Coin>> callback) {
        long now = System.currentTimeMillis();

        // 1. Check in-memory RAM cache first
        synchronized (LOCK) {
            if (!forceRefresh && inMemoryCoins != null && !inMemoryCoins.isEmpty()
                    && (now - inMemoryCoinsTimestamp) < CACHE_DURATION_MS) {
                callback.onSuccess(new ArrayList<>(inMemoryCoins));
                return;
            }
        }

        // 2. Check Room database cache if not force refreshing
        if (!forceRefresh && cachedCoinDao != null) {
            diskExecutor.execute(() -> {
                List<CachedCoin> cachedList = cachedCoinDao.getAllCachedCoins();
                Long oldestTimestamp = cachedCoinDao.getOldestCacheTimestamp();
                if (cachedList != null && !cachedList.isEmpty() && oldestTimestamp != null
                        && (now - oldestTimestamp) < CACHE_DURATION_MS) {
                    List<Coin> coins = mapCachedCoins(cachedList);
                    synchronized (LOCK) {
                        inMemoryCoins = coins;
                        inMemoryCoinsTimestamp = oldestTimestamp;
                    }
                    callback.onSuccess(coins);
                } else {
                    executeNetworkFetch(callback);
                }
            });
            return;
        }

        // 3. Network fetch (forced or cache expired/missing)
        executeNetworkFetch(callback);
    }

    private void executeNetworkFetch(Callback<List<Coin>> callback) {
        long now = System.currentTimeMillis();
        synchronized (LOCK) {
            if ((now - lastNetworkFetchTimestamp) < MIN_REFRESH_INTERVAL_MS && inMemoryCoins != null && !inMemoryCoins.isEmpty()) {
                // Rate limiter cooldown active: serve cached data to prevent 429
                callback.onSuccess(new ArrayList<>(inMemoryCoins));
                return;
            }
            lastNetworkFetchTimestamp = now;
        }

        Call<List<Coin>> call = apiService.getCoinsMarkets("usd", "market_cap_desc", 100, 1, false);
        call.enqueue(new retrofit2.Callback<List<Coin>>() {
            @Override
            public void onResponse(@NonNull Call<List<Coin>> call, @NonNull Response<List<Coin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Coin> freshCoins = response.body();
                    long fetchTime = System.currentTimeMillis();
                    synchronized (LOCK) {
                        inMemoryCoins = new ArrayList<>(freshCoins);
                        inMemoryCoinsTimestamp = fetchTime;
                    }

                    if (cachedCoinDao != null) {
                        diskExecutor.execute(() -> {
                            List<CachedCoin> toCache = new ArrayList<>(freshCoins.size());
                            for (Coin c : freshCoins) {
                                CachedCoin cc = CachedCoin.fromCoin(c, fetchTime);
                                if (cc != null) {
                                    toCache.add(cc);
                                }
                            }
                            cachedCoinDao.deleteAll();
                            cachedCoinDao.insertAll(toCache);
                        });
                    }
                    callback.onSuccess(freshCoins);
                } else {
                    handleFallbackOrError(response.code(), "Błąd serwera: " + response.code(), callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Coin>> call, @NonNull Throwable t) {
                handleFallbackOrError(-1, t.getMessage() != null ? t.getMessage() : "Błąd sieci", callback);
            }
        });
    }

    private void handleFallbackOrError(int code, String defaultErrorMessage, Callback<List<Coin>> callback) {
        synchronized (LOCK) {
            if (inMemoryCoins != null && !inMemoryCoins.isEmpty()) {
                callback.onSuccess(new ArrayList<>(inMemoryCoins));
                return;
            }
        }

        if (cachedCoinDao != null) {
            diskExecutor.execute(() -> {
                List<CachedCoin> cachedList = cachedCoinDao.getAllCachedCoins();
                if (cachedList != null && !cachedList.isEmpty()) {
                    List<Coin> fallbackCoins = mapCachedCoins(cachedList);
                    synchronized (LOCK) {
                        inMemoryCoins = fallbackCoins;
                    }
                    callback.onSuccess(fallbackCoins);
                } else {
                    callback.onError(defaultErrorMessage);
                }
            });
        } else {
            callback.onError(defaultErrorMessage);
        }
    }

    public void fetchCoinDetails(String coinId, Callback<Coin> callback) {
        fetchCoinDetails(coinId, false, callback);
    }

    public void fetchCoinDetails(String coinId, boolean forceRefresh, Callback<Coin> callback) {
        if (coinId == null || coinId.trim().isEmpty()) {
            callback.onError("Nieprawidłowe ID kryptowaluty");
            return;
        }

        long now = System.currentTimeMillis();
        CachedItem<Coin> cached = detailsCache.get(coinId);
        if (!forceRefresh && cached != null && (now - cached.timestamp) < CACHE_DURATION_MS) {
            callback.onSuccess(cached.data);
            return;
        }

        Call<List<Coin>> call = apiService.getCoinDetails("usd", coinId);
        call.enqueue(new retrofit2.Callback<List<Coin>>() {
            @Override
            public void onResponse(@NonNull Call<List<Coin>> call, @NonNull Response<List<Coin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Coin> list = response.body();
                    if (!list.isEmpty()) {
                        Coin coin = list.get(0);
                        detailsCache.put(coinId, new CachedItem<>(coin, System.currentTimeMillis()));
                        callback.onSuccess(coin);
                    } else {
                        callback.onError("Nie znaleziono kryptowaluty");
                    }
                } else {
                    if (cached != null) {
                        callback.onSuccess(cached.data);
                    } else {
                        callback.onError("Błąd serwera: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Coin>> call, @NonNull Throwable t) {
                if (cached != null) {
                    callback.onSuccess(cached.data);
                } else {
                    callback.onError(t.getMessage() != null ? t.getMessage() : "Błąd sieci");
                }
            }
        });
    }

    public void fetchMarketChart(String coinId, String days, Callback<List<Entry>> callback) {
        fetchMarketChart(coinId, days, false, callback);
    }

    public void fetchMarketChart(String coinId, String days, boolean forceRefresh, Callback<List<Entry>> callback) {
        String cacheKey = coinId + "_" + days;
        long now = System.currentTimeMillis();
        CachedItem<List<Entry>> cached = chartCache.get(cacheKey);
        if (!forceRefresh && cached != null && (now - cached.timestamp) < CACHE_DURATION_MS) {
            callback.onSuccess(new ArrayList<>(cached.data));
            return;
        }

        Call<MarketChartResponse> call = apiService.getMarketChart(coinId, "usd", days);
        call.enqueue(new retrofit2.Callback<MarketChartResponse>() {
            @Override
            public void onResponse(@NonNull Call<MarketChartResponse> call, @NonNull Response<MarketChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<List<Double>> rawPrices = response.body().getPrices();
                    List<Entry> entries = new ArrayList<>();
                    if (rawPrices != null) {
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
                    }
                    chartCache.put(cacheKey, new CachedItem<>(entries, System.currentTimeMillis()));
                    callback.onSuccess(entries);
                } else {
                    if (cached != null) {
                        callback.onSuccess(new ArrayList<>(cached.data));
                    } else {
                        callback.onError("Błąd pobierania wykresu: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MarketChartResponse> call, @NonNull Throwable t) {
                if (cached != null) {
                    callback.onSuccess(new ArrayList<>(cached.data));
                } else {
                    callback.onError(t.getMessage() != null ? t.getMessage() : "Błąd sieci wykresu");
                }
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

    private List<Coin> mapCachedCoins(List<CachedCoin> cachedList) {
        List<Coin> list = new ArrayList<>(cachedList.size());
        for (CachedCoin cc : cachedList) {
            list.add(cc.toCoin());
        }
        return list;
    }
}
