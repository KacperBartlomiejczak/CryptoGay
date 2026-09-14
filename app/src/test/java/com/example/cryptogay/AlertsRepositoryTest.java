package com.example.cryptogay;

import static org.mockito.Mockito.verify;

import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.data.local.PriceAlertDao;
import com.example.cryptogay.data.repository.AlertsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Executor;

public class AlertsRepositoryTest {

    @Mock
    private PriceAlertDao mockPriceAlertDao;

    private AlertsRepository alertsRepository;

    // Direct synchronous executor for testing
    private final Executor directExecutor = Runnable::run;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        alertsRepository = new AlertsRepository(mockPriceAlertDao, directExecutor);
    }

    @Test
    public void saveAlert_callsDaoInsertOrUpdate() {
        PriceAlert alert = new PriceAlert(
                "bitcoin", "BTC", "Bitcoin", "https://img/btc.png",
                75000.0, true, true, System.currentTimeMillis()
        );

        alertsRepository.saveAlert(alert, null);

        verify(mockPriceAlertDao).insertOrUpdate(alert);
    }

    @Test
    public void deleteAlert_callsDaoDelete() {
        PriceAlert alert = new PriceAlert(
                "bitcoin", "BTC", "Bitcoin", "https://img/btc.png",
                75000.0, true, true, System.currentTimeMillis()
        );

        alertsRepository.deleteAlert(alert, null);

        verify(mockPriceAlertDao).delete(alert);
    }

    @Test
    public void deleteAlertByCoinId_callsDaoDeleteByCoinId() {
        alertsRepository.deleteAlertByCoinId("ethereum", null);

        verify(mockPriceAlertDao).deleteByCoinId("ethereum");
    }

    @Test
    public void toggleActive_callsDaoUpdateActiveStatus() {
        alertsRepository.toggleActive("bitcoin", false, null);

        verify(mockPriceAlertDao).updateActiveStatus("bitcoin", false);
    }
}
