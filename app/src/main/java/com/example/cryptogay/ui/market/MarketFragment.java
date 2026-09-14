package com.example.cryptogay.ui.market;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cryptogay.R;
import com.example.cryptogay.databinding.FragmentMarketBinding;

import java.util.Collections;

public class MarketFragment extends Fragment {

    private FragmentMarketBinding binding;
    private MarketViewModel viewModel;
    private CryptoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMarketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MarketViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupListeners();
        observeViewModel();

        if (savedInstanceState == null) {
            viewModel.loadCoins();
        }
    }

    private void setupRecyclerView() {
        adapter = new CryptoAdapter(coin -> {
            if (coin != null && coin.getId() != null) {
                Bundle args = new Bundle();
                args.putString("coin_id", coin.getId());
                androidx.navigation.Navigation.findNavController(requireView())
                        .navigate(R.id.action_marketFragment_to_detailsFragment, args);
            }
        });
        binding.rvCoins.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCoins.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString() : "";
                binding.ivClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                viewModel.searchCoins(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.ivClearSearch.setOnClickListener(v -> binding.etSearch.setText(""));
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadCoins());
        binding.btnRetry.setOnClickListener(v -> viewModel.retry());
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state.getStatus()) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    showSuccess(state);
                    break;
                case EMPTY:
                    showEmpty();
                    break;
                case ERROR:
                    showError(state);
                    break;
            }
        });
    }

    private void showLoading() {
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.layoutLoading.setVisibility(View.VISIBLE);
            binding.swipeRefresh.setVisibility(View.GONE);
        }
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    private void showSuccess(MarketUiState state) {
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.VISIBLE);
        adapter.submitList(state.getCoins());
    }

    private void showEmpty() {
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        adapter.submitList(Collections.emptyList());
    }

    private void showError(MarketUiState state) {
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);

        if (state.getErrorMessage() != null && !state.getErrorMessage().isEmpty()) {
            binding.tvErrorMessage.setText(state.getErrorMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
