package com.example.cryptogay.ui.market;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cryptogay.R;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.databinding.ItemCryptoBinding;
import com.example.cryptogay.util.CurrencyFormatter;

import java.util.Objects;

public class CryptoAdapter extends ListAdapter<Coin, CryptoAdapter.CoinViewHolder> {

    public interface OnCoinClickListener {
        void onCoinClick(Coin coin);
    }

    private final OnCoinClickListener clickListener;

    public CryptoAdapter() {
        this(null);
    }

    public CryptoAdapter(OnCoinClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<Coin> DIFF_CALLBACK = new DiffUtil.ItemCallback<Coin>() {
        @Override
        public boolean areItemsTheSame(@NonNull Coin oldItem, @NonNull Coin newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Coin oldItem, @NonNull Coin newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getSymbol(), newItem.getSymbol()) &&
                    Objects.equals(oldItem.getCurrentPrice(), newItem.getCurrentPrice()) &&
                    Objects.equals(oldItem.getPriceChangePercentage24h(), newItem.getPriceChangePercentage24h()) &&
                    Objects.equals(oldItem.getImage(), newItem.getImage());
        }
    };

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCryptoBinding binding = ItemCryptoBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CoinViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CoinViewHolder extends RecyclerView.ViewHolder {

        private final ItemCryptoBinding binding;

        public CoinViewHolder(@NonNull ItemCryptoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Coin coin) {
            Context context = itemView.getContext();

            binding.tvCoinName.setText(coin.getName());
            binding.tvCoinSymbol.setText(coin.getSymbol());
            binding.tvCoinPrice.setText(CurrencyFormatter.formatPrice(coin.getCurrentPrice()));

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

            if (clickListener != null) {
                itemView.setOnClickListener(v -> clickListener.onCoinClick(coin));
            }
        }
    }
}
