package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptogay.data.local.FavoriteCoin;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.FavoritesRepository;
import com.example.cryptogay.ui.favorites.FavoritesUiState;
import com.example.cryptogay.ui.favorites.FavoritesViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FavoritesViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private FavoritesRepository mockFavoritesRepository;

    private MutableLiveData<List<FavoriteCoin>> favoritesLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        favoritesLiveData = new MutableLiveData<>();
        when(mockFavoritesRepository.getAllFavorites()).thenReturn(favoritesLiveData);
    }

    @Test
    public void whenFavoritesListIsEmpty_emitsEmptyState() {
        FavoritesViewModel viewModel = new FavoritesViewModel(mockFavoritesRepository);
        viewModel.getUiState().observeForever(state -> {});

        favoritesLiveData.setValue(Collections.emptyList());

        FavoritesUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(FavoritesUiState.Status.EMPTY, state.getStatus());
        assertTrue(state.getFavoriteCoins().isEmpty());
    }

    @Test
    public void whenFavoritesListHasItems_emitsSuccessStateWithMappedCoins() {
        FavoritesViewModel viewModel = new FavoritesViewModel(mockFavoritesRepository);
        viewModel.getUiState().observeForever(state -> {});

        FavoriteCoin fav1 = new FavoriteCoin(
                "bitcoin", "btc", "Bitcoin", "https://img/btc.png",
                65000.0, 3.5, System.currentTimeMillis()
        );
        FavoriteCoin fav2 = new FavoriteCoin(
                "ethereum", "eth", "Ethereum", "https://img/eth.png",
                3400.0, -1.2, System.currentTimeMillis()
        );
        favoritesLiveData.setValue(Arrays.asList(fav1, fav2));

        FavoritesUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(FavoritesUiState.Status.SUCCESS, state.getStatus());
        assertEquals(2, state.getFavoriteCoins().size());

        Coin coin1 = state.getFavoriteCoins().get(0);
        assertEquals("bitcoin", coin1.getId());
        assertEquals("BTC", coin1.getSymbol());
        assertEquals("Bitcoin", coin1.getName());
        assertEquals(65000.0, coin1.getCurrentPrice(), 0.001);
        assertEquals(3.5, coin1.getPriceChangePercentage24h(), 0.001);

        Coin coin2 = state.getFavoriteCoins().get(1);
        assertEquals("ethereum", coin2.getId());
        assertEquals("ETH", coin2.getSymbol());
    }

    @Test
    public void removeFavorite_callsRepositoryToggleFavorite() {
        FavoritesViewModel viewModel = new FavoritesViewModel(mockFavoritesRepository);

        Coin coin = new Coin("solana", "sol", "Solana", "https://img/sol.png", 140.0, 4.0, 5);
        viewModel.removeFavorite(coin);

        verify(mockFavoritesRepository).toggleFavorite(eq(coin), any());
    }
}
