package com.example.cryptogay.ui.details;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.cryptogay.R;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.databinding.FragmentDetailsBinding;
import com.example.cryptogay.util.CurrencyFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private DetailsViewModel viewModel;
    private static final String DEFAULT_COIN_ID = "bitcoin";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DetailsViewModel.class);

        setupChart();
        setupListeners();
        observeViewModel();

        String coinId = null;
        if (getArguments() != null) {
            coinId = getArguments().getString("coin_id");
        }
        if (coinId == null || coinId.trim().isEmpty()) {
            coinId = DEFAULT_COIN_ID;
        }

        if (savedInstanceState == null) {
            viewModel.loadCoinDetails(coinId);
        }
    }

    private void setupChart() {
        LineChart chart = binding.lineChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.crypto_text_secondary));
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.crypto_card_border));
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.crypto_text_secondary));
        leftAxis.setTextSize(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return CurrencyFormatter.formatPrice((double) value);
            }
        });

        chart.getAxisRight().setEnabled(false);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnFavorite.setOnClickListener(v -> {
            viewModel.toggleFavorite();
            Boolean current = viewModel.getIsFavorite().getValue();
            boolean willBeFavorite = !Boolean.TRUE.equals(current);
            int toastRes = willBeFavorite ? R.string.favorite_added : R.string.favorite_removed;
            Toast.makeText(requireContext(), toastRes, Toast.LENGTH_SHORT).show();
        });

        binding.btnRetry.setOnClickListener(v -> viewModel.retry());

        binding.btnGoToMarket.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.marketFragment)
        );

        binding.chipGroupTimeSpan.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_24h) {
                viewModel.setTimeSpan("1");
            } else if (checkedId == R.id.chip_7d) {
                viewModel.setTimeSpan("7");
            } else if (checkedId == R.id.chip_30d) {
                viewModel.setTimeSpan("30");
            } else if (checkedId == R.id.chip_90d) {
                viewModel.setTimeSpan("90");
            }
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
                case ERROR:
                    showError(state);
                    break;
                case EMPTY:
                    showEmpty();
                    break;
            }
        });

        viewModel.getIsFavorite().observe(getViewLifecycleOwner(), this::updateFavoriteButton);
    }

    private void showLoading() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    private void showSuccess(DetailsUiState state) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        Coin coin = state.getCoin();
        if (coin != null) {
            bindCoinData(coin);
        }

        binding.chartProgressBar.setVisibility(state.isChartLoading() ? View.VISIBLE : View.GONE);
        updateChartData(state.getChartEntries(), coin, state.getSelectedTimeSpan());
    }

    private void showError(DetailsUiState state) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);

        if (state.getErrorMessage() != null && !state.getErrorMessage().isEmpty()) {
            binding.tvErrorMessage.setText(state.getErrorMessage());
        }
    }

    private void showEmpty() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void bindCoinData(@NonNull Coin coin) {
        Context context = requireContext();

        binding.tvCoinName.setText(coin.getName());
        binding.tvCoinSymbol.setText(coin.getSymbol());

        if (coin.getMarketCapRank() > 0) {
            binding.tvCoinRank.setText(getString(R.string.stat_rank) + " #" + coin.getMarketCapRank());
            binding.tvCoinRank.setVisibility(View.VISIBLE);
        } else {
            binding.tvCoinRank.setVisibility(View.GONE);
        }

        binding.tvCurrentPrice.setText(CurrencyFormatter.formatPrice(coin.getCurrentPrice()));

        Double change = coin.getPriceChangePercentage24h();
        binding.tvPriceChange.setText(CurrencyFormatter.formatPercentage(change));
        if (CurrencyFormatter.isPositive(change)) {
            binding.tvPriceChange.setBackgroundResource(R.drawable.bg_badge_green);
            binding.tvPriceChange.setTextColor(ContextCompat.getColor(context, R.color.crypto_green));
        } else {
            binding.tvPriceChange.setBackgroundResource(R.drawable.bg_badge_red);
            binding.tvPriceChange.setTextColor(ContextCompat.getColor(context, R.color.crypto_red));
        }

        Glide.with(context)
                .load(coin.getImage())
                .placeholder(R.drawable.bg_coin_placeholder)
                .error(R.drawable.bg_coin_placeholder)
                .into(binding.ivCoinIcon);

        // Stats grid
        binding.tvStatMarketCap.setText(CurrencyFormatter.formatLargeCurrency(coin.getMarketCap()));
        binding.tvStatVolume.setText(CurrencyFormatter.formatLargeCurrency(coin.getTotalVolume()));
        binding.tvStatHigh.setText(CurrencyFormatter.formatPrice(coin.getHigh24h()));
        binding.tvStatLow.setText(CurrencyFormatter.formatPrice(coin.getLow24h()));
        binding.tvStatAth.setText(CurrencyFormatter.formatPrice(coin.getAth()));
        binding.tvStatSupply.setText(CurrencyFormatter.formatSupply(coin.getCirculatingSupply(), coin.getSymbol()));
    }

    private void updateChartData(List<Entry> entries, Coin coin, String timeSpan) {
        LineChart chart = binding.lineChart;
        if (entries == null || entries.isEmpty()) {
            chart.clear();
            return;
        }

        Context context = requireContext();
        boolean isPositive = coin == null || CurrencyFormatter.isPositive(coin.getPriceChangePercentage24h());
        int color = ContextCompat.getColor(context, isPositive ? R.color.crypto_green : R.color.crypto_red);

        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(color);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(35);
        dataSet.setHighLightColor(color);

        // XAxis date formatting
        final SimpleDateFormat sdf = "1".equals(timeSpan)
                ? new SimpleDateFormat("HH:mm", Locale.getDefault())
                : new SimpleDateFormat("dd MMM", Locale.getDefault());

        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < entries.size()) {
                    Object data = entries.get(index).getData();
                    if (data instanceof Long && ((Long) data) > 0) {
                        return sdf.format(new Date((Long) data));
                    }
                }
                return "";
            }
        });

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.animateX(600);
        chart.invalidate();
    }

    private void updateFavoriteButton(Boolean isFav) {
        boolean favorite = Boolean.TRUE.equals(isFav);
        if (favorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_star_filled);
            binding.btnFavorite.setContentDescription(getString(R.string.btn_favorite_remove));
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_star_outline);
            binding.btnFavorite.setContentDescription(getString(R.string.btn_favorite_add));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
