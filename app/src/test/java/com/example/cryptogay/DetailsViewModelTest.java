package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.CoinRepository;
import com.example.cryptogay.data.repository.FavoritesRepository;
import com.example.cryptogay.ui.details.DetailsUiState;
import com.example.cryptogay.ui.details.DetailsViewModel;
import com.github.mikephil.charting.data.Entry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class DetailsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private CoinRepository mockCoinRepository;

    @Mock
    private FavoritesRepository mockFavoritesRepository;

    private DetailsViewModel viewModel;

    private final Coin testCoin = new Coin("bitcoin", "btc", "Bitcoin", "http://btc.png", 65000.0, 2.5, 1);
    private final List<Entry> testEntries = Arrays.asList(new Entry(0, 60000), new Entry(1, 65000));

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockFavoritesRepository.isFavorite(any())).thenReturn(new MutableLiveData<>(false));
        viewModel = new DetailsViewModel(mockCoinRepository, mockFavoritesRepository);
    }

    @Test
    public void loadCoinDetails_emitsLoadingAndSuccess() {
        doAnswer(invocation -> {
            CoinRepository.Callback<Coin> callback = invocation.getArgument(1);
            callback.onSuccess(testCoin);
            return null;
        }).when(mockCoinRepository).fetchCoinDetails(eq("bitcoin"), any());

        doAnswer(invocation -> {
            CoinRepository.Callback<List<Entry>> callback = invocation.getArgument(2);
            callback.onSuccess(testEntries);
            return null;
        }).when(mockCoinRepository).fetchMarketChart(eq("bitcoin"), eq("7"), any());

        viewModel.loadCoinDetails("bitcoin");

        DetailsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(DetailsUiState.Status.SUCCESS, state.getStatus());
        assertEquals("Bitcoin", state.getCoin().getName());
        assertEquals(2, state.getChartEntries().size());
    }

    @Test
    public void loadCoinDetails_emitsErrorOnFailure() {
        doAnswer(invocation -> {
            CoinRepository.Callback<Coin> callback = invocation.getArgument(1);
            callback.onError("Błąd sieci");
            return null;
        }).when(mockCoinRepository).fetchCoinDetails(eq("bitcoin"), any());

        viewModel.loadCoinDetails("bitcoin");

        DetailsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(DetailsUiState.Status.ERROR, state.getStatus());
        assertEquals("Błąd sieci", state.getErrorMessage());
    }

    @Test
    public void setTimeSpan_reloadsChartData() {
        doAnswer(invocation -> {
            CoinRepository.Callback<Coin> callback = invocation.getArgument(1);
            callback.onSuccess(testCoin);
            return null;
        }).when(mockCoinRepository).fetchCoinDetails(eq("bitcoin"), any());

        doAnswer(invocation -> {
            CoinRepository.Callback<List<Entry>> callback = invocation.getArgument(2);
            callback.onSuccess(testEntries);
            return null;
        }).when(mockCoinRepository).fetchMarketChart(eq("bitcoin"), any(), any());

        viewModel.loadCoinDetails("bitcoin");

        // Change time span to 30 days
        viewModel.setTimeSpan("30");

        verify(mockCoinRepository).fetchMarketChart(eq("bitcoin"), eq("30"), any());
        assertEquals("30", viewModel.getUiState().getValue().getSelectedTimeSpan());
    }

    @Test
    public void toggleFavorite_callsFavoritesRepository() {
        doAnswer(invocation -> {
            CoinRepository.Callback<Coin> callback = invocation.getArgument(1);
            callback.onSuccess(testCoin);
            return null;
        }).when(mockCoinRepository).fetchCoinDetails(eq("bitcoin"), any());

        viewModel.loadCoinDetails("bitcoin");

        viewModel.toggleFavorite();

        verify(mockFavoritesRepository).toggleFavorite(eq(testCoin), any());
    }
}
