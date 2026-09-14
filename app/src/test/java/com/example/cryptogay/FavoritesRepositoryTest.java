package com.example.cryptogay;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptogay.data.local.FavoriteCoin;
import com.example.cryptogay.data.local.FavoriteCoinDao;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.FavoritesRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Executor;

public class FavoritesRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private FavoriteCoinDao mockDao;

    private FavoritesRepository repository;

    // Direct synchronous executor for testing
    private final Executor directExecutor = Runnable::run;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new FavoritesRepository(mockDao, directExecutor);
    }

    @Test
    public void isFavorite_delegatesToDao() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>(true);
        when(mockDao.isFavorite("bitcoin")).thenReturn(liveData);

        assertTrue(repository.isFavorite("bitcoin").getValue());
        verify(mockDao).isFavorite("bitcoin");
    }

    @Test
    public void toggleFavorite_whenNotFavorite_insertsFavoriteAndReturnsTrue() {
        when(mockDao.isFavoriteSync("bitcoin")).thenReturn(false);

        Coin coin = new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 65000.0, 3.2, 1);
        final boolean[] result = new boolean[1];

        repository.toggleFavorite(coin, isFav -> result[0] = isFav);

        assertTrue(result[0]);
        verify(mockDao).insertFavorite(any(FavoriteCoin.class));
    }

    @Test
    public void toggleFavorite_whenAlreadyFavorite_deletesFavoriteAndReturnsFalse() {
        when(mockDao.isFavoriteSync("bitcoin")).thenReturn(true);

        Coin coin = new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 65000.0, 3.2, 1);
        final boolean[] result = new boolean[1];

        repository.toggleFavorite(coin, isFav -> result[0] = isFav);

        assertFalse(result[0]);
        verify(mockDao).deleteFavoriteById(eq("bitcoin"));
    }
}
