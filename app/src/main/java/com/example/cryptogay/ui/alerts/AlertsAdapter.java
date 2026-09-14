package com.example.cryptogay.ui.alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cryptogay.R;
import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.databinding.ItemAlertBinding;
import com.example.cryptogay.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(PriceAlert alert);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(PriceAlert alert);
    }

    private final List<PriceAlert> alerts = new ArrayList<>();
    private final OnAlertClickListener alertClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public AlertsAdapter(OnAlertClickListener alertClickListener, OnDeleteClickListener deleteClickListener) {
        this.alertClickListener = alertClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    public void setAlerts(List<PriceAlert> newAlerts) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return alerts.size();
            }

            @Override
            public int getNewListSize() {
                return newAlerts != null ? newAlerts.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                if (newAlerts == null) return false;
                return Objects.equals(alerts.get(oldItemPosition).getCoinId(), newAlerts.get(newItemPosition).getCoinId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                if (newAlerts == null) return false;
                PriceAlert oldItem = alerts.get(oldItemPosition);
                PriceAlert newItem = newAlerts.get(newItemPosition);
                return oldItem.equals(newItem);
            }
        });

        alerts.clear();
        if (newAlerts != null) {
            alerts.addAll(newAlerts);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlertBinding binding = ItemAlertBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AlertViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        holder.bind(alerts.get(position));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {

        private final ItemAlertBinding binding;

        AlertViewHolder(@NonNull ItemAlertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PriceAlert alert) {
            Context context = itemView.getContext();

            binding.tvCoinName.setText(alert.getCoinName());
            binding.tvCoinSymbol.setText(alert.getCoinSymbol() != null ? alert.getCoinSymbol().toUpperCase() : "");

            String formattedPrice = CurrencyFormatter.formatPrice(alert.getTargetPrice());
            String conditionText = alert.isAbove()
                    ? context.getString(R.string.alert_condition_above, formattedPrice)
                    : context.getString(R.string.alert_condition_below, formattedPrice);
            binding.tvAlertTarget.setText(context.getString(R.string.alert_target_label, conditionText));

            if (alert.isActive()) {
                binding.tvAlertStatus.setText(R.string.alert_status_active);
                binding.tvAlertStatus.setBackgroundResource(R.drawable.bg_badge_green);
                binding.tvAlertStatus.setTextColor(ContextCompat.getColor(context, R.color.crypto_green));
            } else {
                binding.tvAlertStatus.setText(R.string.alert_status_triggered);
                binding.tvAlertStatus.setBackgroundResource(R.drawable.bg_badge_red);
                binding.tvAlertStatus.setTextColor(ContextCompat.getColor(context, R.color.crypto_red));
            }

            Glide.with(context)
                    .load(alert.getCoinImage())
                    .placeholder(R.drawable.bg_coin_placeholder)
                    .error(R.drawable.bg_coin_placeholder)
                    .into(binding.ivCoinIcon);

            binding.getRoot().setOnClickListener(v -> {
                if (alertClickListener != null) {
                    alertClickListener.onAlertClick(alert);
                }
            });

            binding.btnDeleteAlert.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(alert);
                }
            });
        }
    }
}
