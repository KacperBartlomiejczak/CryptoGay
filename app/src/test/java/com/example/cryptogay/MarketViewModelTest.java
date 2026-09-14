package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.repository.CoinRepository;
import com.example.cryptogay.ui.market.MarketUiState;
import com.example.cryptogay.ui.market.MarketViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MarketViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private CoinRepository mockRepository;

    private MarketViewModel viewModel;

    private final List<Coin> testCoins = Arrays.asList(
            new Coin("bitcoin", "btc", "Bitcoin", "url", 65000.0, 3.2, 1),
            new Coin("ethereum", "eth", "Ethereum", "url", 3500.0, -0.8, 2),
            new Coin("cardano", "ada", "Cardano", "url", 0.45, 1.1, 10)
    );

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        org.mockito.Mockito.when(mockRepository.filterCoins(any(), org.mockito.ArgumentMatchers.eq("")))
                .thenAnswer(invocation -> invocation.getArgument(0));
        viewModel = new MarketViewModel(mockRepository);
    }

    @Test
    public void loadCoins_success_emitsLoadingThenSuccess() {
        doAnswer(invocation -> {
            CoinRepository.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onSuccess(testCoins);
            return null;
        }).when(mockRepository).fetchCoins(any());

        viewModel.loadCoins();

        MarketUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(MarketUiState.Status.SUCCESS, state.getStatus());
        assertEquals(3, state.getCoins().size());
        assertEquals("Bitcoin", state.getCoins().get(0).getName());
    }

    @Test
    public void loadCoins_error_emitsLoadingThenError() {
        doAnswer(invocation -> {
            CoinRepository.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onError("Network timeout");
            return null;
        }).when(mockRepository).fetchCoins(any());

        viewModel.loadCoins();

        MarketUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(MarketUiState.Status.ERROR, state.getStatus());
        assertEquals("Network timeout", state.getErrorMessage());
    }

    @Test
    public void searchCoins_matchingQuery_emitsFilteredSuccess() {
        doAnswer(invocation -> {
            CoinRepository.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onSuccess(testCoins);
            return null;
        }).when(mockRepository).fetchCoins(any());

        doAnswer(invocation -> Collections.singletonList(testCoins.get(0)))
                .when(mockRepository).filterCoins(any(), org.mockito.ArgumentMatchers.eq("bit"));

        viewModel.loadCoins();
        viewModel.searchCoins("bit");

        MarketUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(MarketUiState.Status.SUCCESS, state.getStatus());
        assertEquals(1, state.getCoins().size());
        assertEquals("Bitcoin", state.getCoins().get(0).getName());
    }

    @Test
    public void searchCoins_noMatch_emitsEmptyState() {
        doAnswer(invocation -> {
            CoinRepository.Callback<List<Coin>> callback = invocation.getArgument(0);
            callback.onSuccess(testCoins);
            return null;
        }).when(mockRepository).fetchCoins(any());

        doAnswer(invocation -> Collections.emptyList())
                .when(mockRepository).filterCoins(any(), org.mockito.ArgumentMatchers.eq("unknown_coin"));

        viewModel.loadCoins();
        viewModel.searchCoins("unknown_coin");

        MarketUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(MarketUiState.Status.EMPTY, state.getStatus());
        assertTrue(state.getCoins().isEmpty());
    }

    private void assertTrue(boolean condition) {
        org.junit.Assert.assertTrue(condition);
    }
}
