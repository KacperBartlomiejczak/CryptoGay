package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.data.repository.AlertsRepository;
import com.example.cryptogay.ui.alerts.AlertsUiState;
import com.example.cryptogay.ui.alerts.AlertsViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AlertsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    @Mock
    private AlertsRepository mockAlertsRepository;

    private MutableLiveData<List<PriceAlert>> alertsLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        alertsLiveData = new MutableLiveData<>();
        when(mockAlertsRepository.getAllAlerts()).thenReturn(alertsLiveData);
    }

    @Test
    public void whenAlertsListIsEmpty_emitsEmptyState() {
        AlertsViewModel viewModel = new AlertsViewModel(mockApplication, mockAlertsRepository);
        viewModel.getUiState().observeForever(state -> {});

        alertsLiveData.setValue(Collections.emptyList());

        AlertsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(AlertsUiState.Status.EMPTY, state.getStatus());
        assertTrue(state.getAlerts().isEmpty());
    }

    @Test
    public void whenAlertsListHasItems_emitsSuccessStateWithAlerts() {
        AlertsViewModel viewModel = new AlertsViewModel(mockApplication, mockAlertsRepository);
        viewModel.getUiState().observeForever(state -> {});

        PriceAlert alert1 = new PriceAlert(
                "bitcoin", "BTC", "Bitcoin", "https://img/btc.png",
                70000.0, true, true, System.currentTimeMillis()
        );
        PriceAlert alert2 = new PriceAlert(
                "ethereum", "ETH", "Ethereum", "https://img/eth.png",
                3000.0, false, true, System.currentTimeMillis()
        );

        alertsLiveData.setValue(Arrays.asList(alert1, alert2));

        AlertsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(AlertsUiState.Status.SUCCESS, state.getStatus());
        assertEquals(2, state.getAlerts().size());

        PriceAlert result1 = state.getAlerts().get(0);
        assertEquals("bitcoin", result1.getCoinId());
        assertEquals(70000.0, result1.getTargetPrice(), 0.001);
        assertTrue(result1.isAbove());
        assertTrue(result1.isActive());

        PriceAlert result2 = state.getAlerts().get(1);
        assertEquals("ethereum", result2.getCoinId());
        assertEquals(3000.0, result2.getTargetPrice(), 0.001);
        assertEquals(false, result2.isAbove());
    }

    @Test
    public void deleteAlert_callsRepositoryDeleteAlert() {
        AlertsViewModel viewModel = new AlertsViewModel(mockApplication, mockAlertsRepository);

        PriceAlert alert = new PriceAlert(
                "solana", "SOL", "Solana", "https://img/sol.png",
                200.0, true, true, System.currentTimeMillis()
        );

        viewModel.deleteAlert(alert);

        verify(mockAlertsRepository).deleteAlert(eq(alert), any());
    }

    @Test
    public void toggleActive_callsRepositoryToggleActive() {
        AlertsViewModel viewModel = new AlertsViewModel(mockApplication, mockAlertsRepository);

        PriceAlert alert = new PriceAlert(
                "solana", "SOL", "Solana", "https://img/sol.png",
                200.0, true, true, System.currentTimeMillis()
        );

        viewModel.toggleActive(alert);

        verify(mockAlertsRepository).toggleActive(eq("solana"), eq(false), any());
    }
}
