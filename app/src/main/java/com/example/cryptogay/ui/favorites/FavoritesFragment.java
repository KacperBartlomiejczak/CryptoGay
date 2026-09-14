package com.example.cryptogay.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cryptogay.R;
import com.example.cryptogay.databinding.FragmentFavoritesBinding;
import com.example.cryptogay.ui.market.CryptoAdapter;

import java.util.Collections;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private FavoritesViewModel viewModel;
    private CryptoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new CryptoAdapter(coin -> {
            if (coin != null && coin.getId() != null) {
                Bundle args = new Bundle();
                args.putString("coin_id", coin.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_favoritesFragment_to_detailsFragment, args);
            }
        });
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.btnGoToMarket.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.marketFragment)
        );

        binding.btnRetry.setOnClickListener(v -> {
            // Room is reactive, but re-assert state if needed
        });
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
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showSuccess(FavoritesUiState state) {
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.VISIBLE);

        int count = state.getCount();
        String countText = count == 1
                ? getString(R.string.favorites_count_single, count)
                : getString(R.string.favorites_count_plural, count);
        binding.tvFavoritesSubtitle.setText(countText);

        adapter.submitList(state.getFavoriteCoins());
    }

    private void showEmpty() {
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.tvFavoritesSubtitle.setText(R.string.favorites_subtitle);
        adapter.submitList(Collections.emptyList());
    }

    private void showError(FavoritesUiState state) {
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
