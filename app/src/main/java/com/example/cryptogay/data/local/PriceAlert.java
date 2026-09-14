package com.example.cryptogay.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "price_alerts")
public class PriceAlert {

    @PrimaryKey
    @NonNull
    private String coinId;

    private String coinSymbol;
    private String coinName;
    private String coinImage;
    private double targetPrice;
    private boolean isAbove;
    private boolean isActive;
    private long createdAt;

    public PriceAlert(@NonNull String coinId, String coinSymbol, String coinName,
                      String coinImage, double targetPrice, boolean isAbove,
                      boolean isActive, long createdAt) {
        this.coinId = coinId;
        this.coinSymbol = coinSymbol;
        this.coinName = coinName;
        this.coinImage = coinImage;
        this.targetPrice = targetPrice;
        this.isAbove = isAbove;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(@NonNull String coinId) {
        this.coinId = coinId;
    }

    public String getCoinSymbol() {
        return coinSymbol;
    }

    public void setCoinSymbol(String coinSymbol) {
        this.coinSymbol = coinSymbol;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinImage() {
        return coinImage;
    }

    public void setCoinImage(String coinImage) {
        this.coinImage = coinImage;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public boolean isAbove() {
        return isAbove;
    }

    public void setAbove(boolean above) {
        isAbove = above;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceAlert that = (PriceAlert) o;
        return Double.compare(that.targetPrice, targetPrice) == 0 &&
                isAbove == that.isAbove &&
                isActive == that.isActive &&
                coinId.equals(that.coinId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coinId, targetPrice, isAbove, isActive);
    }
}
