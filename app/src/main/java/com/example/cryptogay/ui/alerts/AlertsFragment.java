package com.example.cryptogay.ui.alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cryptogay.R;
import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.databinding.FragmentAlertsBinding;

import java.util.List;

public class AlertsFragment extends Fragment {

    private FragmentAlertsBinding binding;
    private AlertsViewModel viewModel;
    private AlertsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAlertsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AlertsViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AlertsAdapter(
                alert -> {
                    Bundle args = new Bundle();
                    args.putString("coin_id", alert.getCoinId());
                    Navigation.findNavController(requireView()).navigate(R.id.action_alertsFragment_to_detailsFragment, args);
                },
                alert -> {
                    viewModel.deleteAlert(alert);
                    Toast.makeText(requireContext(), R.string.alert_deleted, Toast.LENGTH_SHORT).show();
                }
        );

        binding.recyclerAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAlerts.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            // LiveData from Room is automatic, simply stop spinner
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.btnGoToMarket.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.marketFragment)
        );

        binding.btnRetry.setOnClickListener(v -> {
            // Trigger refresh
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            binding.swipeRefresh.setRefreshing(false);

            switch (state.getStatus()) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    showSuccess(state.getAlerts());
                    break;
                case EMPTY:
                    showEmpty();
                    break;
                case ERROR:
                    showError(state.getErrorMessage());
                    break;
            }
        });
    }

    private void showLoading() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.tvAlertsCount.setVisibility(View.GONE);
    }

    private void showSuccess(List<PriceAlert> alerts) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.VISIBLE);

        adapter.setAlerts(alerts);

        int count = alerts.size();
        String countText = count == 1
                ? getString(R.string.alerts_count_single, count)
                : getString(R.string.alerts_count_plural, count);
        binding.tvAlertsCount.setText(countText);
        binding.tvAlertsCount.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.tvAlertsCount.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvAlertsCount.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
